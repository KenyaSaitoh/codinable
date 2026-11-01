// Express で書いた計算アプリケーション
//
// Spring MVC 版・Django 版と同じものを Node.js で書いている
// 違いは「サーバー側で HTML を組み立てる」という点ではなく、
// フレームワークの書き方だけであることを見比べてほしい
//
// ▶ アプリ実行を押すと、初回だけ依存パッケージ (express / ejs) を自動で入れてから起動し、
// http://localhost:3000 を検知してプレビュータブが開く

const express = require('express');

const app  = express();
const port = 3000;

// テンプレートエンジンの設定。views/*.ejs を探すようになる
app.set('view engine', 'ejs');

// フォーム (application/x-www-form-urlencoded) の本文を req.body に展開する
// これを書き忘れると req.body が undefined になる
app.use(express.urlencoded({ extended: false }));

// ── ビジネスロジック ──
// Express を知らない関数として切り離しておく (Spring MVC 版の CalcService にあたる)
const operations = {
  add:      (param1, param2) => param1 + param2,
  subtract: (param1, param2) => param1 - param2,
  multiply: (param1, param2) => param1 * param2,
  divide:   (param1, param2) => {
    if (param2 === 0) throw new Error('0 で割ることはできません');
    return param1 / param2;
  },
};
const symbols = { add: '+', subtract: '-', multiply: '×', divide: '÷' };

// 計算履歴。メモリ上に持つだけなので、サーバーを再起動すると消え、全利用者で共有される
// 実際のアプリケーションではデータベースに保存する
const history = [];

const calcService = {
  // 計算して、計算式と結果を履歴に追加する
  calculate(operator, param1, param2) {
    const result = operations[operator](param1, param2);
    history.push(`${param1} ${symbols[operator]} ${param2} = ${result}`);
    return result;
  },
  // 新しい順に limit 件までの履歴を返す
  getHistory(limit) {
    return history.slice(-limit).reverse();
  },
};

// ── 入力値の検証 ──
// Spring MVC 版の CalcParam (@NotNull / @Min / @Max) と同じ決まり・同じメッセージにする
function validate(value) {
  if (value === undefined || String(value).trim() === '') return '値を入力してください';
  const number = Number(value);
  if (Number.isNaN(number)) return '数値を入力してください';
  if (number < -1000) return '入力可能な最小値は-1000';
  if (number > 1000) return '入力可能な最大値は1000';
  return null;
}

app.get('/', (req, res) => {
  res.render('input', { form: {}, errors: {}, error: null });
});

// 計算履歴を表示する
// 計算 (/add など) は履歴というサーバーの状態を変えるので POST、
// 履歴の表示は取得するだけで状態を変えないので GET にする
// /history?limit=5 のクエリパラメータは req.query に入る (文字列なので数値に直す)
app.get('/history', (req, res) => {
  const limit = parseInt(req.query.limit, 10);
  res.render('history', { entries: calcService.getHistory(limit > 0 ? limit : 10) });
});

// 同じフォームを、押したボタンによって別の URL へ送る (views/input.ejs の formaction)
for (const operator of Object.keys(operations)) {
  app.post(`/${operator}`, (req, res) => {
    const form = { param1: req.body.param1, param2: req.body.param2 };
    const errors = { param1: validate(form.param1), param2: validate(form.param2) };
    // 入力値の決まりに反していれば入力画面に戻す
    if (errors.param1 || errors.param2) {
      res.render('input', { form, errors, error: null });
      return;
    }

    // フォームの値は文字列で届くので数値に直す
    const param1 = Number(form.param1);
    const param2 = Number(form.param2);
    try {
      const result = calcService.calculate(operator, param1, param2);
      res.render('output', { param1, param2, result });
    } catch (err) {
      // 入力形式は正しいが処理として成立しない場合 (業務エラー) は入力画面に戻す
      res.render('input', { form, errors: {}, error: err.message });
    }
  });
}

app.listen(port, () => {
  // この URL の出力をアプリが見つけて、プレビュータブを開く
  console.log(`http://localhost:${port} で待ち受けています`);
});
