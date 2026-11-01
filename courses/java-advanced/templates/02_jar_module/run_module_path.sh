#!/usr/bin/env bash
# 2 つのモジュールを javac でコンパイルし、java の --module-path と --module で起動する（レッスン2.1.3 のコマンド）
# 前提: なし（ファイル実行だけで動く）。結果は 13 が表示される
cd "$(dirname "$0")" || exit 1
shopt -s globstar
OUT=build/modules
rm -rf "$OUT"
set -x
javac -encoding UTF-8 -d "$OUT/pro.kensait.foo" 02_module_foo_library/src/main/java/**/*.java || exit 1
javac -encoding UTF-8 --module-path "$OUT" -d "$OUT/pro.kensait.hoge" 02_module_hoge_app/src/main/java/**/*.java || exit 1
java --module-path "$OUT" --module pro.kensait.hoge/pro.kensait.hoge.Application
