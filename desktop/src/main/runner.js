// ═══════════════════════════════════════════════════════════
//  実行エンジン
//
//  ワークスペースのプロジェクトディレクトリをそのまま作業ディレクトリにして実行する
//  (一時ディレクトリへ書き出してから動かすことはしない)。そのため Gradle の
//  ビルドキャッシュも node_modules も 2 回目以降そのまま効き、同じフォルダを
//  VS Code や IntelliJ で開いてもプロジェクトとして成立する。
//
//  実行枠は 1 つ。新しく実行すると前のプロセスは止める (⏹ でも止められる)。
//  常駐させたいプロセスが 2 つ以上あるときはターミナルタブを使う。
//
//  実行中の出力からは「起動した URL」を拾い、見つかったら renderer に伝える。
//  Spring Boot / Django / Express / Vite など、どのフレームワークでも
//  プレビュータブが自動で開くようにするため。
// ═══════════════════════════════════════════════════════════

const fs   = require('fs');
const os   = require('os');
const path = require('path');
const net  = require('net');
const { spawn } = require('child_process');

const {
  IS_WIN, resolveJavaTool, resolveNode, resolveNpm, resolveNpmCli,
  resolvePython, resolveBash,
  resolveHsqldb, resolveGradleWrapperJar, resolveRuntimeDir, getDevEnv,
  getJavaRuntimeOptions, getJavacRuntimeOptions,
} = require('./runtimes');
const crypto = require('crypto');
const { decodeOutput, killTree, killPort, walkTree } = require('./util');
const { resolveProjectDir, detectProject } = require('./workspace');
const { JACOCO_INIT_SCRIPT, collectTestRunArtifacts } = require('../test-report');

const CP_SEP = IS_WIN ? ';' : ':';

let current = null;   // { proc, projectDir, kind, task, isTest }

// 実行の世代番号。前処理 (npm install など) の途中で ⏹ を押されたり、
// 別の実行が始まったりしたときに、古い実行が続きを走らせないようにする。
let runToken = 0;

// ── 起動 URL の検出 ────────────────────────────────────────
//
// 明示的に URL を書き出すもの (Django / Vite / Next / Nuxt / http-server) と、
// ポート番号だけを書くもの (Spring Boot / Express の定番ログ) の両方に対応する。
const URL_PATTERNS = [
  /https?:\/\/(?:localhost|127\.0\.0\.1|0\.0\.0\.0|\[::1\]):(\d{2,5})(?:\/\S*)?/i,
  /Tomcat started on port\(?s?\)?:?\s*(\d{2,5})/i,
  /Tomcat started on port\s*(\d{2,5})/i,
  /(?:listening|Listening|listening on|Server running).{0,20}?port\s*:?\s*(\d{2,5})/i,
  /Netty started on port\(?s?\)?:?\s*(\d{2,5})/i,
];

function detectUrl(text) {
  for (const re of URL_PATTERNS) {
    const m = text.match(re);
    if (!m) continue;
    const port = Number(m[1]);
    if (!Number.isFinite(port) || port < 1 || port > 65535) continue;
    return `http://localhost:${port}`;
  }
  return null;
}

/**
 * stdout/stderr の data イベントは URL の途中でも分割される。
 * 直近の出力をつないでから調べ、`http://local` / `host:5173` のような
 * 境界で分かれても Web プレビューを開けるようにする。
 */
function createUrlDetector() {
  let tail = '';
  return text => {
    tail = (tail + String(text || '')).slice(-8192);
    return detectUrl(tail);
  };
}

/**
 * ログの文面がフレームワークの更新で変わっても、既定ポートが実際に
 * 待ち受けを始めたらプレビューへ進めるための予備検出。
 */
function waitForPort(port, { token, proc, onReady, timeoutMs = 300_000 }) {
  const deadline = Date.now() + timeoutMs;

  const probe = () => {
    if (token !== runToken || !current || current.proc !== proc || Date.now() >= deadline) return;

    const socket = net.createConnection({ host: '127.0.0.1', port });
    let settled = false;
    const retry = () => {
      if (settled) return;
      settled = true;
      socket.destroy();
      setTimeout(probe, 350);
    };
    socket.setTimeout(500);
    socket.once('connect', () => {
      if (settled) return;
      settled = true;
      socket.destroy();
      if (token === runToken && current && current.proc === proc) onReady();
    });
    socket.once('timeout', retry);
    socket.once('error', retry);
  };

  probe();
}

// ── Gradle Wrapper ─────────────────────────────────────────

const GRADLE_DISTRIBUTION = 'gradle-9.6.1-bin.zip';

const GRADLEW_BAT =
  '@if "%DEBUG%"=="" @echo off\r\n' +
  'setlocal\r\n' +
  'set DIRNAME=%~dp0\r\n' +
  'if "%DIRNAME%"=="" set DIRNAME=.\r\n' +
  'set APP_BASE_NAME=%~n0\r\n' +
  'set APP_HOME=%DIRNAME%\r\n' +
  'for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi\r\n' +
  'set DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"\r\n' +
  'if defined JAVA_HOME goto findJavaFromJavaHome\r\n' +
  'set JAVA_EXE=java.exe\r\n' +
  '%JAVA_EXE% -version >NUL 2>&1\r\n' +
  'if %ERRORLEVEL% equ 0 goto execute\r\n' +
  'echo ERROR: JAVA_HOME is not set.\r\n' +
  'goto fail\r\n' +
  ':findJavaFromJavaHome\r\n' +
  'set JAVA_HOME=%JAVA_HOME:"=%\r\n' +
  'set JAVA_EXE=%JAVA_HOME%/bin/java.exe\r\n' +
  'if exist "%JAVA_EXE%" goto execute\r\n' +
  'echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%\r\n' +
  'goto fail\r\n' +
  ':execute\r\n' +
  'set CLASSPATH=%APP_HOME%gradle\\wrapper\\gradle-wrapper.jar\r\n' +
  '"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% ' +
  '"-Dorg.gradle.appname=%APP_BASE_NAME%" -classpath "%CLASSPATH%" ' +
  'org.gradle.wrapper.GradleWrapperMain %*\r\n' +
  ':end\r\n' +
  'if %ERRORLEVEL% equ 0 goto mainEnd\r\n' +
  ':fail\r\n' +
  'set EXIT_CODE=%ERRORLEVEL%\r\n' +
  'if %EXIT_CODE% == 0 set EXIT_CODE=1\r\n' +
  'exit /b %EXIT_CODE%\r\n' +
  ':mainEnd\r\n' +
  'if "%OS%"=="Windows_NT" endlocal\r\n';

/**
 * プロジェクトに Gradle Wrapper を用意する。
 * 雛形には gradlew を同梱せず、同梱の gradle-wrapper.jar からここで作る
 * (受講者が Gradle を別途インストールしなくても済むようにするため)。
 */
function ensureGradleWrapper(projectDir) {
  const wrapperDir = path.join(projectDir, 'gradle', 'wrapper');
  fs.mkdirSync(wrapperDir, { recursive: true });

  const jarDest = path.join(wrapperDir, 'gradle-wrapper.jar');
  if (!fs.existsSync(jarDest)) {
    const jarSrc = resolveGradleWrapperJar();
    if (!jarSrc) return { ok: false, error: 'gradle-wrapper.jar is not bundled' };
    fs.copyFileSync(jarSrc, jarDest);
  }

  const propsPath = path.join(wrapperDir, 'gradle-wrapper.properties');
  if (!fs.existsSync(propsPath)) {
    fs.writeFileSync(propsPath,
      'distributionBase=GRADLE_USER_HOME\n' +
      'distributionPath=wrapper/dists\n' +
      `distributionUrl=https\\://services.gradle.org/distributions/${GRADLE_DISTRIBUTION}\n` +
      'networkTimeout=10000\n' +
      'validateDistributionUrl=true\n' +
      'zipStoreBase=GRADLE_USER_HOME\n' +
      'zipStorePath=wrapper/dists\n');
  }

  if (IS_WIN) {
    const batPath = path.join(projectDir, 'gradlew.bat');
    if (!fs.existsSync(batPath)) fs.writeFileSync(batPath, GRADLEW_BAT);
  }
  return { ok: true };
}

// ── 単体 Java の実行 ───────────────────────────────────────

/** src/main/java 形式にも「フォルダ直置き」にも対応してソースを集める */
function collectJavaSources(projectDir) {
  return walkTree(projectDir, { limit: 2000 })
    .filter(e => !e.dir && e.path.endsWith('.java') && !e.path.includes('/test/'))
    .map(e => path.join(projectDir, e.path.split('/').join(path.sep)));
}

/** package 宣言 + ファイル名から完全修飾クラス名を作る */
function mainClassOf(file) {
  let source = '';
  try { source = fs.readFileSync(file, 'utf8'); } catch {}
  const pkg  = source.match(/^\s*package\s+([\w.]+)\s*;/m);
  const base = path.basename(file).replace(/\.java$/, '');
  return pkg ? `${pkg[1]}.${base}` : base;
}

/** プロジェクト内の「main メソッドを持つファイル」を探す */
function findJavaMain(projectDir, preferred) {
  const sources = collectJavaSources(projectDir);
  if (preferred) {
    const full = path.join(projectDir, preferred.split('/').join(path.sep));
    if (sources.includes(full)) return { sources, mainFile: full };
  }
  const withMain = sources.find(f => {
    try {
      return /public\s+static\s+void\s+main\s*\(/.test(fs.readFileSync(f, 'utf8'));
    } catch { return false; }
  });
  return { sources, mainFile: withMain || sources[0] || null };
}

// ── 依存の自動用意 ─────────────────────────────────────────
//
// 演習は「選んで実行を押したら動く」ことを最優先にする。そのため
// npm install / pip install は受講者に踏ませず、実行の前段として自動で通す。
// 一度用意できたら次回は飛ばすので、待たされるのは初回だけである。

/**
 * npm install が要るかを判定する。
 * node_modules の有無だけで見ると、install が途中で切れた木 (ディレクトリは
 * あるが vite などが入っていない) を「用意済み」と誤判定し、
 * 「'vite' は認識されていません」から永久に抜け出せなくなる。
 * そのため宣言された依存が実際に置かれているかまで見る。
 */
function needsNpmInstall(projectDir) {
  const pkg = path.join(projectDir, 'package.json');
  if (!fs.existsSync(pkg)) return false;

  let deps;
  try {
    const parsed = JSON.parse(fs.readFileSync(pkg, 'utf8'));
    deps = Object.keys({ ...(parsed.dependencies || {}), ...(parsed.devDependencies || {}) });
  } catch { return false; }
  if (!deps.length) return false;

  const modules = path.join(projectDir, 'node_modules');
  if (!fs.existsSync(modules)) return true;
  if (deps.some(name => !fs.existsSync(path.join(modules, ...name.split('/'))))) return true;

  try {
    // 依存を足した後は package.json のほうが新しくなる
    return fs.statSync(pkg).mtimeMs > fs.statSync(modules).mtimeMs;
  } catch { return false; }
}

function npmInstallStep(projectDir, env) {
  const spec = npmSpec(['install', '--no-audit', '--no-fund'], projectDir, env);
  if (!spec) return null;
  return { label: 'npm install', ...spec };
}

/**
 * npm の起動仕様を作る。node で npm-cli.js を直接動かすのが既定で、
 * それが見つからないときだけ npm.cmd + shell に落とす。
 */
function npmSpec(args, projectDir, env) {
  const cli = resolveNpmCli();
  if (cli) {
    return { exe: resolveNode(), args: [cli, ...args], cwd: projectDir, env };
  }
  const npm = resolveNpm();
  if (!npm) return null;
  return { command: `"${npm}" ${args.join(' ')}`, shell: true, cwd: projectDir, env };
}

/**
 * requirements.txt の内容に対応する済み印。
 * 同梱 Python の site-packages の中に置くので、ランタイムを入れ直したら
 * 印も一緒に消える (= パッケージが無いのに「済み」と誤判定しない)。
 */
function pipMarkerPath(requirementsPath) {
  const hash = crypto.createHash('sha1')
    .update(fs.readFileSync(requirementsPath)).digest('hex').slice(0, 12);
  return path.join(resolveRuntimeDir('python'), 'Lib', 'site-packages',
                   `.codinable-requirements-${hash}`);
}

function pipInstallStep(projectDir, env) {
  const requirements = path.join(projectDir, 'requirements.txt');
  if (!fs.existsSync(requirements)) return null;

  let marker;
  try { marker = pipMarkerPath(requirements); } catch { return null; }
  if (fs.existsSync(marker)) return null;

  return {
    label: 'pip install -r requirements.txt',
    exe:   resolvePython(),
    args:  ['-m', 'pip', 'install', '-r', requirements, '--disable-pip-version-check',
            '--no-warn-script-location'],
    cwd: projectDir, env,
    // 成功したときだけ印を付ける
    onSuccess: () => {
      try {
        fs.mkdirSync(path.dirname(marker), { recursive: true });
        fs.writeFileSync(marker, new Date().toISOString(), 'utf8');
      } catch { /* 印が書けなくても動作には影響しない (次回また入れ直すだけ) */ }
    },
  };
}

// ── 実行仕様の組み立て ─────────────────────────────────────

/**
 * @returns {{ steps: Array, run: object, label: string, isTest?: boolean }}
 */
function buildSpec({ kind, projectDir, relPath, task, uiLang }) {
  const env = getDevEnv(uiLang, { gradle: kind === 'gradle' });

  switch (kind) {
    // ── Gradle (Spring Boot / JUnit を含む) ──
    case 'gradle': {
      const wrapper = ensureGradleWrapper(projectDir);
      if (!wrapper.ok) throw new Error(wrapper.error);

      const safeTask = /^[\w:. -]+$/.test(String(task || '').trim())
        ? String(task).trim() : 'build';
      const isTest = safeTask.split(/\s+/).includes('test');
      const isBootRun = safeTask.split(/\s+/).includes('bootRun');

      let extra = '';
      if (isTest) {
        // テストのカバレッジは JaCoCo を init script で後付けする
        // (雛形の build.gradle に JaCoCo の記述を強いないため)
        const initPath = path.join(projectDir, '.codinable', 'jacoco.init.gradle');
        try {
          fs.mkdirSync(path.dirname(initPath), { recursive: true });
          fs.writeFileSync(initPath, JACOCO_INIT_SCRIPT, 'utf8');
          extra = ` -I "${initPath}"`;
        } catch { /* カバレッジが取れなくてもテスト自体は動かす */ }
      }

      const gradlew = IS_WIN
        ? path.join(projectDir, 'gradlew.bat')
        : path.join(projectDir, 'gradlew');
      return {
        label:   `gradle ${safeTask}`,
        isTest,
        // ポート占有はプレビューを伴うタスクのときだけ掃除する
        freePort: isBootRun ? 8080 : null,
        previewUrl: isBootRun ? 'http://localhost:8080' : null,
        interactive: false,
        run: {
          command: `"${gradlew}" ${safeTask}${extra} --console=plain`,
          shell: true,
          cwd: projectDir,
          env,
        },
      };
    }

    // ── npm スクリプト ──
    case 'npm': {
      const script = String(task || '').trim();
      if (!/^[\w:@./-]{1,64}$/.test(script)) throw new Error(`不正な npm スクリプト名: ${script}`);
      // install / ci は run を付けずに呼ぶ
      const args = ['install', 'ci', 'test', 'start'].includes(script)
        ? [script] : ['run', script];
      const run = npmSpec(args, projectDir, env);
      if (!run) throw new Error('npm が見つかりません (runtime/node が未セットアップです)');

      let scriptBody = '';
      try {
        const pkg = JSON.parse(fs.readFileSync(path.join(projectDir, 'package.json'), 'utf8'));
        scriptBody = String(pkg.scripts?.[script] || '');
      } catch { /* 壊れた package.json は npm 自身のエラーとして表示する */ }
      const isVite = /(^|\s|&&)vite(?:\s|$)/.test(scriptBody) && !/\bvite\s+build\b/.test(scriptBody);
      const vitePort = isVite ? (script === 'preview' ? 4173 : 5173) : null;

      const steps = [];
      if (script !== 'install' && script !== 'ci' && needsNpmInstall(projectDir)) {
        const install = npmInstallStep(projectDir, env);
        if (install) steps.push(install);
      }
      return {
        label: `npm ${args.join(' ')}`,
        steps,
        run,
        freePort: vitePort,
        previewUrl: vitePort ? `http://localhost:${vitePort}` : null,
        // npm スクリプトはサーバーやビルドが中心。stdin 欄を出すと、Vite に
        // 入力を送るための UI に見えてしまうので対話実行とは扱わない。
        interactive: false,
      };
    }

    // ── 単体 Java (Gradle を使わないプロジェクト) ──
    case 'java': {
      const { sources, mainFile } = findJavaMain(projectDir, relPath);
      if (!mainFile) throw new Error('実行できる .java ファイルが見つかりません');
      const classesDir = path.join(projectDir, '.codinable', 'classes');
      fs.mkdirSync(classesDir, { recursive: true });
      const resourceDir = path.join(projectDir, 'src', 'main', 'resources');
      const classpath = [classesDir,
                         fs.existsSync(resourceDir) ? resourceDir : null,
                         resolveHsqldb()].filter(Boolean).join(CP_SEP);
      return {
        label: `java ${path.relative(projectDir, mainFile).replace(/\\/g, '/')}`,
        interactive: true,
        steps: [{
          exe:  resolveJavaTool('javac'),
          args: [...getJavacRuntimeOptions(uiLang), '-encoding', 'UTF-8',
                 '-d', classesDir, '-cp', classpath, ...sources],
          cwd: projectDir, env, label: 'javac',
        }],
        run: {
          exe:  resolveJavaTool('java'),
          args: [...getJavaRuntimeOptions(uiLang), '-cp', classpath, mainClassOf(mainFile)],
          cwd: projectDir, env,
        },
      };
    }

    // ── 開いているファイルをそのまま実行 ──
    case 'file': {
      if (!relPath) throw new Error('実行するファイルが選択されていません');
      const full = path.join(projectDir, relPath.split('/').join(path.sep));
      if (!fs.existsSync(full)) throw new Error(`ファイルが見つかりません: ${relPath}`);
      const ext = path.extname(full).toLowerCase();

      if (ext === '.java') return buildSpec({ kind: 'java', projectDir, relPath, uiLang });

      if (ext === '.py') {
        const pip = pipInstallStep(projectDir, env);
        return {
          label: `python ${relPath}`,
          interactive: true,
          steps: pip ? [pip] : [],
          // -u: 対話入力でプロンプトが先に届くようバッファリングを切る
          run: { exe: resolvePython(), args: ['-X', 'utf8', '-u', full], cwd: projectDir, env },
        };
      }
      if (ext === '.ts' || ext === '.mts' || ext === '.js' || ext === '.mjs' || ext === '.cjs') {
        const steps = [];
        if (needsNpmInstall(projectDir)) {
          const install = npmInstallStep(projectDir, env);
          if (install) steps.push(install);
        }
        // Node.js 24 以降は type stripping で .ts をそのまま実行できる
        return {
          label: `node ${relPath}`,
          interactive: true,
          steps,
          run: { exe: resolveNode(), args: ['--no-warnings', full], cwd: projectDir, env },
        };
      }
      if (ext === '.sh' || ext === '.bash') {
        const bash = resolveBash();
        if (!bash) throw new Error('bash が見つかりません (runtime/bash が未セットアップです)');
        return {
          label: `bash ${relPath}`,
          interactive: true,
          run: { exe: bash, args: ['--noprofile', '--norc', full], cwd: projectDir, env },
        };
      }
      throw new Error(`この拡張子は直接実行できません: ${ext || path.basename(full)}`);
    }

    default:
      throw new Error(`未対応の実行タイプ: ${kind}`);
  }
}

// ── 実行 ───────────────────────────────────────────────────

/**
 * 前処理 (javac / npm install / pip install) を 1 つ動かす。
 * npm install は分単位で掛かることがあるので、出力は溜めずに流し、
 * 動いている間も current に入れて ⏹ で止められるようにする。
 */
function execStep(step, send) {
  return new Promise(resolve => {
    let proc;
    try {
      proc = step.shell
        ? spawn(step.command, [], { cwd: step.cwd, env: step.env, shell: true, windowsHide: true })
        : spawn(step.exe, step.args, { cwd: step.cwd, env: step.env, windowsHide: true });
    } catch (err) {
      send('run-output', `\n❌ ${err.message}\n`);
      resolve(-1);
      return;
    }

    current = { proc, projectDir: step.cwd, kind: 'prepare', task: step.label, isTest: false };

    const onData = chunk => send('run-output', decodeOutput(chunk));
    proc.stdout.on('data', onData);
    proc.stderr.on('data', onData);

    proc.on('error', err => {
      send('run-output', `\n❌ ${err.code === 'ENOENT' ? 'コマンドが見つかりません' : err.message}\n`);
      resolve(-1);
    });
    proc.on('close', code => {
      if (current && current.proc === proc) current = null;
      resolve(code);
    });
  });
}

async function stop() {
  runToken++;
  if (!current) return { ok: true };
  const task = current;
  current = null;
  await killTree(task.proc);
  return { ok: true };
}

/**
 * 実行を開始する。出力は run-output / run-exit / run-url / run-test-results で流す。
 */
async function start(event, { project, kind, relPath, task, uiLang } = {}) {
  const wc   = event.sender;
  const send = (channel, payload) => { if (!wc.isDestroyed()) wc.send(channel, payload); };

  const projectDir = resolveProjectDir(project);
  if (!projectDir || !fs.existsSync(projectDir)) {
    return { ok: false, error: 'プロジェクトが見つかりません' };
  }

  await stop();
  const token = runToken;

  let spec;
  try {
    spec = buildSpec({ kind, projectDir, relPath, task, uiLang });
  } catch (err) {
    send('run-output', `\n❌ ${err.message}\n`);
    send('run-exit', { code: -1, phase: 'prepare' });
    return { ok: false, error: err.message };
  }

  if (spec.freePort) {
    const killed = await killPort(spec.freePort);
    if (killed.length) {
      send('run-output', `⚠️  ポート ${spec.freePort} を使用中のプロセス (PID: ${killed.join(', ')}) を終了しました\n`);
    }
  }

  send('run-output', `▶ ${spec.label}\n\n`);
  if (spec.previewUrl) {
    send('run-output', uiLang === 'en'
      ? '⏳ Preparing the web app. The first run may take a few minutes.\n\n'
      : '⏳ Webアプリを準備しています。初回は依存関係の準備に数分かかることがあります。\n\n');
  }

  // 前処理 (javac / npm install / pip install)。失敗したらそこで終わる
  for (const step of spec.steps || []) {
    if (step.label !== spec.label) send('run-output', `▶ ${step.label}\n`);
    const code = await execStep(step, send);
    if (token !== runToken) return { ok: true };   // 途中で ⏹ か別の実行が始まった
    if (code !== 0) {
      send('run-output', `\n❌ ${step.label} が失敗しました\n`);
      send('run-exit', { code: typeof code === 'number' ? code : 1, phase: step.label || 'compile' });
      return { ok: true };
    }
    if (step.onSuccess) step.onSuccess();
    if (step.label !== spec.label) send('run-output', '\n');
  }

  let proc;
  try {
    proc = spec.run.shell
      ? spawn(spec.run.command, [], { cwd: spec.run.cwd, env: spec.run.env, shell: true, windowsHide: true })
      : spawn(spec.run.exe, spec.run.args, { cwd: spec.run.cwd, env: spec.run.env, windowsHide: true });
  } catch (err) {
    send('run-output', `\n❌ ${err.message}\n`);
    send('run-exit', { code: -1 });
    return { ok: false, error: err.message };
  }

  current = { proc, projectDir, kind, task, isTest: !!spec.isTest };

  let urlSent = false;
  const detectRunUrl = createUrlDetector();
  const announceUrl = url => {
    if (urlSent) return;
    urlSent = true;
    send('run-url', { url });
  };
  const onData = chunk => {
    const text = decodeOutput(chunk);
    send('run-output', text);
    if (!urlSent) {
      const url = detectRunUrl(text);
      if (url) announceUrl(url);
    }
  };
  proc.stdout.on('data', onData);
  proc.stderr.on('data', onData);

  proc.on('error', err => {
    if (current && current.proc === proc) current = null;
    send('run-output', `\n❌ ${err.code === 'ENOENT' ? 'コマンドが見つかりません' : err.message}\n`);
    send('run-exit', { code: -1 });
  });

  proc.on('close', code => {
    const wasTest = current && current.proc === proc ? current.isTest : false;
    if (current && current.proc === proc) current = null;
    if (wasTest) {
      try { send('run-test-results', collectTestRunArtifacts(projectDir)); }
      catch (err) { console.warn('[run] test report parse failed:', err.message); }
    }
    send('run-exit', { code });
  });

  if (spec.previewUrl) {
    const port = Number(new URL(spec.previewUrl).port);
    if (port) waitForPort(port, { token, proc, onReady: () => announceUrl(spec.previewUrl) });
  }

  return { ok: true, interactive: !!spec.interactive, label: spec.label };
}

/** 実行中プロセスの標準入力へ書き込む (Scanner / input() の対話実行用) */
function writeStdin(text) {
  if (!current) return;
  try { current.proc.stdin.write(String(text)); } catch { /* すでに閉じている */ }
}

function isRunning() {
  return !!current;
}

/** 終了時のクリーンアップ */
function disposeAll() {
  if (current) { killTree(current.proc); current = null; }
}

module.exports = {
  start, stop, writeStdin, isRunning, disposeAll,
  ensureGradleWrapper, detectUrl, detectProject,
  // テスト用に公開
  buildSpec, createUrlDetector,
};
