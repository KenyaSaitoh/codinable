# 06_network_io — ネットワークと HTTP

## 何を学ぶ演習か

`ByteBuffer`、TCP と UDP のサーバーとクライアント、セレクタ、HTTP クライアント API を確かめます（チャプター6.1・6.2）。

## 見るファイル（`src/main/java/pro/kensait/java/advanced/`）

- `lsn_6_1_2.Main` … `ByteBuffer` の位置とリミット
- `lsn_6_1_3` … TCP（`server.Main`・`server.Main_Multi`、`client.Main`・`client.Main_Multi`）
- `lsn_6_1_4` … セレクタのサーバー、`lsn_6_1_5` … UDP（`nonblockingserver` を含む）
- `lsn_6_2_3` … HTTP の同期呼び出し（ポート 8080）、`lsn_6_2_4` … 非同期呼び出し（応答まで 10 秒待つサーバー）

## ボタンでの動かし方

**クライアントのクラスを開いて ファイル実行を押すだけで、相手のサーバーも一緒に起動します。** サーバーの出力は `[server]` が付いて、同じ「実行結果」に流れます。

| クライアント | 一緒に起動するサーバー |
|:--|:--|
| `lsn_6_1_3.client.Main`・`Main_Multi` | `lsn_6_1_3.server.Main` |
| `lsn_6_1_4.client.Main` | `lsn_6_1_4.server.Main` |
| `lsn_6_1_5.client.Main`・`Main_Multi` | `lsn_6_1_5.server.Main` |
| `lsn_6_2_3.httpclient` の各クラス | `lsn_6_2_3.httpserver.Main` |
| `lsn_6_2_4.httpclient` の各クラス | `lsn_6_2_4.httpserver.Main` |

## サーバーの止め方

サーバーは無限ループで待ち続けます。クライアントが終わっても残るので、確かめ終わったら止めます。

- 一緒に起動したサーバー … 出力欄の右の列の **server の停止** を押します（次のクライアントを ファイル実行すると、自動で起こし直します）。
- サーバーのクラスを開いて ファイル実行したとき … 出力欄の **停止** を押します。

## 別の組み合わせを試すとき（`client/` のスクリプト）

実行は同時に 1 本だけなので、サーバーを ファイル実行した後にクライアントの .java を ファイル実行すると、サーバーは止まって表の組み合わせに替わります。表に無い組み合わせは、先にサーバーを ファイル実行し、`client/` の `.sh` を開いて ファイル実行します（サーバーを止めずに脇で動きます）。

- `server.Main_Multi` と `tcp_client_multi.sh` … 接続ごとのスレッド。3 件がほぼ同時に終わります
- `nonblockingserver.Main` と `udp_client_multi.sh` … `clear()` が要らない形。3 件とも表示されます
- `lsn_6_2_4.httpserver.Main` と `http_get_sync.sh` … 遅いサーバーを相手にした同期呼び出し
- サーバーなしで `tcp_client.sh`（接続できずに例外）、`udp_client.sh`（例外にならない）、`http_async_3.sh`（30 秒待って何も出ない）。先に server を止めておきます

## 結果のどこを見るか

- TCP は `response => Hello! 私はAliceです`、HTTP はステータス `200` とボディ `Hello! 私はAliceです` です。
- `lsn_6_1_3.client.Main_Multi` は 1 件ずつ約 2 秒おきに処理されます。
- `lsn_6_1_5.client.Main_Multi` で `[server]` に表示される 3 行は、どれも最初に届いた名前です（`server.Main` に `clear()` が無いため、2 件目以降は捨てられます）。
