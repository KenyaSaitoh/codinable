// 静的ページの演習が、プレビューで実際に動くかを確かめる検査用スクリプト
//
// アプリのプレビューは Electron の webview なので、同じ Chromium で読み込んで
// コンソールのエラーと、ページごとの見どころ（JavaScript が書き換えた結果など）を
// 確かめる。読み込めるだけでは足りないので、演習ごとに「こうなっていること」を書く
//
//   node desktop/test/serve-templates.js 4173   （別のターミナルで）
//   npx electron desktop/test/check-static-pages.js [port]
//
// SHOTS=<ディレクトリ> を付けると、各ページの画面も保存する（目で見て確かめたいとき）

const { app, BrowserWindow } = require('electron');
const fs   = require('fs');
const path = require('path');

// 教材のページは CSP を書いていないので、Electron の注意書きが混ざる。検査の邪魔になる
process.env.ELECTRON_DISABLE_SECURITY_WARNINGS = 'true';

const PORT = Number(process.argv[process.argv.length - 1]) || 4173;
const base = `http://localhost:${PORT}`;

// expect は読み込み後にページ内で評価する。false / 文字列を返したら NG とする
const PAGES = [
  { dir: 'html-basic', expect: `
      (() => {
         const heads = document.querySelectorAll('h1,h2,h3').length;
         const img   = document.querySelector('img[alt]');
         if (heads < 3)  return '見出しが ' + heads + ' 個しかない';
         if (!img)       return 'alt 付きの img が無い';
         if (!img.complete || img.naturalWidth === 0) return '画像が表示できていない: ' + img.src;
         if (!document.querySelector('a[href^=http]')) return '外部リンクが無い';
         return true;
       })()` },
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
         const before = document.querySelectorAll('#log li').length;
         document.querySelector('#hello-button').click();
         const after = document.querySelectorAll('#log li').length;
         if (after <= before) return 'クリックしてもログが増えなかった';
         if (document.querySelector('#click-count').textContent === '0 回')
           return 'クリック回数が更新されていない';
         return true;
       })()` },
  { dir: 'js-form', expect: `
      (() => {
         const form = document.querySelector('#registration-form');
         const fill = (pw2) => {
           form.querySelector('[name=name]').value            = '山田太郎';
           form.querySelector('[name=email]').value           = 'yamada@example.com';
           form.querySelector('[name=password]').value        = 'secret123';
           form.querySelector('[name=confirmPassword]').value = pw2;
           form.querySelector('[name=agree]').checked         = true;
           form.querySelector('[name=skills][value=Java]').checked = true;
         };
         // 先に不一致を試す（成功すると form.reset() で空になるため）
         fill('ちがう');
         form.requestSubmit();
         if (document.querySelector('#error-message').textContent.trim() === '')
           return 'パスワード不一致でもエラーが出ない';
         if (document.querySelector('#result').textContent.includes('山田太郎'))
           return '不一致なのに登録されてしまった';

         fill('secret123');
         form.requestSubmit();
         const out = document.querySelector('#result').textContent;
         if (!out.includes('山田太郎')) return '送信結果に入力値が出ない: ' + out.slice(0, 80);
         if (!out.includes('Java'))     return 'チェックボックスの値が出ない: ' + out.slice(0, 80);
         if (out.includes('secret123')) return 'パスワードがそのまま表示されている';
         if (form.querySelector('[name=name]').value !== '')
           return '送信後に入力欄が空に戻っていない';
         return true;
       })()` },
  { dir: 'js-fetch', wait: 'document.querySelectorAll("#user-list tr").length > 0', expect: `
      (() => {
         const rows = document.querySelectorAll('#user-list tr').length;
         if (rows < 4) return 'users.json の 4 件が出ていない: ' + rows + ' 行';
         if (!document.body.textContent.includes('営業部')) return '取得した値が表に出ていない';
         document.querySelector('#load-broken').click();
         return true;
       })()` },

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
         const pos = sel => {
           const el = document.querySelector(sel);
           return el ? getComputedStyle(el).position : ('要素が無い: ' + sel);
         };
         const checks = {
           '.floating-button': 'fixed',
           '.tag':             'absolute',
           '.anchor':          'relative',
           '.sticky-head':     'sticky',
         };
         for (const [sel, want] of Object.entries(checks)) {
           const got = pos(sel);
           if (got !== want) return sel + ' が ' + want + ' になっていない: ' + got;
         }
         return true;
       })()` },
  { dir: 'css-layout', expect: `
      (() => {
         const flex = [...document.querySelectorAll('*')]
           .filter(el => getComputedStyle(el).display === 'flex').length;
         const grid = [...document.querySelectorAll('*')]
           .filter(el => getComputedStyle(el).display === 'grid').length;
         return (flex > 0 && grid > 0) || ('flex=' + flex + ' grid=' + grid);
       })()` },
  // メディアクエリは幅で切り替わるので、広い窓と狭い窓の両方で確かめる
  { dir: 'css-responsive', width: 1200, label: 'css-responsive (1200px)', expect: `
      (() => {
         const cols = sel => getComputedStyle(document.querySelector(sel))
           .gridTemplateColumns.split(' ').length;
         if (cols('.products')  !== 3) return '商品が 3 列でない: ' + cols('.products');
         if (cols('.container') !== 2) return 'サイドバーが横並びでない: ' + cols('.container');
         return true;
       })()` },
  { dir: 'css-responsive', width: 420, label: 'css-responsive (420px)', expect: `
      (() => {
         const cols = sel => getComputedStyle(document.querySelector(sel))
           .gridTemplateColumns.split(' ').length;
         if (cols('.products')  !== 1) return '商品が 1 列でない: ' + cols('.products');
         if (cols('.container') !== 1) return 'サイドバーが積まれていない: ' + cols('.container');
         return true;
       })()` },

  { dir: 'static-web', expect: `
      document.querySelectorAll('tbody tr, li').length >= 2
        && !!document.querySelector('button')` },
];

// 1 枚ずつ開いて閉じるので、閉じた瞬間に既定の終了処理が走らないようにする
app.on('window-all-closed', () => {});

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
      await load(win, `${base}/${page.dir}/`);
      if (page.wait) await waitFor(win, page.wait);
      const result = await win.webContents.executeJavaScript(`(() => (${page.expect}))()`, true);
      if (result !== true) problems.push(`期待どおりでない: ${result}`);
      if (process.env.SHOTS) {
        fs.mkdirSync(process.env.SHOTS, { recursive: true });
        const png = await win.webContents.capturePage();
        fs.writeFileSync(path.join(process.env.SHOTS, label.replace(/[^\w.-]+/g, '_') + '.png'),
                         png.toPNG());
      }
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

// 連続してウィンドウを作って読み込むと、たまに ERR_FAILED で弾かれる
// ページ側の問題と紛れるので、少し待って一度だけやり直す
async function load(win, url) {
  try {
    await win.loadURL(url);
  } catch (e) {
    await new Promise(r => setTimeout(r, 300));
    await win.loadURL(url);
  }
}

async function waitFor(win, condition, timeoutMs = 5000) {
  const until = Date.now() + timeoutMs;
  while (Date.now() < until) {
    if (await win.webContents.executeJavaScript(`!!(${condition})`, true)) return;
    await new Promise(r => setTimeout(r, 100));
  }
  throw new Error(`条件が満たされなかった: ${condition}`);
}
