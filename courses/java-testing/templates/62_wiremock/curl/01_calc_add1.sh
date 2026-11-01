#!/usr/bin/env bash
# GET /calc/add1 のスタブを呼ぶ（calc.rest.test1.WireMockApp）
# 前提: 先に calc.rest.test1.WireMockApp を起動しておく
# 条件に一致すれば 200 と 40.0、一致しなければ 404
set -x
curl -sS -i "http://localhost:8080/calc/add1?param1=30.0&param2=10.0"
echo
curl -sS -i "http://localhost:8080/calc/add1?param1=30&param2=10"
echo
