// Payara Server の版とポート。セットアップ (scripts/setup.ps1)・実行 (main/payara.js)・
// 設定画面の同梱ランタイムの表示で共有する
// 版を上げるときは Jakarta EE の講座 (courses/jakartaee-*) の演習を流し直す
const VERSIONS = { payara: '7.2026.9' };

// 配布物の domain1 の既定のまま。待ち受けは 127.0.0.1 だけにする (main/payara.js)
const PORTS = { http: 8080, https: 8181, admin: 4848, iiop: 3700, jms: 7676, jmx: 8686 };

module.exports = { VERSIONS, PORTS };
