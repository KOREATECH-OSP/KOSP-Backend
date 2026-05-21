"""
기능명세서.md → CSV 변환 스크립트 (원본 Google Sheets 양식 기준)
실행: python3 md_to_csv.py
결과: 기능명세서.csv
"""

import csv
import re

MD_FILE = "기능명세서.md"
CSV_FILE = "기능명세서.csv"

# 원본 Google Sheets 컬럼 순서
HEADER = ["기능 ID", "대분류", "소분류", "기능명", "사용자 유형",
          "기능 상세(설명)", "입력", "출력", "예외/에러", "우선순위", "상태", "비고"]

def is_separator(cells):
    return all(re.fullmatch(r":?-+:?", c) for c in cells if c)

def parse_tables(filepath):
    rows = []
    header_done = False

    with open(filepath, encoding="utf-8") as f:
        lines = f.readlines()

    for line in lines:
        stripped = line.strip()

        if not stripped.startswith("|"):
            header_done = False
            continue

        cells = [c.strip() for c in stripped.split("|")[1:-1]]

        if is_separator(cells):
            header_done = True
            continue

        if not header_done:
            continue  # 헤더 행 스킵

        # 기능 ID가 있는 데이터 행만 수집
        if cells and cells[0].strip():
            # <br> → 줄바꿈 변환
            cells = [c.replace("<br>", "\n") for c in cells]
            rows.append(cells)

    return rows

def main():
    rows = parse_tables(MD_FILE)

    if not rows:
        print("파싱된 데이터가 없습니다.")
        return

    with open(CSV_FILE, "w", newline="", encoding="utf-8-sig") as f:
        writer = csv.writer(f)
        writer.writerow(HEADER)
        for row in rows:
            padded = row + [""] * (len(HEADER) - len(row))
            writer.writerow(padded[:len(HEADER)])

    print(f"✅ 변환 완료: {CSV_FILE} ({len(rows)}행)")
    print(f"컬럼: {', '.join(HEADER)}")
    print("Google Sheets → 파일 > 가져오기 > 업로드로 불러오세요.")

if __name__ == "__main__":
    main()
