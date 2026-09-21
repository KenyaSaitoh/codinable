// テスト結果 (JUnit XML) / カバレッジ (JaCoCo XML) の解析ユーティリティ
// main.js から require され、springboot-run (test タスク) の完了時に使われる
'use strict';

const fs = require('fs');
const path = require('path');

// ── テスト結果 (JUnit XML) / カバレッジ (JaCoCo XML) の解析 ─────────────────
// Gradle の test タスクが生成するレポートを解析し、Eclipse の JUnit ビューのように
// テストメソッド単位の成否・失敗メッセージ・カバレッジをレンダラーに渡す

function decodeXmlEntities(s) {
  return String(s)
    .replace(/&#x([0-9a-fA-F]+);/g, (_, h) => String.fromCodePoint(parseInt(h, 16)))
    .replace(/&#(\d+);/g, (_, d) => String.fromCodePoint(parseInt(d, 10)))
    .replace(/&lt;/g, '<').replace(/&gt;/g, '>')
    .replace(/&quot;/g, '"').replace(/&apos;/g, "'")
    .replace(/&amp;/g, '&');
}

function parseXmlAttrs(attrText) {
  const attrs = {};
  const re = /([\w:-]+)\s*=\s*"([^"]*)"/g;
  let m;
  while ((m = re.exec(attrText)) !== null) attrs[m[1]] = decodeXmlEntities(m[2]);
  return attrs;
}

// build/test-results/test/TEST-*.xml → [{classname, name, time, status, message, type, detail}]
function parseJUnitXmlReports(resultsDir) {
  const tests = [];
  if (!fs.existsSync(resultsDir)) return tests;
  for (const fname of fs.readdirSync(resultsDir)) {
    if (!fname.startsWith('TEST-') || !fname.endsWith('.xml')) continue;
    let xml;
    try { xml = fs.readFileSync(path.join(resultsDir, fname), 'utf8'); } catch { continue; }
    const tcRe = /<testcase\b([^>]*?)(\/>|>([\s\S]*?)<\/testcase>)/g;
    let m;
    while ((m = tcRe.exec(xml)) !== null) {
      const attrs = parseXmlAttrs(m[1]);
      const body  = m[3] || '';
      const t = {
        classname: attrs.classname || '',
        name:      attrs.name || '',
        time:      parseFloat(attrs.time || '0'),
        status:    'passed',
        message:   '',
        type:      '',
        detail:    '',
      };
      const fm = /<(failure|error)\b([^>]*?)(\/>|>([\s\S]*?)<\/\1>)/.exec(body);
      if (fm) {
        const fa = parseXmlAttrs(fm[2]);
        t.status  = 'failed';
        t.message = fa.message || '';
        t.type    = fa.type || '';
        t.detail  = decodeXmlEntities((fm[4] || '').replace(/<!\[CDATA\[([\s\S]*?)\]\]>/g, '$1')).trim();
      } else if (/<skipped\b/.test(body)) {
        t.status = 'skipped';
      }
      tests.push(t);
    }
  }
  return tests;
}

// build/reports/jacoco/test/jacocoTestReport.xml →
//   { files: { 'pro/kensait/.../FeeService.java': { lines: {12:{mi,ci,mb,cb}}, missed, covered } },
//     missed, covered }   (LINE カウンタベース)
function parseJacocoXml(xmlPath) {
  if (!fs.existsSync(xmlPath)) return null;
  let xml;
  try { xml = fs.readFileSync(xmlPath, 'utf8'); } catch { return null; }
  const cov = { files: {}, missed: 0, covered: 0 };
  const pkgRe = /<package\b([^>]*?)>([\s\S]*?)<\/package>/g;
  let pm;
  while ((pm = pkgRe.exec(xml)) !== null) {
    const pkgName = (parseXmlAttrs(pm[1]).name || '');
    const pkgBody = pm[2];
    const sfRe = /<sourcefile\b([^>]*?)>([\s\S]*?)<\/sourcefile>/g;
    let sm;
    while ((sm = sfRe.exec(pkgBody)) !== null) {
      const sfName = parseXmlAttrs(sm[1]).name || '';
      const key    = pkgName ? `${pkgName}/${sfName}` : sfName;
      const entry  = { lines: {}, missed: 0, covered: 0 };
      const lineRe = /<line\b([^>]*?)\/>/g;
      let lm;
      while ((lm = lineRe.exec(sm[2])) !== null) {
        const a = parseXmlAttrs(lm[1]);
        entry.lines[parseInt(a.nr, 10)] = {
          mi: parseInt(a.mi || '0', 10), ci: parseInt(a.ci || '0', 10),
          mb: parseInt(a.mb || '0', 10), cb: parseInt(a.cb || '0', 10),
        };
      }
      const cm = /<counter\s+type="LINE"\s+missed="(\d+)"\s+covered="(\d+)"/.exec(sm[2]);
      if (cm) { entry.missed = parseInt(cm[1], 10); entry.covered = parseInt(cm[2], 10); }
      if (Object.keys(entry.lines).length > 0) {
        cov.files[key] = entry;
        cov.missed  += entry.missed;
        cov.covered += entry.covered;
      }
    }
  }
  return Object.keys(cov.files).length > 0 ? cov : null;
}

// JaCoCo を全プロジェクトに自動適用する init スクリプト (テスト実行時に -I で注入)
const JACOCO_INIT_SCRIPT = `// codinable: JaCoCo カバレッジ自動計測 (アプリが自動注入)
allprojects {
    plugins.withId('java') {
        apply plugin: 'jacoco'
        jacoco { toolVersion = '0.8.14' }
        tasks.withType(Test).configureEach {
            finalizedBy 'jacocoTestReport'
            // 講義にはテストではなく main() のデモを見せるものがある。Gradle 9 は
            // 「テスト 0 件」を設定ミスとみなしてビルドを失敗させるが、学習者には
            // 壊れて見えるだけなので、0 件は静かに成功として扱う
            if (it.hasProperty('failOnNoDiscoveredTests')) {
                it.failOnNoDiscoveredTests = false
            }
            // JDK 24 以降は動的エージェントのロードが既定で禁止されるため、
            // Mockito の MockMaker が自己アタッチできず初期化に失敗する
            // テスト用クラスパスにある mockito-core をそのまま -javaagent で渡す
            // (バージョンは教材の build.gradle の指定に従う。Mockito を使わない
            //  プロジェクトでは jar が見つからないので何もしない)
            doFirst {
                def mockitoJar = classpath.find { it.name.startsWith('mockito-core') }
                if (mockitoJar) {
                    jvmArgs "-javaagent:\${mockitoJar.absolutePath}"
                }
            }
        }
        tasks.withType(JacocoReport).configureEach {
            reports {
                xml.required = true
                html.required = true
            }
        }
    }
}
`;

function collectTestRunArtifacts(projectDir) {
  const tests = parseJUnitXmlReports(path.join(projectDir, 'build', 'test-results', 'test'));
  // JaCoCo XML のパスは既定 (jacocoTestReport.xml)。コース側で出力先を変えている場合に備えて探索する
  let coverage = parseJacocoXml(path.join(projectDir, 'build', 'reports', 'jacoco', 'test', 'jacocoTestReport.xml'));
  if (!coverage) {
    const repRoot = path.join(projectDir, 'build', 'reports', 'jacoco');
    if (fs.existsSync(repRoot)) {
      const stack = [repRoot];
      while (stack.length && !coverage) {
        const cur = stack.pop();
        for (const e of fs.readdirSync(cur, { withFileTypes: true })) {
          const p = path.join(cur, e.name);
          if (e.isDirectory()) stack.push(p);
          else if (e.name.endsWith('.xml')) { coverage = parseJacocoXml(p); if (coverage) break; }
        }
      }
    }
  }
  const summary = {
    total:   tests.length,
    passed:  tests.filter(t => t.status === 'passed').length,
    failed:  tests.filter(t => t.status === 'failed').length,
    skipped: tests.filter(t => t.status === 'skipped').length,
    time:    tests.reduce((a, t) => a + (t.time || 0), 0),
  };
  return { tests, summary, coverage };
}

module.exports = { decodeXmlEntities, parseXmlAttrs, parseJUnitXmlReports, parseJacocoXml, JACOCO_INIT_SCRIPT, collectTestRunArtifacts };
