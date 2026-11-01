// ═══════════════════════════════════════════════════════════
//  ワークスペース
//
//  Codinable は「ワークスペースルート直下のディレクトリ = 1 プロジェクト」という
//  素直なモデルをとる。教材アプリのようなセッション / 設問の入れ子は持たない
//
//    <workspaceRoot>/
//    ├── my-first-page/        ← プロジェクト (静的 Web)
//    ├── spring-hello/         ← プロジェクト (Spring Boot)
//    └── django-hello/         ← プロジェクト (Python)
//
//  ファイルはすべて実体として置かれ、エディタもターミナルも言語サーバーも
//  同じディレクトリを直接見る。そのため VS Code や IntelliJ で同じフォルダを
//  開いてもそのままプロジェクトとして成立する
// ═══════════════════════════════════════════════════════════

const fs    = require('fs');
const path  = require('path');
const { shell, dialog } = require('electron');

const { getWorkspaceRoot } = require('./config');
const { walkTree, safeJoin, isTextFile, copyDirSafe } = require('./util');

/** プロジェクトの絶対パス (逸脱は null) */
function resolveProjectDir(projectName) {
  const root = getWorkspaceRoot();
  if (!projectName) return null;
  return safeJoin(root, projectName);
}

/** 実行・プレビューの対象となるディレクトリ (relPath 付きのサブディレクトリも許す) */
function resolveInProject(projectName, relPath = '') {
  const projectDir = resolveProjectDir(projectName);
  if (!projectDir) return null;
  if (!relPath) return projectDir;
  return safeJoin(projectDir, relPath);
}

// ── プロジェクト種別の判定 ─────────────────────────────────

function readTextSafe(file, limit = 200_000) {
  try {
    const stat = fs.statSync(file);
    if (stat.size > limit) return '';
    return fs.readFileSync(file, 'utf8');
  } catch {
    return '';
  }
}

function hasAnyExt(entries, ext) {
  return entries.some(e => !e.dir && e.path.toLowerCase().endsWith(ext));
}

/**
 * プロジェクトの中身から「何が動かせるか」を割り出す
 * ファイル名の規約だけで判断するので、どの Udemy 講座のサンプルでも同じ扱いになる
 */
function detectProject(projectDir) {
  const entries = walkTree(projectDir, { limit: 2500 });
  const names   = new Set(entries.filter(e => !e.dir).map(e => e.path));
  const kinds   = [];

  // ── Gradle / Spring Boot ──
  const gradleFile = ['build.gradle', 'build.gradle.kts'].find(f => names.has(f));
  if (gradleFile) {
    kinds.push('gradle');
    const build = readTextSafe(path.join(projectDir, gradleFile));
    if (/org\.springframework\.boot|spring-boot-starter/.test(build)) kinds.push('spring');
    // war プラグインの Jakarta EE のアプリは、WAR を同梱の Payara Server へデプロイして動かす
    else if (/id\s*\(?\s*['"]war['"]|apply\s+plugin\s*:\s*['"]war['"]/.test(build)) kinds.push('jakarta');
  }

  // ── Node.js / npm ──
  let npmScripts = {};
  if (names.has('package.json')) {
    kinds.push('node');
    try {
      const pkg = JSON.parse(readTextSafe(path.join(projectDir, 'package.json')));
      npmScripts = pkg.scripts || {};
    } catch { /* package.json が壊れていてもプロジェクト自体は開けるようにする */ }
  }

  // ── Python ──
  if (names.has('requirements.txt') || names.has('pyproject.toml') || hasAnyExt(entries, '.py')) {
    kinds.push('python');
  }

  // サブディレクトリに置かれた npm パッケージ (Spring の隣の frontend/ など)
  // その中身は npm スクリプトが入口なので、静的配信やファイル単体の実行対象にしない
  const inNestedPackage = relPath => [...names].some(n =>
    n !== 'package.json' && n.endsWith('/package.json') &&
    relPath.startsWith(n.slice(0, -'package.json'.length)));

  // ── 静的 Web ページ ──
  // index.html があるディレクトリのうち、もっとも浅いものを配信ルートにする
  const htmlRoots = entries
    .filter(e => !e.dir && /(^|\/)index\.html$/i.test(e.path) && !inNestedPackage(e.path))
    .map(e => e.path.replace(/(^|\/)index\.html$/i, '').replace(/^\//, ''))
    .sort((a, b) => a.split('/').length - b.split('/').length || a.length - b.length);
  const staticRoot = htmlRoots.length ? htmlRoots[0] : null;
  if (staticRoot !== null) kinds.push('static');

  // ── 単体 Java ──
  if (hasAnyExt(entries, '.java') && !kinds.includes('gradle')) kinds.push('java');

  // ── SQL ──
  if (hasAnyExt(entries, '.sql')) kinds.push('sql');

  const runnableFiles = collectRunnableFiles(projectDir, entries, {
    hasPackageJson: names.has('package.json'),
    isStatic: staticRoot !== null,
    isGradle: kinds.includes('gradle'),
    inNestedPackage,
  });

  return {
    kinds,
    // '' はプロジェクトルート自身が配信ルートという意味
    staticRoot,
    runnableFiles,
    run: pickRunEntries({
      kinds, npmScripts, staticRoot, names,
      hasGradleTests: [...names].some(n => n.startsWith('src/test/') || n.startsWith('src/backend/src/test/')),
    }),
    hasGradleWrapper: names.has('gradlew') || names.has('gradlew.bat'),
  };
}

/**
 * ツールバーの「▶ アプリ実行」と「🧪 テスト実行」が何を動かすかを決める
 *
 * Gradle のタスクや npm のスクリプトを並べて選ばせると、受講者は何を選べば
 * よいかで迷う。プロジェクト全体として意味があるのは「アプリを動かす」と
 * 「テストする」の 2 つだけなので、それぞれの入口をプロジェクトの中身から 1 つに決める
 * ファイル 1 つを動かすのはエディタのタブの右上の「▶ 実行」(fileRunTarget)
 * 値は実行対象の書式 ('gradle:bootRun' / 'npm:dev' / 'static:' …)
 *
 *   app:   ▶ アプリ実行で動かすもの。SQL やスクリプトのようにファイル単位で動かす
 *          プロジェクトでは null (ボタンは押せない)
 *   test:  🧪 テスト実行で動かすもの。テストが無ければ null (ボタンは押せない)
 */
function pickRunEntries({ kinds, npmScripts, staticRoot, names, hasGradleTests }) {
  if (kinds.includes('gradle')) {
    return {
      app:  kinds.includes('spring') ? 'gradle:bootRun'
          : kinds.includes('jakarta') ? 'gradle:payaraRun' : 'gradle:run',
      test: hasGradleTests ? 'gradle:test' : null,
    };
  }

  const scripts = Object.keys(npmScripts);
  if (scripts.length) {
    // dev サーバー (Vite) があればそれ、無ければ start。build や lint は入口にしない
    const app = ['dev', 'start'].find(s => scripts.includes(s));
    // npm init の既定 ("Error: no test specified") はテストが無いのと同じ
    const test = npmScripts.test && !/no test specified/.test(npmScripts.test) ? 'npm:test' : null;
    return { app: app ? `npm:${app}` : null, test };
  }

  // Django は manage.py が入口 (引数なしで runserver になるようにしてある)
  if (names.has('manage.py')) return { app: 'file:manage.py', test: null };

  // 静的配信は npm スクリプトが無いときだけ。Vite などは index.html が
  // あっても dev サーバー越しでないと動かない
  if (staticRoot !== null) return { app: `static:${staticRoot}`, test: null };

  return { app: null, test: null };
}

/**
 * エディタのタブの右上の「▶ 実行」で、そのファイルだけを動かすときの実行対象
 * 単独で意味のある結果が出ないファイルは null (ボタンを出さない)
 *
 *   .sql / .sh / .py / 単体の .js・.ts / Gradle を使わない .java の main
 *       → collectRunnableFiles が拾ったもの ('sql:…' / 'file:…')
 *   Gradle の src/test/java のテストクラス → そのクラスのテストだけ ('gradle:test --tests …')
 *   Gradle の src/main/java の main        → その main だけ (下の gradleMainTarget)
 *   @SpringBootApplication のクラス        → アプリそのもの (▶ アプリ実行と同じ)
 */
function fileRunTarget(projectDir, relPath) {
  if (!projectDir || !relPath) return null;
  const info = detectProject(projectDir);
  const listed = info.runnableFiles.find(f => f.relPath === relPath);
  if (listed) return `${listed.kind}:${listed.relPath}`;
  return gradleFileRun(projectDir, relPath, info);
}

/**
 * プロジェクトの中の「単独で動かせるファイル」をまとめて返す ({ 相対パス: 実行対象 })
 * ファイルツリーのバッジ (▶) と、演習を選んだときのハイライトに使う
 */
function listFileRuns(projectDir, info = detectProject(projectDir)) {
  const runs = {};
  for (const f of info.runnableFiles) runs[f.relPath] = `${f.kind}:${f.relPath}`;
  if (info.kinds.includes('gradle')) {
    const javaFiles = walkTree(projectDir, { limit: 2500 })
      .filter(e => !e.dir && GRADLE_JAVA_SOURCE.test(e.path))
      .slice(0, 400);
    for (const { path: relPath } of javaFiles) {
      const target = gradleFileRun(projectDir, relPath, info);
      if (target) runs[relPath] = target;
    }
  }
  return runs;
}

// ルートの Gradle プロジェクトのソース (src/main/java・src/test/java)
// バックエンドとフロントエンドを src/backend・src/frontend に分けたモノレポの配置
// (Jakarta EE の書店 leaf-books など。build.gradle の sourceSets が src/backend/src/... を指す) も同じに扱う
const GRADLE_JAVA_SOURCE = /^(?:src\/backend\/)?src\/(main|test)\/java\/.+\.java$/;

/** Gradle のプロジェクトの .java を 1 つだけ動かす実行対象 (動かせなければ null) */
function gradleFileRun(projectDir, relPath, info) {
  if (!info.kinds.includes('gradle')) return null;
  // 入れ子の Gradle プロジェクト (client/ など) はルートのタスクでは動かせないので対象にしない
  const m = GRADLE_JAVA_SOURCE.exec(relPath);
  if (!m) return null;
  const source = readTextSafe(path.join(projectDir, relPath.split('/').join(path.sep)));
  const pkg  = /^\s*package\s+([\w.]+)\s*;/m.exec(source)?.[1];
  const fqcn = (pkg ? `${pkg}.` : '') + path.basename(relPath, '.java');

  if (m[1] === 'test') {
    // @ArchTest は ArchUnit のルール (JUnit 5 のエンジンで流れる)
    const isTest = /@(Test|ParameterizedTest|RepeatedTest|TestFactory|ArchTest)\b/.test(source) &&
                   !/\babstract\s+class\b/.test(source);
    return isTest ? `gradle:test --tests ${fqcn}` : null;
  }
  if (!/\bstatic\s+void\s+main\s*\(/.test(source)) return null;
  if (/@SpringBootApplication\b/.test(source)) return info.run.app;
  return gradleMainTarget(projectDir, fqcn, info);
}

/**
 * Gradle のプロジェクトで main を 1 つだけ動かす実行対象
 *
 * java-db-access の雛形は run が -PmainClass を受け取り、main ごとに DB を初期データへ
 * 戻してから動かすので、それに任せる。受け取らない build.gradle には、runner.js が
 * init script で codinableMain タスク (main のクラスパスで JavaExec) を足す
 */
function gradleMainTarget(projectDir, fqcn, info) {
  const gradleFile = ['build.gradle', 'build.gradle.kts'].map(f => path.join(projectDir, f)).find(f => fs.existsSync(f));
  const build = gradleFile ? readTextSafe(gradleFile) : '';
  // application { mainClass = … } そのもの (SampleLauncher など) は ▶ アプリ実行と同じ
  const appMain = /mainClass\s*=\s*['"]([\w.]+)['"]/.exec(build)?.[1];
  if (appMain === fqcn) return info.run.app;
  return /hasProperty\(\s*['"]mainClass['"]\s*\)/.test(build)
    ? `gradle:run -PmainClass=${fqcn}`
    : `gradle:codinableMain -PcodinableMain=${fqcn}`;
}

/**
 * 「そのファイルを指定すれば動く」ファイルを集める
 *
 * 実行ボタンはエディタで選んでいるファイルに依存させない (選択と実行対象が
 * 連動すると、README を開いただけで実行できなくなる)。代わりに、動かせる
 * ものをプロジェクト全体から拾って実行対象セレクトに並べる
 *
 * @returns {Array<{ relPath: string, kind: 'file'|'sql' }>}
 */
function collectRunnableFiles(projectDir, entries, { hasPackageJson, isStatic, isGradle, inNestedPackage }) {
  const files = entries.filter(e => !e.dir).map(e => e.path);
  const out   = [];

  // Django のプロジェクトは manage.py が入口。urls.py や views.py は単体で動かすと必ず失敗するので並べない
  const djangoRoots = files.filter(f => /(^|\/)manage\.py$/.test(f)).map(f => f.slice(0, -'manage.py'.length));
  const inDjango = relPath => djangoRoots.some(root => relPath.startsWith(root) && !relPath.endsWith('manage.py'));

  // .py / .sh / .sql は単体で動かせる
  for (const relPath of files) {
    const lower = relPath.toLowerCase();
    if (lower.endsWith('.py') && inDjango(relPath)) continue;
    if (lower.endsWith('.py') || lower.endsWith('.sh') || lower.endsWith('.bash')) {
      out.push({ relPath, kind: 'file' });
    } else if (lower.endsWith('.sql') && lower !== 'reset.sql' && !isGradle) {
      // ルートの reset.sql は SQL を流す前に毎回自動で流す初期化用なので、実行対象に並べない
      // Gradle のプロジェクトの .sql はアプリが起動時にインメモリ DB へ流すもので、
      // 共有の HSQLDB へ単独で流しても意味がないので対象にしない
      out.push({ relPath, kind: 'sql' });
    }
  }

  // JavaScript / TypeScript は「単体のスクリプト」のときだけ並べる
  // package.json があれば npm スクリプトが入口であり、index.html があれば
  // ブラウザで読まれる側なので、node で直接動かすと必ず失敗する
  if (!hasPackageJson && !isStatic) {
    for (const relPath of files) {
      if (/\.(js|mjs|cjs|ts|mts)$/i.test(relPath) && !inNestedPackage(relPath)) {
        out.push({ relPath, kind: 'file' });
      }
    }
  }

  // Gradle を使わない Java は main を持つファイルだけ並べる
  // 数が多いプロジェクトで全部読むのは無駄なので上限を置き、
  // 超えるときは kinds の 'java' による自動検出に任せる
  if (!isGradle) {
    const javaFiles = files.filter(f => f.toLowerCase().endsWith('.java'));
    if (javaFiles.length && javaFiles.length <= 60) {
      for (const relPath of javaFiles) {
        const source = readTextSafe(path.join(projectDir, relPath.split('/').join(path.sep)));
        if (/public\s+static\s+void\s+main\s*\(/.test(source)) out.push({ relPath, kind: 'file' });
      }
    }
  }

  // 浅いものを先に、同じ深さなら名前順 (01_… のような連番が並ぶ)
  out.sort((a, b) =>
    a.relPath.split('/').length - b.relPath.split('/').length ||
    a.relPath.localeCompare(b.relPath));
  return out.slice(0, 30);
}

/** ワークスペース直下のプロジェクト一覧 */
function listProjects() {
  const root = getWorkspaceRoot();
  try { fs.mkdirSync(root, { recursive: true }); } catch { /* 権限が無ければ下で空配列になる */ }

  const projects = [];
  let dirents;
  try { dirents = fs.readdirSync(root, { withFileTypes: true }); } catch { return projects; }

  for (const entry of dirents) {
    if (!entry.isDirectory() || entry.name.startsWith('.')) continue;
    const dir = path.join(root, entry.name);
    let mtime = null;
    try { mtime = fs.statSync(dir).mtime.toISOString(); } catch {}
    const meta = readProjectMeta(dir);
    projects.push({
      name:  entry.name,
      path:  dir,
      mtime,
      courseId: meta.courseId || null,
      template: meta.template || null,
      ...detectProject(dir),
    });
  }
  projects.sort((a, b) => new Date(b.mtime || 0) - new Date(a.mtime || 0));
  return projects;
}

// ── プロジェクトのメタ情報 (.codinable/project.json) ───────

function metaPath(projectDir) {
  return path.join(projectDir, '.codinable', 'project.json');
}

function readProjectMeta(projectDir) {
  try { return JSON.parse(fs.readFileSync(metaPath(projectDir), 'utf8')); } catch { return {}; }
}

function writeProjectMeta(projectDir, meta) {
  try {
    fs.mkdirSync(path.dirname(metaPath(projectDir)), { recursive: true });
    fs.writeFileSync(metaPath(projectDir), JSON.stringify(meta, null, 2), 'utf8');
  } catch (err) {
    console.warn('[workspace] project meta write failed:', err.message);
  }
}

// ── 作成・削除 ─────────────────────────────────────────────

const NAME_RE = /^[A-Za-z0-9][A-Za-z0-9._-]{0,63}$/;

/**
 * プロジェクトを作る
 * templateDir が与えられればそれを丸ごとコピーし、無ければ空ディレクトリを作る
 */
function createProject({ name, templateDir = null, courseId = null, template = null }) {
  const safeName = String(name || '').trim();
  if (!NAME_RE.test(safeName)) {
    return { ok: false, error: 'invalid-name' };
  }
  const dir = resolveProjectDir(safeName);
  if (!dir) return { ok: false, error: 'invalid-name' };
  if (fs.existsSync(dir)) return { ok: false, error: 'already-exists' };

  try {
    fs.mkdirSync(dir, { recursive: true });
    if (templateDir && fs.existsSync(templateDir)) copyDirSafe(templateDir, dir);
    writeProjectMeta(dir, {
      name: safeName,
      courseId,
      template,
      createdAt: new Date().toISOString(),
    });
  } catch (err) {
    return { ok: false, error: err.message };
  }
  return { ok: true, name: safeName, path: dir, ...detectProject(dir) };
}

/**
 * 演習を配布時の状態に戻す (雛形のファイルを上書きする)
 *
 * 上書きだけで、受講者が自分で足したファイルは消さない。消す方が「初期状態」に
 * 忠実だが、試したコードを黙って捨てることになるため、雛形にあるものだけを戻す
 */
/**
 * 雛形にあって作業用プロジェクトに無いファイルを足す (上書きはしない)
 *
 * プロジェクトは作った時点の雛形の写しなので、あとから雛形に足したファイル
 * (演習の概要の README.md など) は、以前に作ったプロジェクトには無い
 * 演習を開くときに「最初に見せるファイル」だけをこれで補う
 */
function addMissingFromTemplate({ name, templateDir, files }) {
  const dir = resolveProjectDir(name);
  if (!dir || !fs.existsSync(dir)) return { ok: false, error: 'not-found' };
  if (!templateDir || !fs.existsSync(templateDir)) return { ok: false, error: 'no-template' };

  const added = [];
  for (const rel of files || []) {
    const to   = safeJoin(dir, rel);
    const from = safeJoin(templateDir, rel);
    if (!to || !from || fs.existsSync(to) || !fs.existsSync(from)) continue;
    try {
      fs.mkdirSync(path.dirname(to), { recursive: true });
      fs.copyFileSync(from, to);
      added.push(rel);
    } catch (err) {
      console.warn(`[workspace] could not add ${rel}:`, err.message);
    }
  }
  return { ok: true, added };
}

// 初期化でも消さないもの
//   .codinable    どの講座のどの演習かの記録。消すと演習との対応が切れる
//   node_modules / .gradle   依存のキャッシュ。雛形からは作れず、消すと次の実行で
//                            取り直しになるだけで結果は変わらない
const RESET_KEEP = new Set(['.codinable', 'node_modules', '.gradle']);

/**
 * 演習を配布時の状態に戻す
 * 自分で足したファイルも含めて消してから雛形を書き戻す (完全な初期化)
 * 消せなかったもの (別のプロセスが掴んでいる等) は failed に名前を返す
 */
function resetToTemplate({ name, templateDir }) {
  const dir = resolveProjectDir(name);
  if (!dir || !fs.existsSync(dir)) return { ok: false, error: 'not-found' };
  if (!templateDir || !fs.existsSync(templateDir)) return { ok: false, error: 'no-template' };

  const failed = [];
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    if (RESET_KEEP.has(entry.name)) continue;
    try {
      fs.rmSync(path.join(dir, entry.name), { recursive: true, force: true, maxRetries: 3 });
    } catch {
      failed.push(entry.name);
    }
  }
  const written = copyDirSafe(templateDir, dir, { overwrite: true });
  return { ok: true, written, failed };
}

/**
 * チャットに渡すため、プロジェクトのテキストファイルを集める
 *
 * 受講者に添付の操作をさせない代わりに、開いているプロジェクトの中身をそのまま渡す
 * ただしモデルに渡せる量には限りがあるので、次の順で落とす
 *   1. 生成物・巨大ファイル・機械が作るもの (SKIP_DIRS / EXCLUDE_NAMES / サイズ上限)
 *   2. 入りきらない分 (件数・合計バイトの上限)
 * 落とした件数を返すので、画面には「何件渡したか」を出せる
 */
const CONTEXT_EXCLUDE = [
  /^package-lock\.json$/, /^yarn\.lock$/, /^pnpm-lock\.yaml$/,
  /^gradlew(\.bat)?$/, /^gradle\/wrapper\//, /\.min\.(js|css)$/,
];

function collectContextFiles(name, { maxFiles = 60, maxBytes = 300_000, maxFileBytes = 60_000 } = {}) {
  const dir = resolveProjectDir(name);
  if (!dir || !fs.existsSync(dir)) return { ok: false, files: [], skipped: 0 };

  const candidates = walkTree(dir)
    .filter(e => !e.dir && e.text)
    .filter(e => !CONTEXT_EXCLUDE.some(re => re.test(e.path)));

  const files = [];
  let bytes = 0;
  let skipped = 0;
  for (const entry of candidates) {
    const full = path.join(dir, entry.path);
    let stat;
    try { stat = fs.statSync(full); } catch { skipped++; continue; }
    if (stat.size > maxFileBytes || files.length >= maxFiles || bytes + stat.size > maxBytes) {
      skipped++;
      continue;
    }
    try {
      files.push({ path: entry.path, content: fs.readFileSync(full, 'utf8') });
      bytes += stat.size;
    } catch { skipped++; }
  }
  return { ok: true, files, skipped };
}

// ── ファイル操作 ───────────────────────────────────────────

function listTree(name) {
  const dir = resolveProjectDir(name);
  if (!dir || !fs.existsSync(dir)) return { ok: false, entries: [] };
  return { ok: true, root: dir, entries: walkTree(dir) };
}

function readFile(name, relPath) {
  const full = resolveInProject(name, relPath);
  if (!full || !fs.existsSync(full)) return { ok: false, error: 'not-found' };
  if (!isTextFile(relPath)) return { ok: false, error: 'binary' };
  try {
    const stat = fs.statSync(full);
    if (stat.size > 2_000_000) return { ok: false, error: 'too-large' };
    return { ok: true, content: fs.readFileSync(full, 'utf8'), path: full };
  } catch (err) {
    return { ok: false, error: err.message };
  }
}

function writeFile(name, relPath, content) {
  const full = resolveInProject(name, relPath);
  if (!full) return { ok: false, error: 'invalid-path' };
  try {
    fs.mkdirSync(path.dirname(full), { recursive: true });
    fs.writeFileSync(full, String(content ?? ''), 'utf8');
    return { ok: true };
  } catch (err) {
    return { ok: false, error: err.message };
  }
}

function createEntry(name, relPath, kind) {
  const full = resolveInProject(name, relPath);
  if (!full) return { ok: false, error: 'invalid-path' };
  if (fs.existsSync(full)) return { ok: false, error: 'already-exists' };
  try {
    if (kind === 'dir') {
      fs.mkdirSync(full, { recursive: true });
    } else {
      fs.mkdirSync(path.dirname(full), { recursive: true });
      fs.writeFileSync(full, '', 'utf8');
    }
    return { ok: true };
  } catch (err) {
    return { ok: false, error: err.message };
  }
}

function deleteEntry(name, relPath) {
  const full = resolveInProject(name, relPath);
  if (!full || !fs.existsSync(full)) return { ok: false, error: 'not-found' };
  try {
    fs.rmSync(full, { recursive: true, force: true });
    return { ok: true };
  } catch (err) {
    return { ok: false, error: err.message };
  }
}

function renameEntry(name, relPath, newRelPath) {
  const from = resolveInProject(name, relPath);
  const to   = resolveInProject(name, newRelPath);
  if (!from || !to) return { ok: false, error: 'invalid-path' };
  if (!fs.existsSync(from)) return { ok: false, error: 'not-found' };
  if (fs.existsSync(to)) return { ok: false, error: 'already-exists' };
  try {
    fs.mkdirSync(path.dirname(to), { recursive: true });
    fs.renameSync(from, to);
    return { ok: true };
  } catch (err) {
    return { ok: false, error: err.message };
  }
}

/** エクスプローラ / Finder で開く */
function revealPath(name, relPath = '') {
  const full = relPath ? resolveInProject(name, relPath) : resolveProjectDir(name);
  if (!full || !fs.existsSync(full)) return { ok: false, error: 'not-found' };
  if (fs.statSync(full).isDirectory()) shell.openPath(full);
  else shell.showItemInFolder(full);
  return { ok: true };
}

async function pickDirectory() {
  const result = await dialog.showOpenDialog({ properties: ['openDirectory', 'createDirectory'] });
  if (result.canceled || !result.filePaths.length) return { ok: false };
  return { ok: true, path: result.filePaths[0] };
}

module.exports = {
  resolveProjectDir,
  resolveInProject,
  detectProject,
  fileRunTarget,
  listFileRuns,
  listProjects,
  readProjectMeta,
  writeProjectMeta,
  createProject,
  resetToTemplate,
  addMissingFromTemplate,
  collectContextFiles,
  listTree,
  readFile,
  writeFile,
  createEntry,
  deleteEntry,
  renameEntry,
  revealPath,
  pickDirectory,
};
