// Local learning brokers have their own lifetime, independent of the run button.
const fs = require('fs');
const path = require('path');
const net = require('net');
const http = require('http');
const crypto = require('crypto');
const { spawn } = require('child_process');
const { EventEmitter } = require('events');
const { PORTS, VERSIONS, connectionEnv } = require('../messaging-config');
const { killTree, decodeOutput } = require('./util');

const IDS = ['kafka', 'rabbitmq'];
const delay = ms => new Promise(resolve => setTimeout(resolve, ms));

function requirements(projectDir) {
  const file = path.join(projectDir, 'codinable.services.json');
  if (!fs.existsSync(file)) return [];
  if (fs.statSync(file).size > 4096) throw new Error('codinable.services.json is too large');
  const config = JSON.parse(fs.readFileSync(file, 'utf8'));
  if (!Array.isArray(config.services) || config.services.some(id => !IDS.includes(id))) {
    throw new Error('codinable.services.json: services must contain only "kafka" or "rabbitmq"');
  }
  return [...new Set(config.services)];
}

function portOpen(port) {
  return new Promise(resolve => {
    const socket = net.connect({ host: '127.0.0.1', port });
    const done = value => { socket.destroy(); resolve(value); };
    socket.once('connect', () => done(true));
    socket.once('error', () => done(false));
    socket.setTimeout(500, () => done(false));
  });
}

function assertFree(port) {
  return new Promise((resolve, reject) => {
    const server = net.createServer();
    server.once('error', () => reject(new Error(`Port ${port} is already in use. Stop the other application and retry.`)));
    server.listen({ host: '127.0.0.1', port, exclusive: true }, () => server.close(resolve));
  });
}

function rabbitReady(port) {
  return new Promise(resolve => {
    const req = http.get({ host: '127.0.0.1', port, path: '/api/health/checks/ready-to-serve-clients',
      auth: 'guest:guest', timeout: 800 }, res => { res.resume(); resolve(res.statusCode === 200); });
    req.on('error', () => resolve(false));
    req.on('timeout', () => { req.destroy(); resolve(false); });
  });
}

class MessagingManager extends EventEmitter {
  constructor(options) {
    super();
    this.options = options;
    this.root = path.resolve(options.dataDir);
    this.ports = { ...PORTS, ...options.ports };
    this.states = Object.fromEntries(IDS.map(id => [id, {
      id, state: 'stopped', error: null, log: '', proc: null, auxiliaries: [],
      operation: null, cancelled: false,
    }]));
    this.closing = false;
  }

  state(id) {
    if (!IDS.includes(id)) throw new Error('Unknown messaging service');
    return this.states[id];
  }

  available(id) {
    const o = this.options;
    return id === 'kafka'
      ? fs.existsSync(path.join(o.kafkaDir, 'libs', `kafka_2.13-${VERSIONS.kafka}.jar`)) && fs.existsSync(o.javaExe)
      : fs.existsSync(path.join(o.rabbitmqDir, 'sbin', 'rabbitmq-server.bat')) &&
        fs.existsSync(path.join(o.erlangDir, 'bin', 'erl.exe'));
  }

  status() {
    return IDS.map(id => {
      const s = this.state(id);
      return { id, state: s.state, error: s.error, available: this.available(id),
        version: VERSIONS[id], log: s.log, dataDir: path.join(this.root, id),
        endpoint: id === 'kafka' ? `127.0.0.1:${this.ports.kafka}` : `amqp://guest:guest@127.0.0.1:${this.ports.rabbitmq}/`,
        managementUrl: id === 'rabbitmq' ? `http://127.0.0.1:${this.ports.management}` : null };
    });
  }

  update(s, state, error = null) {
    s.state = state;
    s.error = error;
    this.emit('status', this.status());
  }

  log(s, text) {
    s.log = (s.log + text).slice(-64000);
    this.emit('log', { id: s.id, text });
  }

  launch(s, exe, args, env, auxiliary = false) {
    const proc = spawn(exe, args, { env, cwd: path.join(this.root, s.id), windowsHide: true,
      stdio: ['pipe', 'pipe', 'pipe'] });
    proc.stdout.on('data', b => this.log(s, decodeOutput(b)));
    proc.stderr.on('data', b => this.log(s, decodeOutput(b)));
    proc.stdin.on('error', () => {});
    proc.done = new Promise(resolve => {
      proc.once('error', err => { proc.failure = err; resolve(-1); });
      proc.once('close', code => { proc.closed = true; resolve(code); });
    });
    if (auxiliary) s.auxiliaries.push(proc);
    else {
      s.proc = proc;
      proc.done.then(async code => {
        if (s.proc === proc && s.state === 'running') {
          this.update(s, 'stopping');
          s.operation = this.cleanup(s).finally(() => {
            s.operation = null;
            this.update(s, 'error', `Server exited (${code}). See the log.`);
          });
          await s.operation;
        }
      });
    }
    return proc;
  }

  async command(s, exe, args, env, timeout = 30000) {
    const proc = this.launch(s, exe, args, env, true);
    let timer;
    try {
      const code = await Promise.race([proc.done, new Promise((_, reject) => {
        timer = setTimeout(() => reject(new Error('Command timed out')), timeout);
      })]);
      if (proc.failure) throw proc.failure;
      if (code !== 0) throw new Error(`Command failed (${code}). See the ${s.id} log.`);
    } finally {
      clearTimeout(timer);
      if (!proc.closed) await killTree(proc);
      s.auxiliaries = s.auxiliaries.filter(p => p !== proc);
    }
  }

  checkCancelled(s) {
    if (s.cancelled || this.closing) throw new Error('Startup cancelled');
  }

  start(id) {
    const s = this.state(id);
    if (this.closing) return Promise.reject(new Error('Application is closing'));
    if (s.operation) return s.operation.then(() => {
      if (s.state !== 'running') throw new Error('Startup cancelled');
    });
    if (s.state === 'running') return Promise.resolve();
    s.cancelled = false;
    s.operation = this.startServer(s).finally(() => { s.operation = null; });
    return s.operation;
  }

  async startServer(s) {
    this.update(s, 'starting');
    try {
      if (!this.available(s.id)) throw new Error(`${s.id} is not bundled. Run scripts/setup-messaging.ps1 during development.`);
      fs.mkdirSync(path.join(this.root, s.id), { recursive: true });
      const ports = s.id === 'kafka' ? [this.ports.kafka, this.ports.controller]
        : [this.ports.rabbitmq, this.ports.management, this.ports.distribution, this.ports.epmd];
      for (const port of ports) await assertFree(port);
      this.checkCancelled(s);
      if (s.id === 'kafka') await this.startKafka(s);
      else await this.startRabbit(s);
      const deadline = Date.now() + (this.options.startTimeout || 90000);
      while (Date.now() < deadline) {
        this.checkCancelled(s);
        if (s.proc?.failure) throw s.proc.failure;
        if (!s.proc || s.proc.closed) throw new Error(`${s.id} exited during startup. See the log.`);
        const ready = s.id === 'kafka'
          ? s.log.includes('Kafka Server started') && await portOpen(this.ports.kafka)
          : await rabbitReady(this.ports.management);
        if (ready) { this.checkCancelled(s); this.update(s, 'running'); return; }
        await delay(250);
      }
      throw new Error(`${s.id} startup timed out. See the log.`);
    } catch (err) {
      await this.cleanup(s);
      this.update(s, s.cancelled ? 'stopped' : 'error', s.cancelled ? null : err.message);
      throw err;
    }
  }

  async startKafka(s) {
    const { kafkaDir, javaExe, javacExe } = this.options;
    const dir = path.join(this.root, 'kafka');
    const env = this.environment();
    const cp = path.join(kafkaDir, 'libs', '*');
    const config = path.join(dir, 'server.properties');
    // Java properties escape non-ASCII paths to work for Japanese Windows users too.
    const dataPath = path.join(dir, 'data').replace(/\\/g, '/').replace(/[^\x20-\x7e]/g,
      c => '\\u' + c.charCodeAt(0).toString(16).padStart(4, '0'));
    fs.writeFileSync(config, [
      'process.roles=broker,controller', 'node.id=1',
      `listeners=PLAINTEXT://127.0.0.1:${this.ports.kafka},CONTROLLER://127.0.0.1:${this.ports.controller}`,
      `advertised.listeners=PLAINTEXT://127.0.0.1:${this.ports.kafka}`,
      'listener.security.protocol.map=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT',
      'controller.listener.names=CONTROLLER', 'inter.broker.listener.name=PLAINTEXT',
      `controller.quorum.bootstrap.servers=127.0.0.1:${this.ports.controller}`,
      `log.dirs=${dataPath}`, 'num.partitions=1', 'offsets.topic.replication.factor=1',
      'transaction.state.log.replication.factor=1', 'transaction.state.log.min.isr=1',
      'group.initial.rebalance.delay.ms=0', 'log.retention.hours=24',
      'log.retention.bytes=134217728', 'log.segment.bytes=33554432', '',
    ].join('\n'));
    const javaArgs = ['-Xms128m', '-Xmx512m', '-Dfile.encoding=UTF-8',
      `-Dlog4j2.configurationFile=${path.join(kafkaDir, 'config', 'log4j2.yaml')}`];
    if (!fs.existsSync(path.join(dir, 'data', 'meta.properties'))) {
      await this.command(s, javaExe, [...javaArgs, '-cp', cp, 'kafka.tools.StorageTool',
        'format', '--standalone', '-t', crypto.randomBytes(16).toString('base64url'), '-c', config], env);
    }
    this.checkCancelled(s);
    // The wrapper turns parent stdin EOF/EXIT into a JVM shutdown with Kafka's hooks.
    const source = `import java.io.*;\npublic class CodinableKafka {\n public static void main(String[] args) throws Exception {\n  new Thread(() -> kafka.Kafka.main(args), "kafka").start();\n  new BufferedReader(new InputStreamReader(System.in)).readLine();\n  System.exit(0);\n }\n}\n`;
    const javaFile = path.join(dir, 'CodinableKafka.java');
    if (!fs.existsSync(javaFile) || fs.readFileSync(javaFile, 'utf8') !== source ||
        !fs.existsSync(path.join(dir, 'CodinableKafka.class'))) {
      fs.writeFileSync(javaFile, source);
      await this.command(s, javacExe, ['-encoding', 'UTF-8', '-cp', cp, '-d', dir, javaFile], env);
    }
    this.checkCancelled(s);
    s.log = '';
    this.launch(s, javaExe, [...javaArgs, '-cp', dir + path.delimiter + cp, 'CodinableKafka', config], env);
  }

  environment() {
    const env = { ...(this.options.env || process.env) };
    // A system-wide RabbitMQ/Java installation must not alter these local servers.
    for (const key of Object.keys(env)) {
      if (/^(RABBITMQ_|ERL_|ERLANG_|KAFKA_|JAVA_TOOL_OPTIONS$|_JAVA_OPTIONS$|JDK_JAVA_OPTIONS$)/i.test(key)) delete env[key];
    }
    return env;
  }

  prepareErlang() {
    const dir = path.join(this.root, 'rabbitmq', 'launcher');
    fs.mkdirSync(dir, { recursive: true });
    const root = this.options.erlangDir;
    const erts = fs.readdirSync(root).find(name => /^erts-/.test(name));
    if (!erts) throw new Error('Bundled Erlang runtime is incomplete');
    const bindir = path.join(root, erts, 'bin');
    const exe = path.join(dir, 'erl.exe');
    fs.copyFileSync(path.join(root, 'bin', 'erl.exe'), exe);
    for (const dll of ['vcruntime140.dll', 'vcruntime140_1.dll', 'msvcp140.dll']) {
      fs.copyFileSync(path.join(root, 'bin', dll), path.join(dir, dll));
    }
    const escape = value => value.replace(/\\/g, '\\\\');
    fs.writeFileSync(path.join(dir, 'erl.ini'), `[erlang]\nBindir=${escape(bindir)}\nProgname=erl\nRootdir=${escape(root)}\n`);
    return { exe, bindir };
  }

  async startRabbit(s) {
    const dir = path.join(this.root, 'rabbitmq');
    const { exe, bindir } = this.prepareErlang();
    const node = 'codinable_' + crypto.createHash('sha256').update(this.root).digest('hex').slice(0, 10) + '@localhost';
    const cookieFile = path.join(dir, 'cookie');
    if (!fs.existsSync(cookieFile)) fs.writeFileSync(cookieFile, crypto.randomBytes(24).toString('hex'));
    const cookie = fs.readFileSync(cookieFile, 'utf8').trim();
    const config = path.join(dir, 'rabbitmq.conf');
    fs.writeFileSync(config, [
      `listeners.tcp.1 = 127.0.0.1:${this.ports.rabbitmq}`,
      'management.tcp.ip = 127.0.0.1', `management.tcp.port = ${this.ports.management}`,
      'loopback_users.guest = true', 'log.console = true', 'log.file = false',
      'vm_memory_high_watermark.absolute = 256MiB', 'disk_free_limit.absolute = 100MB', '',
    ].join('\n'));
    const enabled = path.join(dir, 'enabled_plugins');
    fs.writeFileSync(enabled, '[rabbitmq_management].\n');
    const env = { ...this.environment(),
      ERLANG_HOME: this.options.erlangDir, ERL_LIBS: path.join(this.options.rabbitmqDir, 'plugins'),
      ERL_EPMD_PORT: String(this.ports.epmd), ERL_EPMD_ADDRESS: '127.0.0.1',
      ERL_CRASH_DUMP: path.join(dir, 'erl_crash.dump'),
      RABBITMQ_HOME: this.options.rabbitmqDir, RABBITMQ_BASE: dir,
      RABBITMQ_NODENAME: node, RABBITMQ_CONFIG_FILE: config,
      RABBITMQ_ADVANCED_CONFIG_FILE: path.join(dir, 'advanced.config'),
      RABBITMQ_ENABLED_PLUGINS_FILE: enabled,
      RABBITMQ_PLUGINS_DIR: path.join(this.options.rabbitmqDir, 'plugins'),
      RABBITMQ_PLUGINS_EXPAND_DIR: path.join(dir, 'plugins'),
      RABBITMQ_MNESIA_BASE: path.join(dir, 'mnesia'),
      RABBITMQ_LOG_BASE: path.join(dir, 'log'),
    };
    s.rabbit = { exe, node, cookie, env };
    s.log = '';
    this.launch(s, path.join(bindir, 'epmd.exe'), ['-port', String(this.ports.epmd), '-address', '127.0.0.1'], env, true);
    for (let n = 0; n < 40 && !await portOpen(this.ports.epmd); n++) { this.checkCancelled(s); await delay(100); }
    this.checkCancelled(s);
    this.launch(s, exe, ['-boot', 'start_sasl', '-noinput', '-noshell', '-sname', node,
      '-setcookie', cookie, '-start_epmd', 'false', '+S', '2:2',
      '-kernel', 'inet_dist_use_interface', '{127,0,0,1}',
      'inet_dist_listen_min', String(this.ports.distribution),
      'inet_dist_listen_max', String(this.ports.distribution),
      '-s', 'rabbit', 'boot'], env);
  }

  async cleanup(s) {
    const proc = s.proc;
    s.proc = null;
    if (proc && !proc.closed) {
      if (s.id === 'kafka') proc.stdin.end('EXIT\n');
      else if (s.rabbit && !s.cancelled) {
        const r = s.rabbit;
        try {
          await this.command(s, r.exe, ['-noshell', '-hidden', '-sname', 'stop_' + r.node,
            '-setcookie', r.cookie, '-start_epmd', 'false', '+S', '1:1',
            '-eval', `rpc:call('${r.node}', init, stop, []), halt().`], r.env, 8000);
        } catch { /* Fall back to terminating only our own process tree. */ }
      }
      await Promise.race([proc.done, delay(s.cancelled ? 500 : 10000)]);
      if (!proc.closed) { await killTree(proc); await Promise.race([proc.done, delay(3000)]); }
    }
    for (const child of s.auxiliaries.splice(0)) {
      if (!child.closed) await killTree(child);
      await Promise.race([child.done, delay(1000)]);
    }
  }

  async stop(id) {
    const s = this.state(id);
    if (s.state === 'starting') {
      s.cancelled = true;
      this.update(s, 'stopping');
      // Formatting/compilation commands must finish or be stopped before cleanup.
      for (const child of s.auxiliaries) if (!child.closed) await killTree(child);
      await s.operation?.catch(() => {});
    }
    if (s.operation) return s.operation;
    s.operation = (async () => {
      this.update(s, 'stopping');
      await this.cleanup(s);
      this.update(s, 'stopped');
    })().finally(() => { s.operation = null; });
    return s.operation;
  }

  async reset(id) {
    const s = this.state(id);
    if (s.operation || !['stopped', 'error'].includes(s.state)) throw new Error('Stop the server before resetting its data');
    const target = path.resolve(this.root, id);
    if (path.dirname(target) !== this.root) throw new Error('Invalid data directory');
    // IDs are fixed; never delete a path supplied by the renderer or a symlink target.
    if (fs.existsSync(target) && fs.lstatSync(target).isSymbolicLink()) throw new Error('Refusing to reset a linked directory');
    fs.rmSync(target, { recursive: true, force: true });
    s.log = '';
    this.update(s, 'stopped');
  }

  async ensure(ids, cancelled = () => false) {
    for (const id of ids) {
      if (cancelled()) throw new Error('Run cancelled');
      await this.start(id);
    }
  }

  async dispose() {
    this.closing = true;
    await Promise.all(IDS.map(id => this.stop(id)));
  }
}

let singleton;
function getManager() {
  if (!singleton) {
    const { app } = require('electron');
    const rt = require('./runtimes');
    singleton = new MessagingManager({ dataDir: path.join(app.getPath('userData'), 'messaging'),
      kafkaDir: rt.resolveRuntimeDir('kafka'), rabbitmqDir: rt.resolveRuntimeDir('rabbitmq'),
      erlangDir: rt.resolveRuntimeDir('erlang'), javaExe: rt.resolveJavaTool('java'),
      javacExe: rt.resolveJavaTool('javac'), env: rt.getDevEnv() });
  }
  return singleton;
}

module.exports = { MessagingManager, getManager, requirements, connectionEnv, portOpen };
