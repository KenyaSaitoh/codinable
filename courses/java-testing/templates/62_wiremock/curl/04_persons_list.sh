#!/usr/bin/env bash
# withBodyFile のスタブを呼ぶ（person.rest.test2.WireMockApp）
# 前提: 先に person.rest.test2.WireMockApp を起動しておく
# src/test/resources/__files/persons.json の中身が返る
set -x
curl -sS -i http://localhost:8080/persons
echo
