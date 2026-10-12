#!/usr/bin/env bash
# .env.example を .env にコピーする（初回だけ。.env が既にあれば上書きしない）
# .env の KEYCLOAK_CLIENT_SECRET は、同梱の realm（keycloak/learn-realm.json）と対になるローカル教材専用の値
# .env は application.yml の spring.config.import で起動時に読み込まれる
# このあと Docker で Keycloak を起動してから（README 参照）、▶ アプリ実行でアプリを起動する
cd "$(dirname "$0")/.." || exit 1
set -x
if [ -f .env ]; then
  echo '.env は既にあります（上書きしません）'
else
  cp .env.example .env
fi
cat .env
