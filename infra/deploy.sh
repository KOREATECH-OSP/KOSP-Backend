#!/usr/bin/env bash
set -euo pipefail

# =============================================================================
# KOSP Deploy 스크립트
# 아래 변수를 서버 환경에 맞게 수정 후 사용
# =============================================================================

MODULE="backend"          # backend | harvester
BRANCH="develop"
PROFILE="dev"
BACKEND_PORT=8080

# =============================================================================

PROJECT_DIR="KOSP-Backend"
LOG_DIR="/home/ubuntu/logs"
LOG_FILE="${LOG_DIR}/${MODULE}.log"
PID_FILE="${LOG_DIR}/${MODULE}.pid"

cd "$(dirname "$0")" || exit 1

echo "=========================================="
echo "${MODULE} 배포 시작 (branch: ${BRANCH}, profile: ${PROFILE})"
echo "=========================================="

mkdir -p "${LOG_DIR}"

rm -f "./${PROJECT_DIR}/${MODULE}/src/main/resources/application-${PROFILE}.yml"

echo "[1/6] 소스 업데이트 중..."
cd "./${PROJECT_DIR}" || exit 1
git pull origin "${BRANCH}" || exit 1
cd ..

echo "[2/6] 설정 파일 복사 중..."
cp "./application-${PROFILE}.yml" "./${PROJECT_DIR}/${MODULE}/src/main/resources/application-${PROFILE}.yml" || exit 1
cp "./.env.${PROFILE}" "./${PROJECT_DIR}/.env.${PROFILE}" || exit 1

echo "[3/6] ${MODULE} 빌드 중..."
"./${PROJECT_DIR}/gradlew" -p "./${PROJECT_DIR}" ":${MODULE}:clean" ":${MODULE}:build" -x test || exit 1

echo "[4/6] JAR 파일 탐색 중..."
JAR_FILE=$(find "./${PROJECT_DIR}/${MODULE}/build/libs" -name "*.jar" ! -name "*-plain.jar" | head -1)

if [[ -z "$JAR_FILE" ]]; then
  echo "Error: JAR 파일을 찾을 수 없습니다."
  exit 1
fi

echo "JAR 파일: ${JAR_FILE}"

echo "[5/6] 기존 프로세스 종료 중..."

stop_process() {
  local pid="$1"
  if [[ -n "$pid" ]] && kill -0 "$pid" 2>/dev/null; then
    echo "프로세스 종료 중... (PID: ${pid})"
    kill "$pid"
    tail --pid="$pid" -f /dev/null 2>/dev/null || true
  fi
}

if [[ "$MODULE" == "backend" ]]; then
  PID=$(lsof -ti:"${BACKEND_PORT}" 2>/dev/null || true)
  stop_process "$PID"
else
  if [[ -f "$PID_FILE" ]]; then
    PID=$(cat "$PID_FILE")
    stop_process "$PID"
    rm -f "$PID_FILE"
  fi
fi

echo "[6/6] ${MODULE} 시작 중 (profile: ${PROFILE})..."
nohup java -jar "${JAR_FILE}" --spring.profiles.active="${PROFILE}" > "${LOG_FILE}" 2>&1 &
echo $! > "$PID_FILE"

echo "=========================================="
echo "${MODULE} 시작 완료! (PID: $(cat "$PID_FILE"))"
echo "로그: ${LOG_FILE}"
echo "=========================================="

tail -f "${LOG_FILE}"
