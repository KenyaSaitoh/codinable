// イベント処理の基本形は、いつもこの 1 行に尽きる。
//
//   要素.addEventListener('イベント名', 発生したときに呼ばれる関数);
//
// 「いつ実行するか」を自分で決めるのではなく、「起きたら呼んでもらう」。
// この書き方をイベント駆動型プログラミングと呼ぶ。

const log = document.getElementById('log');

/** 起きたイベントを記録欄の先頭に積む */
function record(event, detail = '') {
  const li = document.createElement('li');
  const time = new Date().toLocaleTimeString('ja-JP');
  // event.type はイベント名、event.target はイベントが起きた要素。
  // load はページ全体の出来事なので、target は要素ではなく document になる
  const tag = event.target.tagName ? `<${event.target.tagName.toLowerCase()}>` : 'document';
  li.textContent = `${time}  ${event.type}  ${tag}  ${detail}`;
  log.prepend(li);
}

// ── click: 押されたとき ───────────────────────────────
let clickCount = 0;
const countLabel = document.getElementById('click-count');

document.getElementById('hello-button').addEventListener('click', event => {
  clickCount++;
  countLabel.textContent = `${clickCount} 回`;
  record(event);
});

// ── input: 値が変わるたび（1 文字ごと） ────────────────
const keyword = document.getElementById('keyword');
const echo    = document.getElementById('echo');

keyword.addEventListener('input', event => {
  // event.target は、このイベントが起きた要素（= keyword）を指す
  echo.textContent = event.target.value;
  record(event, `value="${event.target.value}"`);
});

// ── change: 値が確定したとき（フォーカスが外れたタイミング） ──
// input との違いを見るため、同じ入力欄に両方を登録してある
keyword.addEventListener('change', event => {
  record(event, `確定値="${event.target.value}"`);
});

document.getElementById('department').addEventListener('change', event => {
  record(event, `選択="${event.target.value}"`);
});

// ── focus / blur: フォーカスが当たったとき・外れたとき ──
const memo = document.getElementById('memo');

memo.addEventListener('focus', event => {
  memo.style.borderColor = '#2f6feb';
  record(event);
});
memo.addEventListener('blur', event => {
  memo.style.borderColor = '';
  record(event);
});

// ── keydown: キーが押されたとき ───────────────────────
document.getElementById('keys').addEventListener('keydown', event => {
  // event には、そのイベント固有の情報が入っている（キー名・座標・押されたボタンなど）
  record(event, `key="${event.key}"`);
});

// ── mouseover / mouseout: マウスが乗ったとき・離れたとき ──
const hoverBox = document.getElementById('hover-box');

hoverBox.addEventListener('mouseover', event => {
  hoverBox.classList.add('hovered');
  record(event);
});
hoverBox.addEventListener('mouseout', event => {
  hoverBox.classList.remove('hovered');
  record(event);
});

// ── 記録を消す ───────────────────────────────────────
document.getElementById('clear-log').addEventListener('click', () => {
  log.innerHTML = '';
});

// ── load: ページと画像などの読み込みが終わったとき ─────
// window に対して登録する（要素ではなくページ全体の出来事なので）
window.addEventListener('load', event => {
  record(event, 'ページの読み込みが終わった');
});
