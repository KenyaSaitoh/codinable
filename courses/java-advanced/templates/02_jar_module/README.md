# 02_jar_module — モジュールシステムの構造

## 何を学ぶ演習か

依存（`requires`）・公開（`exports`）・`public` の 3 つがそろったときだけ、別のモジュールのクラスを使えることを確かめます（レッスン2.1.2・2.1.3）。

## 見るファイル

- `02_module_foo_library/src/main/java/module-info.java` … fooモジュール。`pro.kensait.foo.api` だけを `exports` する
- `02_module_foo_library/src/main/java/pro/kensait/foo/api/StringUtil.java` … 公開するクラス
- `02_module_foo_library/src/main/java/pro/kensait/foo/internal/Common.java` … `public` でも外から使えないクラス
- `02_module_hoge_app/src/main/java/module-info.java` … hogeモジュール。fooモジュールを `requires` する
- `02_module_hoge_app/src/main/java/pro/kensait/hoge/Application.java` … メインクラス

## ボタンでの動かし方

- ツールバーの **アプリ実行** で `Application` を動かします。hoge と foo の 2 つのモジュールをビルドし、モジュールパスで起動します。
  サブフォルダーの中のクラスなので、`Application.java` のエディタには ファイル実行が出ません。
- `run_module_path.sh` を開いて **ファイル実行** すると、`javac` と `java --module-path ... --module pro.kensait.hoge/pro.kensait.hoge.Application` で同じものを起動します（レッスン2.1.3 のコマンド）。
- `list_modules.sh` を開いて **ファイル実行** すると、`java --list-modules` の結果が出ます。

## 結果のどこを見るか

- `Application` の結果は `13` です（"ABCDE" と "ABCDEFGH" の長さの合計）。
- `Application` の `Common` の行のコメントを外して アプリ実行を押すと、コンパイルエラーになります。`pro.kensait.foo.internal` が `exports` されていないためです。**↺ 初期化** で戻せます。
- `list_modules.sh` の結果で、`java.` で始まるものと `jdk.` で始まるものを見比べます。
