FROM nginx:latest

# 1. 패키지 설치
# python3: 스크립트 실행용
# curl: 스크립트 내부의 subprocess.run("curl"...) 실행용
# certbot, python3-certbot-nginx, cron: 필수 요소
RUN apt-get update && apt-get install -y \
    certbot \
    cron \
    curl \
    python3 \
    python3-certbot-nginx \
    && \
    rm -rf /var/lib/apt/lists/*

# 2. 파이썬 스크립트 복사
COPY certbot_with_slack_hook.py /usr/local/bin/certbot_renew.py

# 3. 파이썬 스크립트 실행 권한 부여 및 Crontab 등록
# 매월 1일 3시에 파이썬 스크립트 실행
# 파일에 쓰지 않고, Docker 표준 출력(stdout)과 표준 에러(stderr)로 보냄
# > /proc/1/fd/1 : 표준 출력을 Docker 로그로
# 2> /proc/1/fd/2 : 에러 출력을 Docker 에러 로그로
RUN chmod +x /usr/local/bin/certbot_renew.py \
    && \
    echo "0 3 1 * * * root /usr/bin/python3 /usr/local/bin/certbot_renew.py > /proc/1/fd/1 2>/proc/1/fd/2" > /etc/cron.d/certbot-renew

# 4. Cron & Nginx 실행
CMD ["sh", "-c", "cron && nginx -g 'daemon off;'"]
