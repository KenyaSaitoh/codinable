// 演習の静的ページを、アプリと同じ配信の仕組みで開けるようにする検査用サーバー。
//
// アプリ本体（main/static-server.js）をそのまま使うので、
// MIME の扱いやディレクトリの index.html 補完もアプリと同じ条件になる。
//
//   node desktop/test/serve-templates.js [port]

const path   = require('path');
const http   = require('http');
const fs     = require('fs');

const ROOT = path.resolve(__dirname, '../../courses/webapp-archi-overview/templates');
const PORT = Number(process.argv[2] || 4173);

const MIME = {
  '.html': 'text/html; charset=utf-8',
  '.css':  'text/css; charset=utf-8',
  '.js':   'text/javascript; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.svg':  'image/svg+xml',
};

http.createServer((req, res) => {
  const pathname = decodeURIComponent(new URL(req.url, 'http://localhost').pathname);
  const parts = pathname.split('/').filter(p => p && p !== '.' && p !== '..');
  let file = path.resolve(ROOT, ...parts);
  if (!file.startsWith(ROOT)) { res.writeHead(403).end('forbidden'); return; }
  if (fs.existsSync(file) && fs.statSync(file).isDirectory()) file = path.join(file, 'index.html');
  if (!fs.existsSync(file)) { res.writeHead(404).end('not found'); return; }
  res.writeHead(200, {
    'content-type':  MIME[path.extname(file).toLowerCase()] || 'application/octet-stream',
    'cache-control': 'no-store',
  }).end(fs.readFileSync(file));
}).listen(PORT, '127.0.0.1', () => {
  console.log(`serving ${ROOT} on http://localhost:${PORT}/`);
});
