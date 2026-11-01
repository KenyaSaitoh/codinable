#!/usr/bin/env bash
# Person のスタブを呼ぶ（person.rest.test1.WireMockApp）
# 前提: 先に person.rest.test1.WireMockApp を起動しておく
# どの personId でも alice が返る。/persons/999 に 404 を返すスタブを足したら、ここで確かめる
set -x
curl -sS -i http://localhost:8080/persons/1
echo
curl -sS -i http://localhost:8080/persons/999
echo
curl -sS -i "http://localhost:8080/persons/query_by_age?lowerAge=30"
echo
curl -sS -i -X POST http://localhost:8080/persons -H "Content-Type: application/json" -d '{"personName":"Frank","age":36,"gender":"male"}'
echo
curl -sS -i -X DELETE http://localhost:8080/persons/6
echo
