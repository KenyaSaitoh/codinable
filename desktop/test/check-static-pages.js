// 静的ページの演習が、プレビューで実際に動くかを確かめる検査用スクリプト。
//
// アプリのプレビューは Electron の webview なので、同じ Chromium で読み込んで
// コンソールのエラーと、ページごとの見どころ（JavaScript が書き換えた結果など）を
// 確かめる。読み込めるだけでは足りないので、演習ごとに「こうなっていること」を書く。
//
//   node desktop/test/serve-templates.js 4173   （別のターミナルで）
//   npx electron desktop/test/check-static-pages.js [port]

const { app, BrowserWindow } = require('electron');

const PORT = Number(process.argv[process.argv.length - 1]) || 4173;
const base = `http://localhost:${PORT}`;

// expect は読み込み後にページ内で評価する。false / 文字列を返したら NG とする
const PAGES = [
  { dir: 'html-basic', expect: `
      document.querySelectorAll('h1,h2,h3').length >= 3
        && !!document.querySelector('img[alt]')
        && document.querySelector('img').naturalWidth > 0` },
  { dir: 'html-list-table', expect: `
      document.querySelectorAll('ul li, ol li').length >= 6
        && document.querySelectorAll('table thead th').length >= 2
        && document.querySelectorAll('table tbody tr').length >= 2` },
  { dir: 'html-semantic', expect: `
      ['header','nav','main','article','footer'].every(t => !!document.querySelector(t))
        && getComputedStyle(document.querySelector('main')).borderTopWidth !== '0px'` },
  { dir: 'html-form', expect: `
      document.querySelectorAll('form input, form select, form textarea').length >= 10
        && !!document.querySelector('input[required]')
        && !!document.querySelector('input[pattern]')
        && document.querySelector('form').checkValidity() === false` },

  { dir: 'js-dom', expect: `
      !document.body.textContent.includes('書き換え前')
        && document.querySelectorAll('tbody tr').length >= 3` },
  { dir: 'js-events', expect: `
      (() => {
         const btn = document.querySelector('button');
         const before = document.querySelectorAll('#event-log li').length;
         btn.click();
         const after = document.querySelectorAll('#event-log li').length;
         return after > before || 'クリックしてもログが増えなかった';
       })()` },
  { dir: 'js-form', expect: `
      (() => {
         const form = document.querySelector('form');
         form.querySelector('[name=name]').value      = '山田太郎';
         form.querySelector('[name=email]').value     = 'yamada@example.com';
         form.querySelector('[name=password]').value  = 'secret123';
         form.querySelector('[name=password2]').value = 'secret123';
         form.requestSubmit();
         const out = document.querySelector('#result').textContent;
         return out.includes('山田太郎') || ('送信結果に入力値が出ない: ' + out.slice(0, 80));
       })()` },
  { dir: 'js-fetch', wait: 'document.querySelectorAll("tbody tr").length > 0', expect: `
      document.querySelectorAll('tbody tr').length >= 3
        && document.body.textContent.includes('@example.com')` },

  { dir: 'css-selectors', expect: `
      (() => {
         const s = getComputedStyle(document.querySelector('.highlight') || document.body);
         return s.color !== 'rgb(0, 0, 0)' || 'クラスセレクタの色が当たっていない';
       })()` },
  { dir: 'css-box', expect: `
      (() => {
         const cb = document.querySelector('.content-box');
         const bb = document.querySelector('.border-box');
         if (!cb || !bb) return 'box-sizing の比較要素が無い';
         return cb.offsetWidth !== bb.offsetWidth
           || ('content-box と border-box の実幅が同じ: ' + cb.offsetWidth);
       })()` },
  { dir: 'css-position', expect: `
      (() => {
         const f = document.querySelector('.fixed-badge, .fixed-box, [class*=fixed]');
         const a = document.querySelector('[class*=absolute], .tag');
         if (!f || !a) return '固定・絶対配置の要素が無い';
         return getComputedStyle(f).position === 'fixed'
           && getComputedStyle(a).position === 'absolute';
       })()` },
  { dir: 'css-layout', expect: `
      (() => {
         const flex = [...document.querySelectorAll('*')]
           .filter(el => getComputedStyle(el).display === 'flex').length;
         const grid = [...document.querySelectorAll('*')]
           .filter(el => getComputedStyle(el).display === 'grid').length;
         return (flex > 0 && grid > 0) || ('flex=' + flex + ' grid=' + grid);
       })()` },
  { dir: 'css-responsive', width: 1200, expect: `
      (() => {
         const grid = document.querySelector('.product-grid, [class*=grid]');
         if (!grid) return 'グリッドが無い';
         const cols = getComputedStyle(grid).gridTemplateColumns.split(' ').length;
         return cols >= 3 || ('1200px 幅で ' + cols + ' 列になっている');
       })()` },
  { dir: 'css-responsive', width: 420, label: 'css-responsive (420px)', expect: `
      (() => {
         const grid = document.querySelector('.product-grid, [class*=grid]');
         const cols = getComputedStyle(grid).gridTemplateColumns.split(' ').length;
         return cols === 1 || ('420px 幅で ' + cols + ' 列になっている');
       })()` },

  { dir: 'static-web', expect: `
      document.querySelectorAll('tbody tr, li').length >= 2
        && !!document.querySelector('button')` },
];

app.whenReady().then(async () => {
  let ng = 0;

  for (const page of PAGES) {
    const label = page.label || page.dir;
    const win = new BrowserWindow({
      show: false,
      width: page.width || 900,
      height: 800,
      webPreferences: { contextIsolation: true },
    });

    const problems = [];
    win.webContents.on('console-message', (_e, level, message, line, source) => {
      // level 3 = error。読み込み失敗も console に出る
      if (level >= 2) problems.push(`console: ${message} (${source}:${line})`);
    });
    win.webContents.on('did-fail-load', (_e, code, desc, url) => {
      problems.push(`読み込み失敗: ${desc} (${url})`);
    });

    try {
      await win.loadURL(`${base}/${page.dir}/`);
      if (page.wait) await waitFor(win, page.wait);
      const result = await win.webContents.executeJavaScript(`(() => (${page.expect}))()`, true);
      if (result !== true) problems.push(`期待どおりでない: ${result}`);
    } catch (e) {
      problems.push(`例外: ${e.message}`);
    }
    win.destroy();

    if (problems.length) ng++;
    console.log((problems.length ? 'NG   ' : 'OK   ') + label
      + problems.map(p => '\n     ' + p).join(''));
  }

  console.log(ng === 0 ? `全 ${PAGES.length} 件 OK` : `NG ${ng} 件`);
  app.exit(ng === 0 ? 0 : 1);
});

async function waitFor(win, condition, timeoutMs = 5000) {
  const until = Date.now() + timeoutMs;
  while (Date.now() < until) {
    if (await win.webContents.executeJavaScript(`!!(${condition})`, true)) return;
    await new Promise(r => setTimeout(r, 100));
  }
  throw new Error(`条件が満たされなかった: ${condition}`);
}
