// ═══════════════════════════════════════════════════════════
//  開発用の起動 (npm start)
//
//  受講者の手元には「講座ごとのインストーラで入れたコース」が 1 つ以上並ぶ
//  その状態を手元で再現できるよう、起動時に読み込むコースを選ぶ
//
//    npm start                      → 一覧から選ぶ (Enter だけなら全コース = 統合版)
//    npm start -- all               → 全コース
//    npm start -- spring-advanced   → そのコースだけ
//    npm start -- 2,3               → 一覧の番号で指定 (id と混ぜてもよい)
//
//  選んだ結果は環境変数 CODINABLE_COURSES で Electron に渡し、
//  src/main/courses.js の getDevCourseFilter が同梱・共有・個人の全置き場に効かせる
//  (パッケージ後のアプリは、この変数を見ない)
// ═══════════════════════════════════════════════════════════

const fs       = require('fs');
const path     = require('path');
const readline = require('readline');
const { spawn } = require('child_process');
const yaml     = require('js-yaml');
const electron = require('electron');

const COURSES_DIR = path.resolve(__dirname, '..', '..', 'courses');

/** 同梱のコース (リポジトリの courses/) を order 順に並べる */
function listCourses() {
  let entries = [];
  try { entries = fs.readdirSync(COURSES_DIR, { withFileTypes: true }); } catch { return []; }
  const courses = [];
  for (const entry of entries) {
    if (!entry.isDirectory()) continue;
    try {
      const meta = yaml.load(fs.readFileSync(path.join(COURSES_DIR, entry.name, 'course.yaml'), 'utf8')) || {};
      courses.push({
        id:    String(meta.id || entry.name),
        name:  (meta.names && (meta.names.ja || meta.names.en)) || entry.name,
        order: Number.isFinite(meta.order) ? meta.order : 999,
        count: Array.isArray(meta.exercises) ? meta.exercises.length : 0,
      });
    } catch { /* course.yaml が無い・壊れているものは並べない (アプリ側でも読まれない) */ }
  }
  return courses.sort((a, b) => a.order - b.order || a.id.localeCompare(b.id));
}

/**
 * 「all」「id」「一覧の番号」をカンマ・空白区切りで受け取り、コース id の配列にする
 * @returns {string[]|null} null は全コース
 */
function parseSelection(input, courses) {
  const tokens = String(input).split(/[\s,]+/).map(s => s.trim()).filter(Boolean);
  if (!tokens.length || tokens.includes('all')) return null;
  const ids = [];
  for (const token of tokens) {
    const byNumber = /^\d+$/.test(token) ? courses[Number(token) - 1] : null;
    const id = byNumber ? byNumber.id : token;
    if (!byNumber && !courses.some(c => c.id === id)) {
      // 共有・個人フォルダにだけあるコースも指定できるようにして、止めはしない
      console.warn(`[start] courses/ に無い id です（共有・個人フォルダにあれば読み込まれます）: ${id}`);
    }
    if (!ids.includes(id)) ids.push(id);
  }
  return ids;
}

function ask(question) {
  const rl = readline.createInterface({ input: process.stdin, output: process.stdout });
  return new Promise(resolve => rl.question(question, answer => { rl.close(); resolve(answer); }));
}

async function chooseCourses(courses) {
  const args = process.argv.slice(2);
  if (args.length) return parseSelection(args.join(','), courses);
  // 環境変数で先に決めてあれば (テストスクリプトなど) それに従う
  if (process.env.CODINABLE_COURSES) return parseSelection(process.env.CODINABLE_COURSES, courses);
  // 端末でなければ (CI やパイプ) 聞かずに全コース
  if (!process.stdin.isTTY || courses.length <= 1) return null;

  console.log('\n起動時に読み込むコースを選んでください（受講者がインストール済みの講座の組み合わせを再現します）\n');
  courses.forEach((c, i) =>
    console.log(`  ${String(i + 1).padStart(2)}. ${c.name}  (${c.id}, 演習 ${c.count} 件)`));
  console.log('\n  番号か id をカンマ・空白区切りで入力。Enter だけなら全コース（統合版）');
  return parseSelection(await ask('> '), courses);
}

async function main() {
  const courses  = listCourses();
  const selected = await chooseCourses(courses);

  const env = { ...process.env };
  if (selected) {
    env.CODINABLE_COURSES = selected.join(',');
    console.log(`[start] 読み込むコース: ${selected.join(', ')}`);
  } else {
    delete env.CODINABLE_COURSES;
    console.log(`[start] 読み込むコース: 全コース（${courses.length} 件）`);
  }

  const child = spawn(electron, ['.'], {
    cwd: path.resolve(__dirname, '..'),
    env,
    stdio: 'inherit',
    windowsHide: false,
  });
  child.on('exit', code => process.exit(code ?? 0));
  // Ctrl+C は Electron 側にも届くので、こちらは終了を待つだけにする
  process.on('SIGINT', () => {});
}

main().catch(err => {
  console.error(err);
  process.exit(1);
});
