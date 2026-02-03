#!/usr/bin/env python3

import os
import subprocess
import json
from datetime import datetime

# --- [설정] ---
# 슬랙 웹훅 URL, 알림을 보낼 채널 ID, 서버 이름을 설정합니다.
WEBHOOK_URL = "https://hooks.slack.com/services/..."
CHANNEL_ID = "ABCDEFGHIJK" # 채널 ID
SERVER_NAME = "*[KOSP 스테이지]*"
# ----------------

def send_slack_message(blocks):
    """지정된 블록 배열을 사용하여 슬랙 메시지를 전송합니다."""
    payload = {
        "channel": CHANNEL_ID,
        "blocks": blocks
    }
    # curl을 subprocess로 호출하여 데이터를 표준 입력으로 안전하게 전달합니다.
    subprocess.run(
        [
            "curl", "-s", "-X", "POST", "--data-urlencode",
            "-H", "Content-type: application/json",
            "--data", json.dumps(payload),
            WEBHOOK_URL
        ],
        check=True,
        stdout=subprocess.DEVNULL
    )

def create_rich_text_list_block(title, emoji, domain_list):
    """도메인 목록으로 rich_text_list 블록을 생성합니다."""
    # 각 도메인을 rich_text_section 객체로 변환합니다.
    list_elements = [
        {"type": "rich_text_section", "elements": [{"type": "text", "text": domain}]}
        for domain in domain_list
    ]

    # 최종 블록 구조를 조립합니다.
    return [
        {"type": "section", "text": {"type": "mrkdwn", "text": f"{emoji} *{title}*"}},
        {"type": "rich_text", "elements": [{"type": "rich_text_list", "style": "bullet", "elements": list_elements}]}
    ]

def main():
    """메인 실행 함수"""
    # 1. 작업 시작 알림
    start_message = f"{SERVER_NAME} :gear: Certbot 갱신 cron 작업을 시작합니다. ({datetime.now().strftime('%Y-%m-%d %H:%M:%S')})"
    send_slack_message([{"type": "section", "text": {"type": "mrkdwn", "text": start_message}}])

    # 2. Certbot 실행
    result = subprocess.run(
        ["certbot", "renew", "--post-hook", "nginx -s reload"],
        capture_output=True, text=True
    )
    exit_code = result.returncode
    output = result.stdout + result.stderr

    # 3. 결과 분석
    renewed_domains = [line.split('/')[-2] for line in output.splitlines() if "(success)" in line]
    skipped_domains = [line.split('/')[-2] for line in output.splitlines() if "(skipped)" in line]
    failed_domains = [line.split('/')[-2] for line in output.splitlines() if "(failure)" in line]

    # 4. 블록 킷 메시지 조립
    blocks = []

    # [제목 블록]
    title = f"{SERVER_NAME} :heavy_check_mark: Certbot 갱신 작업 결과"
    blocks.append({"type": "section", "text": {"type": "mrkdwn", "text": title}})

    # [각 섹션 블록 생성]
    if renewed_domains:
        blocks.extend(create_rich_text_list_block("갱신 성공", ":sparkles:", renewed_domains))
    if skipped_domains:
        blocks.extend(create_rich_text_list_block("갱신 건너뜀", ":fast_forward:", skipped_domains))
    if failed_domains:
        blocks.extend(create_rich_text_list_block("갱신 실패", ":warning:", failed_domains))

    # 5. 최종 리포트 전송
    if renewed_domains or skipped_domains or failed_domains:
        send_slack_message(blocks)
    else:
        # 처리할 인증서가 없는 경우
        no_certs_message = f"{SERVER_NAME} :information_source: Certbot: 처리할 인증서가 없거나 아무 작업도 수행되지 않았습니다."
        send_slack_message([{"type": "section", "text": {"type": "mrkdwn", "text": no_certs_message}}])

if __name__ == "__main__":
    main()
