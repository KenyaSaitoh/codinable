// ═══════════════════════════════════════════════════════════
//  講座の配信物を作る (アプリ本体とは別に講座だけを更新するため)
//
//    npm run build:courses            → dist-updates/courses/ に全講座
//    npm run build:courses -- <id>,…  → 指定した講座だけ (index.json は全講座ぶん作り直す)
//
//  できるもの
//    dist-updates/courses/index.json            … 講座ごとの最新版 (id / version / file / sha256)
//    dist-updates/courses/<id>-<version>.codpack … 講座フォルダの中身を gzip した JSON
//
//  これを配信先の <baseUrl>/courses/ にそのまま置く (app-config.js の UPDATES)
//  受講者のアプリは「講座を新しく始めるとき」に index.json を見て、
//  手元より新しい version があれば codpack を取り込む (src/main/courses.js)
//
//  講座を直したら course.yaml の version を上げること。上げないと配信されない
// ═══════════════════════════════════════════════════════════

const fs     = require('fs');
const path   = require('path');
const zlib   = require('zlib');
const crypto = require('crypto');
const yaml   = require('js-yaml');

const COURSES_DIR = path.resolve(__dirname, '../../courses');
const OUT_DIR     = path.resolve(__dirname, '../dist-updates/courses');
// electron-builder.js の courses の filter と同じ。生成物は配らない
const SKIP_DIRS   = new Set(['.gradle', 'build', 'bin', 'node_modules']);

function listFiles(dir, base = dir) {
  const files = [];
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const full = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      if (!SKIP_DIRS.has(entry.name)) files.push(...listFiles(full, base));
    } else if (entry.isFile()) {
      files.push(path.relative(base, full).split(path.sep).join('/'));
    }
  }
  return files.sort();
}

function buildPack(courseDir) {
  const meta = yaml.load(fs.readFileSync(path.join(courseDir, 'course.yaml'), 'utf8')) || {};
  const id = String(meta.id || path.basename(courseDir));
  const version = meta.version ? String(meta.version) : null;
  if (!version) throw new Error(`${id}: course.yaml に version がありません`);

  const files = listFiles(courseDir).map(rel => ({
    path: rel,
    data: fs.readFileSync(path.join(courseDir, ...rel.split('/'))).toString('base64'),
  }));
  const buffer = zlib.gzipSync(Buffer.from(JSON.stringify({ format: 1, id, version, files })), { level: 9 });
  const file = `${id}-${version}.codpack`;
  fs.writeFileSync(path.join(OUT_DIR, file), buffer);
  return {
    id, version, file,
    sha256: crypto.createHash('sha256').update(buffer).digest('hex'),
    size: buffer.length,
    files: files.length,
  };
}

function main() {
  const only = new Set(String(process.argv[2] || '').split(',').map(s => s.trim()).filter(Boolean));
  fs.mkdirSync(OUT_DIR, { recursive: true });

  const indexPath = path.join(OUT_DIR, 'index.json');
  let previous = [];
  try { previous = JSON.parse(fs.readFileSync(indexPath, 'utf8')).courses || []; } catch { /* 初回 */ }

  const entries = [];
  for (const entry of fs.readdirSync(COURSES_DIR, { withFileTypes: true })) {
    const courseDir = path.join(COURSES_DIR, entry.name);
    if (!entry.isDirectory() || !fs.existsSync(path.join(courseDir, 'course.yaml'))) continue;
    const meta = yaml.load(fs.readFileSync(path.join(courseDir, 'course.yaml'), 'utf8')) || {};
    const id = String(meta.id || entry.name);
    const kept = previous.find(p => p.id === id);
    if (only.size && !only.has(id) && kept) { entries.push(kept); continue; }

    const pack = buildPack(courseDir);
    const { files, ...indexEntry } = pack;
    entries.push(indexEntry);
    console.log(`  ${id} v${pack.version}  ${files} files  ${(pack.size / 1024).toFixed(0)} KB  → ${pack.file}`);
  }

  entries.sort((a, b) => a.id.localeCompare(b.id));
  fs.writeFileSync(indexPath, JSON.stringify({
    format: 1, generatedAt: new Date().toISOString(), courses: entries,
  }, null, 2) + '\n', 'utf8');
  console.log(`\n${path.relative(process.cwd(), OUT_DIR)} に ${entries.length} 講座ぶんを書き出しました。`);
  console.log('配信先の <baseUrl>/courses/ に index.json と .codpack を置いてください。');
}

main();
