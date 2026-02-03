#!/usr/bin/env bash
set -euo pipefail

MODULES=("backend")
BRANCH="develop"
PROFILE="dev"

PROJECT_DIR="KOSP-Backend"

cd "$(dirname "$0")" || exit 1

DEPLOY_TARGET="${MODULES[0]}"

echo "=========================================="
echo "${DEPLOY_TARGET} 배포 시작 (branch: ${BRANCH}, profile: ${PROFILE})"
echo "=========================================="

echo "[1/5] 소스 업데이트 중..."
cd "./${PROJECT_DIR}" || exit 1
git pull origin "${BRANCH}" || exit 1

echo "[2/5] JAR 파일 빌드 중..."
BUILD_TASKS="clean"
for mod in "${MODULES[@]}"; do
  BUILD_TASKS="${BUILD_TASKS} :${mod}:bootJar"
done
./gradlew ${BUILD_TASKS} || exit 1

echo "[3/5] .env 파일 복사 중..."
if [[ "${DEPLOY_TARGET}" == "backend" ]]; then
  cp "../.env.${PROFILE}" "infra/backend/.env" || exit 1
  COMPOSE_FILE="infra/backend/docker-compose.yml"
else
  cp "../.env.${PROFILE}" "infra/workers/.env" || exit 1
  COMPOSE_FILE="infra/workers/docker-compose.yml"
fi

echo "[4/5] 기존 컨테이너 중지 및 제거 중..."
sudo docker compose -f "${COMPOSE_FILE}" down || true

echo "[5/5] Docker 컨테이너 시작 중..."
sudo docker compose -f "${COMPOSE_FILE}" up -d --build

echo "=========================================="
echo "${DEPLOY_TARGET} 시작 완료!"
echo "로그 확인: sudo docker compose -f ${COMPOSE_FILE} logs -f"
echo "=========================================="

sudo docker compose -f "${COMPOSE_FILE}" logs -f
