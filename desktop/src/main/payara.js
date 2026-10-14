// ═══════════════════════════════════════════════════════════
//  Payara Server (Jakarta EE の講座の「▶ アプリ実行」)
//
//  Gradle が作った WAR を、同梱の Payara Server 7 に起動時デプロイして動かす
//  (runner.js の gradle:payaraRun)。サーバーは実行枠の 1 本として扱い、
//  ⏹ や別の実行で止める。同梱物 (runtime/payara) には書き込まない
//
//  ドメインは実行のたびに配布物の domain1 から作り直す。前の演習のアプリ・
//  JMS の保存データ・JSP のコンパイル結果を持ち越さないため (DB を毎回初期データに
//  戻すのと同じ考え方)。作り直しても OSGi のキャッシュだけは引き継ぐ
//  (無いと起動に 15 秒ほど余計にかかる)
//
//  domain1 の既定から変えるのは次の 4 点だけ
//    - 待ち受けを 127.0.0.1 に限る (Windows のファイアウォールの確認を出さない。JMS の 7676 だけは
//      変えられない。patchDomainXml のコメント)
//    - Hazelcast (クラスタリング) を止める (単一サーバーの演習では使わず、起動が 3 秒ほど縮む)
//    - サーバーの JVM の文字コードとロケールを Codinable の表示言語に合わせる
//    - 出力の ANSI カラーを切る (出力欄はエスケープシーケンスを解釈しない)
//  加えて、同梱の HSQLDB の JDBC ドライバーをドメインの lib に置く
//  (asadmin で作る接続プールからも使えるようにするため)
// ═══════════════════════════════════════════════════════════

const fs   = require('fs');
const path = require('path');

const { resolveRuntimeDir, resolveJavaHome, resolveHsqldb } = require('./runtimes');
const { resolveJavaLocale } = require('../java-locale');
const { PORTS } = require('../payara-config');

const DOMAIN = 'codinable';

// 雛形が自分で足す asadmin のコマンド (JDBC の接続プール・JMS の宛先など)
// 起動直後、デプロイの前に流す
const TEMPLATE_COMMANDS = path.join('payara', 'post-boot-commands.asadmin');

function resolvePayaraHome() {
  const dir = resolveRuntimeDir('payara');
  return fs.existsSync(path.join(dir, 'glassfish', 'lib', 'client', 'appserver-cli.jar')) ? dir : null;
}

function domainsDir() {
  const { app } = require('electron');
  return path.join(app.getPath('userData'), 'payara', 'domains');
}

/** domain.xml の server-config だけを書き換える (default-config はクラスタ用の雛形なので触らない) */
function patchDomainXml(xml, uiLang) {
  const start = xml.indexOf('<config name="server-config"');
  const end   = xml.indexOf('</config>', start);
  if (start < 0 || end < 0) throw new Error('Payara の domain.xml に server-config が見つかりません');
  let sc = xml.slice(start, end);

  sc = sc.replace('<hazelcast-config-specific-configuration>',
                  '<hazelcast-config-specific-configuration enabled="false">');
  sc = sc.replace(/address="0\.0\.0\.0"/g, 'address="127.0.0.1"');
  sc = sc.replace(/<network-listener (?![^>]*address=)/g, '<network-listener address="127.0.0.1" ');
  // JMS (7676) だけは 0.0.0.0 のまま。server-config に jms-service を足して host を変えると、
  // 既定の JMS 接続ファクトリ (java:comp/DefaultJMSConnectionFactory) が作られなくなる
  const { language, country } = resolveJavaLocale(uiLang);
  const options = ['-Dfile.encoding=UTF-8', '-Dstdout.encoding=UTF-8', '-Dstderr.encoding=UTF-8',
                   `-Duser.language=${language}`, `-Duser.country=${country}`];
  sc = sc.replace('<jvm-options>-server</jvm-options>',
                  '<jvm-options>-server</jvm-options>' + options.map(o => `<jvm-options>${o}</jvm-options>`).join(''));

  return xml.slice(0, start) + sc + xml.slice(end);
}

function readTextSafe(file) {
  try { return fs.readFileSync(file, 'utf8'); } catch { return ''; }
}

function retryLocked(fn, tries = 10) {
  for (let i = 1; ; i++) {
    try { return fn(); } catch (err) {
      if (i >= tries || !['EPERM', 'EBUSY', 'ENOTEMPTY'].includes(err.code)) throw err;
      Atomics.wait(new Int32Array(new SharedArrayBuffer(4)), 0, 0, 500);
    }
  }
}

/** 配布物の domain1 からドメインを作り直す。返り値はドメインのディレクトリ */
function recreateDomain(payaraHome, uiLang) {
  const root   = domainsDir();
  const domain = path.join(root, DOMAIN);
  const cache  = path.join(domain, 'osgi-cache');
  const kept   = path.join(root, '.osgi-cache');
  // OSGi のキャッシュは Payara の置き場所と版に結び付く。アプリの更新で変わったら捨てる
  // (古いキャッシュのままだと一部のモジュールが読めず、JMS などが動かない)
  const stamp  = path.join(root, '.osgi-cache-source');
  const source = `${payaraHome}\n${readTextSafe(path.join(payaraHome, 'codinable-version.txt')).trim()}`;

  fs.mkdirSync(root, { recursive: true });
  // ⏹ の直後は、止めたサーバーがまだファイルを握っていることがあるので少し待って試し直す
  retryLocked(() => {
    fs.rmSync(kept, { recursive: true, force: true });
    if (fs.existsSync(cache) && readTextSafe(stamp) === source) fs.renameSync(cache, kept);
    fs.rmSync(domain, { recursive: true, force: true });
  });
  fs.cpSync(path.join(payaraHome, 'glassfish', 'domains', 'domain1'), domain, { recursive: true });
  if (fs.existsSync(kept)) fs.renameSync(kept, cache);
  fs.writeFileSync(stamp, source, 'utf8');

  const logging = path.join(domain, 'config', 'logging.properties');
  fs.writeFileSync(logging, fs.readFileSync(logging, 'utf8')
    .replace('UniformLogFormatter.ansiColor=true', 'UniformLogFormatter.ansiColor=false'), 'utf8');
  const domainXml = path.join(domain, 'config', 'domain.xml');
  fs.writeFileSync(domainXml, patchDomainXml(fs.readFileSync(domainXml, 'utf8'), uiLang), 'utf8');

  const hsqldb = resolveHsqldb();
  if (hsqldb) fs.copyFileSync(hsqldb, path.join(domain, 'lib', path.basename(hsqldb)));
  return domain;
}

/** gradle war が作った WAR (build/libs の中でいちばん新しいもの) */
function findWar(projectDir) {
  const libs = path.join(projectDir, 'build', 'libs');
  let wars = [];
  try {
    wars = fs.readdirSync(libs).filter(f => f.toLowerCase().endsWith('.war'))
      .map(f => ({ file: path.join(libs, f), mtime: fs.statSync(path.join(libs, f)).mtimeMs }));
  } catch { /* build/libs が無い = WAR ができていない */ }
  wars.sort((a, b) => b.mtime - a.mtime);
  return wars[0]?.file || null;
}

/**
 * ドメインを作り直し、WAR を起動時にデプロイする Payara の起動コマンドを返す
 * gradle war のあとで呼ぶ (runner.js の prepareRun)
 */
function prepareRun({ projectDir, uiLang, env }) {
  const payaraHome = resolvePayaraHome();
  if (!payaraHome) throw new Error('Payara Server が見つかりません (runtime/payara が未セットアップです)');
  const javaHome = resolveJavaHome();
  if (!javaHome) throw new Error('Java が見つかりません (runtime/java が未セットアップです)');
  const war = findWar(projectDir);
  if (!war) throw new Error('WAR が見つかりません (build/libs に .war がありません)');

  const domain = recreateDomain(payaraHome, uiLang);

  // WAR はドメインの中へ写してから渡す。動いているサーバーが WAR を開いたままでも、
  // 次の gradle war が build/libs を書き換えられるようにするため
  const deployDir = path.join(domain, 'codinable-deploy');
  fs.mkdirSync(deployDir, { recursive: true });
  const deployed = path.join(deployDir, path.basename(war).replace(/[^\w.-]/g, '_'));
  fs.copyFileSync(war, deployed);

  const commands = [];
  const own = path.join(projectDir, TEMPLATE_COMMANDS);
  if (fs.existsSync(own)) {
    commands.push(...fs.readFileSync(own, 'utf8').split(/\r?\n/)
      .map(l => l.trim()).filter(l => l && !l.startsWith('#')));
  }
  // 相対パスは config/ から解決されるので絶対パスで渡す。ユーザー名の空白や日本語に備えて引用符で囲む
  commands.push(`deploy --force=true "${deployed.split(path.sep).join('/')}"`);
  const commandFile = path.join(domain, 'config', 'codinable-post-boot.asadmin');
  fs.writeFileSync(commandFile, commands.join('\n') + '\n', 'utf8');

  const { language, country } = resolveJavaLocale(uiLang);
  const runEnv = { ...env, AS_JAVA: javaHome };
  // サーバーの JVM には domain.xml で同じ指定を渡してある。JAVA_TOOL_OPTIONS が残ると
  // JVM ごとに「Picked up JAVA_TOOL_OPTIONS」が出力に混ざる
  delete runEnv.JAVA_TOOL_OPTIONS;

  return {
    exe:  path.join(javaHome, 'bin', 'java.exe'),
    args: ['-Dfile.encoding=UTF-8', '-Dstdout.encoding=UTF-8', '-Dstderr.encoding=UTF-8',
           `-Duser.language=${language}`, `-Duser.country=${country}`,
           '-jar', path.join(payaraHome, 'glassfish', 'lib', 'client', 'appserver-cli.jar'),
           'start-domain', '--verbose', '--domaindir', path.dirname(domain),
           '--postbootcommandfile', commandFile, DOMAIN],
    cwd:  domain,
    env:  runEnv,
  };
}

// ── 出力の整形 ─────────────────────────────────────────────
//
// Payara の出力は 1 件ずつ [#|日時|レベル|製品|ロガー|属性|本文|#] の形で、本文以外が
// 長い。出力欄では「時刻 レベル 本文」だけを見せる。起動時にランチャーが出す
// JVM のコマンドライン (100 行ほど) も、演習で見るものではないので出さない
// 本文の中身 (アプリのログ・例外のスタックトレース) は削らない

const RECORD = /^\[#\|([^|]*)\|([^|]*)\|[^|]*\|([^|]*)\|[^|]*\|([\s\S]*?)\|#\]$/;

function formatRecord(raw) {
  const m = RECORD.exec(raw);
  if (!m) return raw + '\n';
  const time = (/T(\d\d:\d\d:\d\d)/.exec(m[1]) || [])[1] || '';
  const body = m[4].replace(/^\r?\n/, '').split(/\r?\n/).map(l => l.replace(/^ {2}/, '')).join('\n').trimEnd();
  return `${time} ${m[2]}  ${body}\n`;
}

/**
 * 出力を受け取り、整形した文字列とデプロイの結果を返す
 *   feed(text) → { text, contextRoot?, deployed?, failed? }
 */
function createOutputFilter() {
  let pending   = '';      // 行や 1 件のログの途中で切れた残り
  let record    = null;    // [#| から |#] までの途中
  let launcher  = 0;       // ランチャーの JVM のコマンドラインを読み飛ばしている行数
  let contextRoot = null;

  const take = (line, out, state) => {
    if (record !== null) {
      record += '\n' + line;
      if (line.endsWith('|#]')) { out.push(formatRecord(record)); inspect(record, state); record = null; }
      return;
    }
    if (launcher) {
      // "Launching Payara Server ..." が出たらサーバー本体の出力が始まる
      if (/^Launching Payara Server/.test(line) || ++launcher > 400) launcher = 0;
      else return;
    }
    if (/JVM invocation command line:/.test(line)) { launcher = 1; return; }
    if (/GFLauncherLogger|^#!## LogManagerService/.test(line)) return;
    if (line.startsWith('[#|')) {
      if (line.endsWith('|#]')) { out.push(formatRecord(line)); inspect(line, state); }
      else record = line;
      return;
    }
    out.push(line + '\n');
    inspect(line, state);
  };

  const inspect = (text, state) => {
    const ctx = /Loading application \[[^\]]*\] at \[([^\]]*)\]/.exec(text);
    if (ctx) contextRoot = ctx[1];
    if (/ was successfully deployed in /.test(text)) {
      state.deployed = true;
      state.contextRoot = contextRoot ?? '';
    }
    if (/Boot Command deploy (failed|returned with result FAILURE)|Exception while deploying the app/.test(text)) {
      state.failed = true;
    }
  };

  return {
    feed(text) {
      const lines = (pending + String(text || '')).split(/\r?\n/);
      pending = lines.pop();
      const out = [];
      const state = {};
      for (const line of lines) take(line, out, state);
      return { text: out.join(''), ...state };
    },
  };
}

module.exports = {
  resolvePayaraHome, prepareRun, createOutputFilter,
  PORTS,
  // テスト用
  patchDomainXml, formatRecord, findWar,
};
