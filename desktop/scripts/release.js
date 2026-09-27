// ═══════════════════════════════════════════════════════════
//  配信 (GitHub Releases へ上げる)
//
//    npm run release:app       → dist-installer/ のインストーラと latest.yml を
//                                タグ v<version> のリリースとして上げ、「最新」にする
//    npm run release:courses   → 講座の配信物を作り直し (build:courses)、
//                                タグ courses のリリースへ上書きで上げる
//
//  受講者のアプリはここを見る (src/app-config.js の UPDATES / getUpdateUrls)
//    アプリ本体: releases/latest/download/latest.yml
//    講座:       releases/download/courses/index.json
//
//  courses のリリースは「最新」にしない。最新になると releases/latest が
//  講座のリリースを指してしまい、アプリ本体の更新が見つからなくなる
//
//  gh (GitHub CLI) でログインしてある必要がある (gh auth login)
// ═══════════════════════════════════════════════════════════

const fs   = require('fs');
const path = require('path');
const { spawnSync } = require('child_process');

const { UPDATES } = require('../src/app-config');
const pkg = require('../package.json');

const ROOT = path.resolve(__dirname, '..');
const REPO = `${UPDATES.github.owner}/${UPDATES.github.repo}`;

function gh(args, { allowFail = false } = {}) {
  const res = spawnSync('gh', args, { cwd: ROOT, stdio: allowFail ? 'pipe' : 'inherit', encoding: 'utf8' });
  if (res.error) throw new Error(`gh を起動できません: ${res.error.message}`);
  if (res.status !== 0 && !allowFail) throw new Error(`gh ${args.join(' ')} が失敗しました (${res.status})`);
  return res.status === 0;
}

function releaseApp() {
  const version = pkg.version;
  const dir = path.join(ROOT, 'dist-installer');
  const setup = `Codinable-setup-${version}.exe`;
  const files = [setup, `${setup}.blockmap`, 'latest.yml'].map(f => path.join(dir, f));
  const missing = files.filter(f => !fs.existsSync(f));
  if (missing.length) {
    throw new Error(`見つかりません: ${missing.map(f => path.basename(f)).join(', ')}（先に npm run build）`);
  }
  // latest.yml がこの版を指していること (古い build の残りを上げない)
  const latest = fs.readFileSync(path.join(dir, 'latest.yml'), 'utf8');
  if (!new RegExp(`^version: ${version.replace(/\./g, '\\.')}$`, 'm').test(latest)) {
    throw new Error(`latest.yml が v${version} を指していません（npm run build をやり直す）`);
  }

  const tag = `v${version}`;
  if (gh(['release', 'view', tag, '--repo', REPO], { allowFail: true })) {
    throw new Error(`${tag} はもうあります。package.json の version を上げてから build してください`);
  }
  gh(['release', 'create', tag, ...files, '--repo', REPO, '--latest',
      '--title', `Codinable ${tag}`,
      '--notes', 'Codinable のインストーラ（全講座入り）。インストール済みのアプリは起動時に更新を知らせます。']);
  console.log(`\n${tag} を公開しました。受講者のアプリは次の起動時に更新を知らせます。`);
}

function releaseCourses() {
  const build = spawnSync(process.execPath, [path.join(__dirname, 'build-courses.js')], { cwd: ROOT, stdio: 'inherit' });
  if (build.status !== 0) throw new Error('build:courses が失敗しました');

  const dir = path.join(ROOT, 'dist-updates', 'courses');
  const tag = UPDATES.coursesTag;
  if (!gh(['release', 'view', tag, '--repo', REPO], { allowFail: true })) {
    gh(['release', 'create', tag, '--repo', REPO, '--latest=false',
        '--title', 'Codinable courses',
        '--notes', 'Codinable の講座の配信物。講座を新しく始めるときにアプリが確認します（取り組み中の講座は変わりません）。']);
  }
  // 先に中身、最後に index.json を上げる (index が指すファイルが無い瞬間を作らない)
  const packs = fs.readdirSync(dir).filter(f => f.endsWith('.codpack')).map(f => path.join(dir, f));
  gh(['release', 'upload', tag, ...packs, '--repo', REPO, '--clobber']);
  gh(['release', 'upload', tag, path.join(dir, 'index.json'), '--repo', REPO, '--clobber']);
  console.log(`\n講座を ${REPO} の ${tag} リリースへ上げました。次に講座を「新規」で始める人から新しい版になります。`);
}

try {
  const what = process.argv[2];
  if (what === 'app') releaseApp();
  else if (what === 'courses') releaseCourses();
  else throw new Error('使い方: node scripts/release.js app | courses');
} catch (err) {
  console.error(`\n${err.message}`);
  process.exit(1);
}
