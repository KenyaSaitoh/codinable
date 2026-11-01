#!/usr/bin/env bash
# POST /calc/add のスタブを呼ぶ（calc.rest.test2.WireMockApp）
# 前提: 先に calc.rest.test2.WireMockApp を起動しておく
# 広い条件で 200 と 40.0、param2 が -1000.0 なら狭い条件で 400
set -x
curl -sS -i -X POST http://localhost:8080/calc/add -H "Content-Type: application/json" -d '{"param1":30.0,"param2":10.0}'
echo
curl -sS -i -X POST http://localhost:8080/calc/add -H "Content-Type: application/json" -d '{"param1":30.0,"param2":-1000.0}'
echo
curl -sS -i -X POST http://localhost:8080/calc/add -H "Content-Type: application/json" -d '{"param1s":"abc","param2s":"def"}'
echo
