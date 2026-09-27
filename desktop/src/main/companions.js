// ═══════════════════════════════════════════════════════════
//  付き添いのプロセス (companion)
//
//  「実行」する本体とは別に、裏で動いていてほしい演習用のサーバー
//  (書店アプリから呼ばれる外部 API 役の customer-hub など) を受け持つ
//
//  雛形ルートの codinable.services.json の processes に書く
//    { "processes": [{ "id": "customer-hub", "dir": "customer-hub",
//                      "run": "gradle:bootRun", "port": 8081, "when": ["gradle:bootRun"] }] }
//
//  - preview にパスを書くと、待ち受けを始めたときにプレビューでそこを開く
//    (React の画面を別プロセスで出す演習では、受講者が見るのはそちら)
//
//  - 「実行」のたびに起動し直す (DB がインメモリなので、毎回初期データから始まる)
//  - when を書くと、その実行対象のときだけ起動する (test では起こさない など)
//  - 実行の「停止」では止めない。止めるのは出力タブの行の右端のボタンと、
//    別のプロジェクトを実行したとき (同じポートを取り合わないため) と、アプリの終了時
//  - 出力は「[id] 」を頭に付けて実行結果に流す
// ═══════════════════════════════════════════════════════════

const fs   = require('fs');
const path = require('path');
const { spawn } = require('child_process');
const { EventEmitter } = require('events');
const { killTree, killPort, decodeOutput } = require('./util');
const net = require('net');

const STDIO = ['ignore', 'pipe', 'pipe'];
const delay = ms => new Promise(resolve => setTimeout(resolve, ms));

// Vite は localhost を ::1 だけで待ち受けることがあるので、IPv4 と IPv6 の両方を見る
function portOpenOn(host, port) {
  return new Promise(resolve => {
    const socket = net.connect({ host, port });
    const done = value => { socket.destroy(); resolve(value); };
    socket.once('connect', () => done(true));
    socket.once('error', () => done(false));
    socket.setTimeout(500, () => done(false));
  });
}
async function portOpen(port) {
  return await portOpenOn('127.0.0.1', port) || await portOpenOn('::1', port);
}

/** codinable.services.json の processes を読む。書き方の誤りは例外にする */
function requirements(projectDir) {
  const file = path.join(projectDir, 'codinable.services.json');
  if (!fs.existsSync(file)) return [];
  if (fs.statSync(file).size > 4096) throw new Error('codinable.services.json is too large');
  const list = JSON.parse(fs.readFileSync(file, 'utf8')).processes;
  if (list === undefined) return [];
  if (!Array.isArray(list)) throw new Error('codinable.services.json: processes must be an array');
  return list.map(p => {
    const id = String(p?.id || '');
    const dir = String(p?.dir || '').replace(/\\/g, '/');
    const run = String(p?.run || '');
    const port = Number(p?.port);
    if (!/^[a-z0-9][a-z0-9-]{0,39}$/.test(id)) throw new Error(`codinable.services.json: invalid process id "${id}"`);
    if (!dir || dir.startsWith('/') || /^[a-z]:/i.test(dir) || dir.split('/').includes('..')) {
      throw new Error(`codinable.services.json: invalid dir for ${id}`);
    }
    if (!/^(gradle|npm):[\w:@. -]+$/.test(run)) throw new Error(`codinable.services.json: invalid run for ${id}`);
    if (!Number.isInteger(port) || port < 1024 || port > 65535) throw new Error(`codinable.services.json: invalid port for ${id}`);
    const when = Array.isArray(p.when) ? p.when.map(String) : null;
    const preview = typeof p.preview === 'string' && p.preview.startsWith('/') ? p.preview : null;
    return { id, dir, run, port, when, preview };
  });
}

/** その実行対象 ("gradle:bootRun" など) のときに起こすものか */
function appliesTo(def, target) {
  return !def.when || def.when.includes(target);
}

class CompanionManager extends EventEmitter {
  constructor() {
    super();
    this.items = new Map();   // id → { id, port, projectDir, state, proc, stopping }
  }

  status() {
    return [...this.items.values()].map(({ id, port, projectDir, state, preview }) => ({ id, port, projectDir, state, preview }));
  }

  emitStatus() { this.emit('status', this.status()); }

  log(id, text) { this.emit('log', { id, text }); }

  /** 実行のたびに呼ぶ。他のプロジェクトのものは止め、このプロジェクトのものは起こし直す */
  async restartFor(projectDir, defs, uiLang) {
    await this.stopOthers(projectDir);
    for (const def of defs) {
      await this.stop(def.id);
      await this.start(projectDir, def, uiLang);
    }
  }

  async stopOthers(projectDir) {
    for (const item of [...this.items.values()]) {
      if (path.resolve(item.projectDir) !== path.resolve(projectDir)) await this.stop(item.id);
    }
  }

  async start(projectDir, def, uiLang) {
    const current = this.items.get(def.id);
    if (current && ['starting', 'running'].includes(current.state)) return;

    const cwd = path.join(projectDir, ...def.dir.split('/'));
    if (!fs.existsSync(cwd)) throw new Error(`${def.id}: ${def.dir} が見つかりません`);

    const item = { id: def.id, port: def.port, projectDir, state: 'starting', proc: null, stopping: false,
                   preview: def.preview };
    this.items.set(def.id, item);
    this.emitStatus();

    // 前に残ったもの (ターミナルから起こした分など) がポートを掴んでいると起動できない
    await killPort(def.port);

    const [kind, task] = def.run.split(/:(.*)/s);
    // runner は companions を読むので、循環しないようここで読む
    const { buildSpec } = require('./runner');
    let spec;
    try { spec = buildSpec({ kind, projectDir: cwd, task, uiLang }); }
    catch (err) { return this.fail(item, err.message); }

    this.log(def.id, `▶ ${def.dir}: ${spec.label} (port ${def.port})\n`);
    for (const step of spec.steps || []) {
      const code = await this.exec(item, step);
      if (item.stopping) return;
      if (code !== 0) return this.fail(item, `${step.label} exited with ${code}`);
    }
    if (item.stopping) return;
    this.exec(item, spec.run).then(code => {
      if (this.items.get(def.id) !== item) return;
      if (item.stopping) { item.state = 'stopped'; this.emitStatus(); return; }
      this.fail(item, `exited with ${code}`);
    });
    this.waitReady(item);
  }

  exec(item, step) {
    return new Promise(resolve => {
      let proc;
      try {
        proc = step.shell
          ? spawn(step.command, [], { cwd: step.cwd, env: step.env, shell: true, windowsHide: true, stdio: STDIO })
          : spawn(step.exe, step.args, { cwd: step.cwd, env: step.env, windowsHide: true, stdio: STDIO });
      } catch (err) { this.log(item.id, `${err.message}\n`); resolve(-1); return; }
      item.proc = proc;
      const onData = chunk => this.log(item.id, decodeOutput(chunk));
      proc.stdout.on('data', onData);
      proc.stderr.on('data', onData);
      proc.on('error', err => { this.log(item.id, `${err.message}\n`); resolve(-1); });
      proc.on('close', code => { if (item.proc === proc) item.proc = null; resolve(code); });
    });
  }

  async waitReady(item) {
    while (this.items.get(item.id) === item && item.state === 'starting' && item.proc) {
      if (await portOpen(item.port)) {
        item.state = 'running';
        this.emitStatus();
        return;
      }
      await delay(1000);
    }
  }

  fail(item, message) {
    if (this.items.get(item.id) !== item) return;
    item.state = 'error';
    this.log(item.id, `❌ ${message}\n`);
    this.emitStatus();
  }

  async stop(id) {
    const item = this.items.get(id);
    if (!item || ['stopped', 'error'].includes(item.state) && !item.proc) return;
    item.stopping = true;
    item.state = 'stopping';
    this.emitStatus();
    if (item.proc) await killTree(item.proc);
    // Gradle の bootRun は子の JVM が残ることがあるので、ポートでも止める
    await killPort(item.port);
    item.state = 'stopped';
    this.emitStatus();
  }

  async stopAll() {
    for (const id of [...this.items.keys()]) await this.stop(id);
  }
}

let manager = null;
function getManager() {
  if (!manager) manager = new CompanionManager();
  return manager;
}

module.exports = { requirements, appliesTo, getManager, portOpen };
