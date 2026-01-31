#!/bin/bash
set -a
source ../.env.local
set +a
exec ../gradlew :api-service:bootRun
