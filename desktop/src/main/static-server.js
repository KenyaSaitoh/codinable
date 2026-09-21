// ═══════════════════════════════════════════════════════════
//  静的 Web ページ用の簡易 HTTP サーバー
//
//  HTML / CSS / JavaScript だけの教材を、file:// ではなく http:// で開けるようにする
//  file:// だと fetch / ES モジュール / Cookie / localStorage の挙動が
//  本番と変わってしまい、講座の説明と食い違うため
//
//  ループバックにのみ bind し、配信ルートの外は出さない
// ═══════════════════════════════════════════════════════════

const fs   = require('fs');
const http = require('http');
const path = require('path');

const MIME = {
  '.html': 'text/html; charset=utf-8',
  '.htm':  'text/html; charset=utf-8',
  '.css':  'text/css; charset=utf-8',
  '.js':   'text/javascript; charset=utf-8',
  '.mjs':  'text/javascript; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.map':  'application/json; charset=utf-8',
  '.svg':  'image/svg+xml',
  '.png':  'image/png',
  '.jpg':  'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.gif':  'image/gif',
  '.webp': 'image/webp',
  '.avif': 'image/avif',
  '.ico':  'image/x-icon',
  '.woff': 'font/woff',
  '.woff2':'font/woff2',
  '.ttf':  'font/ttf',
  '.otf':  'font/otf',
  '.txt':  'text/plain; charset=utf-8',
  '.md':   'text/plain; charset=utf-8',
  '.csv':  'text/csv; charset=utf-8',
  '.xml':  'application/xml; charset=utf-8',
  '.pdf':  'application/pdf',
  '.mp4':  'video/mp4',
  '.webm': 'video/webm',
  '.mp3':  'audio/mpeg',
  '.wasm': 'application/wasm',
};

let server   = null;
let serveRoot = null;
let servePort = null;

function contentType(file) {
  return MIME[path.extname(file).toLowerCase()] || 'application/octet-stream';
}

function handle(req, res) {
  const root = serveRoot;
  if (!root) { res.writeHead(503).end('server is stopping'); return; }

  let pathname;
  try {
    pathname = decodeURIComponent(new URL(req.url, 'http://localhost').pathname);
  } catch {
    res.writeHead(400).end('bad request');
    return;
  }

  // 配信ルートの外へ出る要求はすべて拒否する
  const parts = pathname.split('/').filter(p => p && p !== '.' && p !== '..');
  const target = path.resolve(root, ...parts);
  if (target !== path.resolve(root) && !target.startsWith(path.resolve(root) + path.sep)) {
    res.writeHead(403).end('forbidden');
    return;
  }

  let file = target;
  try {
    if (fs.existsSync(file) && fs.statSync(file).isDirectory()) {
      file = path.join(file, 'index.html');
    }
    if (!fs.existsSync(file)) {
      res.writeHead(404, { 'content-type': 'text/html; charset=utf-8' })
         .end(`<!doctype html><meta charset="utf-8"><title>404</title>` +
              `<body style="font-family:system-ui;padding:2rem">` +
              `<h1>404 Not Found</h1><p><code>${pathname}</code></p></body>`);
      return;
    }
    const body = fs.readFileSync(file);
    res.writeHead(200, {
      'content-type':  contentType(file),
      'content-length': body.length,
      // 保存してリロードすればすぐ反映されるように、キャッシュさせない
      'cache-control': 'no-store',
    }).end(body);
  } catch (err) {
    res.writeHead(500, { 'content-type': 'text/plain; charset=utf-8' }).end(String(err.message));
  }
}

/** 空きポートで listen する (0 を指定して OS に選ばせる) */
function listenOnFreePort() {
  return new Promise((resolve, reject) => {
    const s = http.createServer(handle);
    s.on('error', reject);
    s.listen(0, '127.0.0.1', () => resolve(s));
  });
}

/**
 * ディレクトリの配信を開始する (既に動いていれば配信ルートだけ差し替える)
 * @returns {Promise<{ok: true, url: string, port: number, root: string}|{ok:false,error:string}>}
 */
async function serve(rootDir) {
  if (!rootDir || !fs.existsSync(rootDir)) return { ok: false, error: 'not-found' };
  serveRoot = rootDir;

  if (server && server.listening) {
    return { ok: true, url: `http://localhost:${servePort}/`, port: servePort, root: rootDir };
  }

  try {
    server = await listenOnFreePort();
  } catch (err) {
    server = null;
    return { ok: false, error: err.message };
  }
  servePort = server.address().port;
  console.log(`[static] serving ${rootDir} on http://localhost:${servePort}/`);
  return { ok: true, url: `http://localhost:${servePort}/`, port: servePort, root: rootDir };
}

function status() {
  return server && server.listening
    ? { running: true, url: `http://localhost:${servePort}/`, port: servePort, root: serveRoot }
    : { running: false };
}

function stop() {
  if (server) {
    try { server.close(); } catch { /* すでに閉じている */ }
    server = null;
  }
  serveRoot = null;
  servePort = null;
  return { ok: true };
}

module.exports = { serve, status, stop };
