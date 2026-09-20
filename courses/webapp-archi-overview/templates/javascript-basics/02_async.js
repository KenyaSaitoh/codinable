// 非同期処理。JavaScript は 1 本のスレッドで動くため、
// 待ち時間のある処理は「終わったら続きをやる」形で書く。

// ── Promise を返す関数 ──
const wait = ms => new Promise(resolve => setTimeout(resolve, ms));

// ── async / await: Promise を同期的な見た目で書く ──
async function main() {
  console.log('1. 開始');

  await wait(300);
  console.log('2. 300ms 待った');

  // fetch は Node.js に標準で入っている (ブラウザーと同じ API)
  const response = await fetch('https://httpbin.org/json');
  if (!response.ok) {
    // HTTP のエラー応答は例外にならないので、自分で判定する
    throw new Error(`HTTP ${response.status}`);
  }
  const data = await response.json();
  console.log('3. 取得したタイトル:', data.slideshow.title);

  // ── 並行実行: 順番に待つ必要がないものは Promise.all でまとめる ──
  const started = Date.now();
  await Promise.all([wait(300), wait(300), wait(300)]);
  console.log(`4. 3 本まとめて待つと ${Date.now() - started}ms (直列なら 900ms)`);
}

main().catch(err => {
  console.error('失敗:', err.message);
  process.exitCode = 1;
});
