#!/bin/bash

APP_URL=http://localhost:8080/dlface

#curl -H "Content-Type: application/json" -X GET http://localhost:8080/dlface/dl/v1/downloads | grep -Po '"name:":.*?[^\\]",'


TMP_FILE=/tmp/$$-dlcmd

trap "rm $TMP_FILE" EXIT

curl -sS -H "Content-Type: application/json" -X GET "$APP_URL"/dl/v1/downloads > $TMP_FILE

#cat $TMP_FILE
echo
echo "====="

CNT=$(grep -Po '"name":"[^"]*"' $TMP_FILE | wc -l)

for NTH in `seq 0 $CNT`; do
(
grep -Po '"name":"[^"]*"' $TMP_FILE
grep -Po '"originalUrl":"[^"]*"' $TMP_FILE
grep -Po '"totalSize":[0-9]*' $TMP_FILE
grep -Po '"downloadedSize":[0-9]*' $TMP_FILE
) | awk 'NR % '$CNT' == '$NTH
echo
done
