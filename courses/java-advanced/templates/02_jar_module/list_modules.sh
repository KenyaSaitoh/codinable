#!/usr/bin/env bash
# Java SE と JDK のモジュールの一覧を表示する（レッスン2.1.3）
# 前提: なし（ファイル実行だけで動く）。java. で始まるものと jdk. で始まるものを見比べる
set -x
java --list-modules
