// Express で書いた計算アプリケーション。
//
// Spring MVC 版・Django 版と同じものを Node.js で書いている。
// 違いは「サーバー側で HTML を組み立てる」という点ではなく、
// フレームワークの書き方だけであることを見比べてほしい。
//
//   1. 実行対象で「npm: install」を選んで実行
//   2. 続けて「npm: start」を選んで実行
//   3. http://localhost:3000 を検知してプレビュータブが開く

const express = require('express');

const app  = express();
const port = 3000;

// テンプレートエンジンの設定。views/*.ejs を探すようになる
app.set('view engine', 'ejs');

// フォーム (application/x-www-form-urlencoded) の本文を req.body に展開する。
// これを書き忘れると req.body が undefined になる。
app.use(express.urlencoded({ extended: false }));

// ビジネスロジック。Express を知らない関数として切り離しておく
function calculate(operator, param1, param2) {
  switch (operator) {
    case 'add':      return param1 + param2;
    case 'subtract': return param1 - param2;
    case 'multiply': return param1 * param2;
    case 'divide':
      if (param2 === 0) throw new Error('0 で割ることはできません');
      return param1 / param2;
    default:
      throw new Error(`不明な演算です: ${operator}`);
  }
}

app.get('/', (req, res) => {
  res.render('input', { error: null });
});

app.post('/calc', (req, res) => {
  // フォームの値は文字列で届くので数値に直す (ここが入力値検証にあたる)
  const param1 = Number(req.body.param1);
  const param2 = Number(req.body.param2);
  if (Number.isNaN(param1) || Number.isNaN(param2)) {
    res.status(400).render('input', { error: '数値を入力してください' });
    return;
  }

  try {
    const result = calculate(req.body.operator, param1, param2);
    res.render('output', { param1, param2, result });
  } catch (err) {
    res.status(400).render('input', { error: err.message });
  }
});

app.listen(port, () => {
  // この URL の出力をアプリが見つけて、プレビュータブを開く
  console.log(`http://localhost:${port} で待ち受けています`);
});
