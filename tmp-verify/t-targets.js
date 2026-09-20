// detectProject の runnableFiles と、renderer が並べる実行対象を雛形ごとに確かめる。
// 検証が終わったら tmp-verify ごと消す。
const path = require('path');
const fs = require('fs');
const Module = require('module');

// workspace.js は electron の app を使うので、require を差し替えて逃がす
const origResolve = Module._resolveFilename;
const origLoad = Module._load;
Module._load = function (request, parent, isMain) {
  if (request === 'electron') {
    return { app: { getPath: () => process.env.TEMP, isPackaged: false }, shell: {}, dialog: {} };
  }
  return origLoad.apply(this, arguments);
};

const ws = require(path.resolve('src/main/workspace.js'));

// renderer の updateRunTargets と同じ順序で並べる
function runTargets(info) {
  const out = [];
  for (const task of info.gradleTasks || []) out.push(`gradle:${task}`);
  for (const s of info.npmScripts || []) out.push(`npm:${s}`);
  if (info.staticRoot !== null && info.staticRoot !== undefined &&
      !(info.npmScripts || []).length) out.push(`static:${info.staticRoot}`);
  for (const e of info.runnableFiles || []) out.push(`${e.kind}:${e.relPath}`);
  const kinds = info.kinds || [];
  if (kinds.includes('java') && !kinds.includes('gradle') &&
      !(info.runnableFiles || []).some(e => e.relPath.endsWith('.java'))) out.push('java');
  return out;
}

const yaml = require(path.resolve('node_modules/js-yaml'));
const courseDir = path.resolve('../courses/webapp-archi-overview');
const meta = yaml.load(fs.readFileSync(path.join(courseDir, 'course.yaml'), 'utf8'));
const declared = new Map(meta.exercises.map(e => [e.dir || e.id, e.run]));

let bad = 0;
for (const dir of fs.readdirSync(path.join(courseDir, 'templates'))) {
  const full = path.join(courseDir, 'templates', dir);
  const info = ws.detectProject(full);
  const targets = runTargets(info);
  const want = declared.get(dir);

  const ok = !want || targets.includes(want);
  if (!ok) bad++;
  console.log(`${ok ? 'OK ' : 'NG '} ${dir}`);
  console.log(`      既定 = ${targets[0] || '(なし)'}   演習の run = ${want || '-'}`);
  console.log(`      候補 = ${targets.join(' , ') || '(なし)'}`);
}
console.log(bad ? `\n=> ${bad} 件、演習の run が候補に無い` : '\n=> 全雛形で演習の run が候補に含まれる');
Module._load = origLoad;
Module._resolveFilename = origResolve;
process.exit(bad ? 1 : 0);
