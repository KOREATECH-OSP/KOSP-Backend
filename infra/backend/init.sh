#!/bin/bash

# 1. .env 파일 로드
if [ ! -f .env ]; then
  echo "Error: .env 파일이 없습니다."
  exit 1
fi
export $(cat .env | grep -v '#' | xargs)

echo "### 1. Nginx 설정 파일 생성 중..."

# 소스(템플릿)는 고정 경로
TEMPLATE_FILE="./nginx/default.conf.template"

# 타겟(실제 설정)은 환경변수 경로
OUTPUT_DIR="${NGINX_VOLUME_PATH}"
OUTPUT_FILE="${OUTPUT_DIR}/default.conf"

# 템플릿 존재 확인
if [ ! -f "$TEMPLATE_FILE" ]; then
  echo "Error: 템플릿 파일($TEMPLATE_FILE)을 찾을 수 없습니다."
  exit 1
fi

# 타겟 디렉토리가 없으면 생성
if [ ! -d "$OUTPUT_DIR" ]; then
  echo "Creating directory: $OUTPUT_DIR"
  mkdir -p "$OUTPUT_DIR"
fi

# 변수 치환: ./nginx/default.conf.template -> ${NGINX_VOLUME_PATH}/default.conf
envsubst '${DOMAIN}' < "$TEMPLATE_FILE" > "$OUTPUT_FILE"
echo "Created: $OUTPUT_FILE from $TEMPLATE_FILE"

echo "### 2. 컨테이너 빌드 및 실행..."
docker compose up -d --build

echo "### 3. Certbot 자동 설정 마법사 실행..."
docker compose exec nginx certbot --nginx \
  -d $DOMAIN \
  --non-interactive \
  --agree-tos \
  --register-unsafely-without-email \
  --redirect

echo "### 설정 완료!"
