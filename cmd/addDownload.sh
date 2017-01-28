#!/bin/bash

APP_URL=http://localhost:8080/dlface

curl -H "Content-Type: application/json" -X POST -d '{"downloadList":"'"$1"'"}' "${APP_URL}"/dl/v1/downloads/add
