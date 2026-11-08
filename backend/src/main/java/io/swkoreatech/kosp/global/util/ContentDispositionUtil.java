package io.swkoreatech.kosp.global.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * {@code Content-Disposition} 헤더 생성 유틸.
 *
 * <p>파일명에 한글이 들어가므로 RFC 5987 의 {@code filename*=UTF-8''} 형식으로 인코딩한다.
 * 인코딩 방식은 {@code S3StorageClient#getPresignedDownloadUrl} 과 동일하게 맞춘다.</p>
 *
 * <p>파일명은 사용자 자유 입력(이력서 제목)에서 오므로 헤더 인젝션 경로가 된다.
 * CR/LF 를 포함한 제어문자와 경로 구분자를 제거한 뒤 인코딩한다.</p>
 */
public final class ContentDispositionUtil {

    /** 파일명 최대 길이(확장자 제외). 너무 길면 일부 브라우저·파일시스템에서 잘린다. */
    private static final int MAX_BASE_NAME_LENGTH = 120;

    private ContentDispositionUtil() {
    }

    /**
     * 첨부파일 다운로드용 {@code Content-Disposition} 헤더 값을 만든다.
     *
     * @param fileName 확장자를 포함한 파일명 (예: {@code 내 이력서.hwpx})
     * @param fallback 파일명이 비었을 때 사용할 기본 파일명
     * @return {@code attachment; filename*=UTF-8''...} 형식의 헤더 값
     */
    public static String attachment(String fileName, String fallback) {
        String safe = sanitize(fileName);
        if (safe.isEmpty()) {
            safe = sanitize(fallback);
        }
        if (safe.isEmpty()) {
            return "attachment";
        }
        String encoded = URLEncoder.encode(safe, StandardCharsets.UTF_8).replace("+", "%20");
        return "attachment; filename*=UTF-8''" + encoded;
    }

    /**
     * 파일명에서 헤더·파일시스템에 위험한 문자를 제거하고 길이를 제한한다.
     *
     * <p>제거 대상: 모든 제어문자(CR/LF 포함), 경로 구분자({@code / \}), 윈도 예약문자
     * ({@code : * ? " < > |}). 제거 후 앞뒤 공백과 마침표를 다듬는다.</p>
     */
    private static String sanitize(String raw) {
        if (raw == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder(raw.length());
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c < 0x20 || c == 0x7F) {
                continue; // 제어문자 (CR/LF 포함) — 헤더 인젝션 차단
            }
            if (c == '/' || c == '\\' || c == ':' || c == '*'
                || c == '?' || c == '"' || c == '<' || c == '>' || c == '|') {
                continue; // 경로 구분자 · 윈도 예약문자
            }
            sb.append(c);
        }

        String cleaned = sb.toString().trim();

        // 확장자를 살리기 위해 마지막 '.' 앞부분만 길이를 제한한다.
        int dot = cleaned.lastIndexOf('.');
        if (dot > 0) {
            String base = cleaned.substring(0, dot);
            String ext = cleaned.substring(dot);
            if (base.length() > MAX_BASE_NAME_LENGTH) {
                base = base.substring(0, MAX_BASE_NAME_LENGTH);
            }
            cleaned = base.trim() + ext;
        } else if (cleaned.length() > MAX_BASE_NAME_LENGTH) {
            cleaned = cleaned.substring(0, MAX_BASE_NAME_LENGTH).trim();
        }

        // 앞뒤 마침표만 남는 이름(".", "..")은 파일명으로 쓸 수 없다.
        if (cleaned.chars().allMatch(c -> c == '.')) {
            return "";
        }
        return cleaned;
    }
}
