package io.swkoreatech.kosp.domain.resume.export;

import java.util.ArrayList;
import java.util.List;

/**
 * hwpx 출력용 텍스트 정제기.
 *
 * <p>{@code resume_data} 는 서버 검증 없이 저장되므로(ResumeSaveRequest 에 제약 어노테이션이 없다)
 * 어떤 문자열이든 들어올 수 있다. 문서가 깨지지 않도록 아래를 보장한다.</p>
 *
 * <ol>
 *   <li><b>XML 1.0 금지 제어문자 제거</b> — hwpxlib 은 {@code & < >} 만 이스케이프하고
 *       제어문자는 그대로 내보낸다. U+0000 같은 문자가 남으면 XML 자체가 불정형이 된다.</li>
 *   <li><b>길이 상한</b> — 한 필드가 과도하게 길면 절단하되, <b>절단 사실을 본문에 남긴다</b>.
 *       조용히 자르면 사용자가 내용이 사라진 것을 알 수 없다.</li>
 *   <li><b>줄바꿈 분리</b> — hwpxlib 은 텍스트 노드의 {@code \n} 을 {@code \r\n} 으로 바꿀 뿐
 *       문단을 나누지 않는다. 한글 문단으로 분리하려면 호출측에서 잘라 줘야 한다.</li>
 * </ol>
 */
public final class HwpxTextSanitizer {

    /** 한 필드가 가질 수 있는 최대 문자 수. 초과분은 절단하고 표시를 남긴다. */
    public static final int MAX_FIELD_LENGTH = 20_000;

    /** 절단이 일어났음을 문서 본문에 드러내는 표시. */
    public static final String TRUNCATION_MARKER = " … (이하 생략 — 내용이 너무 길어 일부만 출력되었습니다)";

    /** 하나의 필드가 만들 수 있는 최대 문단 수. 줄바꿈 폭탄 방어. */
    public static final int MAX_PARAGRAPHS_PER_FIELD = 500;

    private HwpxTextSanitizer() {
    }

    /**
     * 문자열을 한 줄짜리 표시용 텍스트로 정제한다.
     *
     * <p>제어문자를 제거하고 줄바꿈·탭을 공백으로 바꾼 뒤 길이를 제한한다.
     * 제목·라벨처럼 여러 문단으로 나눌 필요가 없는 값에 쓴다.</p>
     *
     * @return 정제된 문자열 (입력이 null 이면 빈 문자열)
     */
    public static String singleLine(String raw) {
        if (raw == null) {
            return "";
        }
        String cleaned = removeIllegalChars(raw, true);
        return truncate(cleaned).text();
    }

    /**
     * 장문을 문단 목록으로 분리한다.
     *
     * <p>{@code \r\n}, {@code \r}, {@code \n} 을 모두 줄바꿈으로 인정하고 그 기준으로 나눈다.
     * 빈 줄은 유지하지 않는다(한글에서 빈 문단이 연속되면 지저분해진다).</p>
     *
     * @return 문단 목록 (내용이 없으면 빈 목록)
     */
    public static List<String> paragraphs(String raw) {
        if (raw == null) {
            return List.of();
        }

        // 제어문자를 제거하되 줄바꿈은 살려 둔다.
        String cleaned = removeIllegalChars(raw, false);
        Truncated truncated = truncate(cleaned);

        String normalized = truncated.text()
            .replace("\r\n", "\n")
            .replace('\r', '\n');

        List<String> result = new ArrayList<>();
        for (String line : normalized.split("\n", -1)) {
            String trimmed = line.strip();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
            if (result.size() >= MAX_PARAGRAPHS_PER_FIELD) {
                result.add(TRUNCATION_MARKER.strip());
                break;
            }
        }
        return result;
    }

    /**
     * 길이 상한을 적용한다. 초과하면 {@link #TRUNCATION_MARKER} 를 덧붙인다.
     *
     * @return 절단 여부와 결과 문자열
     */
    public static Truncated truncate(String raw) {
        if (raw == null) {
            return new Truncated("", false);
        }
        if (raw.length() <= MAX_FIELD_LENGTH) {
            return new Truncated(raw, false);
        }
        // 이모지 등 서로게이트 쌍 중간에서 자르면 깨진 문자가 남는다.
        int cut = MAX_FIELD_LENGTH;
        if (Character.isHighSurrogate(raw.charAt(cut - 1))) {
            cut--;
        }
        return new Truncated(raw.substring(0, cut) + TRUNCATION_MARKER, true);
    }

    /**
     * 입력이 길이 상한을 넘는지 여부만 확인한다 (로깅 판단용).
     */
    public static boolean exceedsLimit(String raw) {
        return raw != null && raw.length() > MAX_FIELD_LENGTH;
    }

    /**
     * XML 1.0 에서 허용되지 않는 문자를 제거한다.
     *
     * <p>허용: U+0009(TAB), U+000A(LF), U+000D(CR), U+0020~U+D7FF,
     * U+E000~U+FFFD, U+10000~U+10FFFF. 그 밖의 제어문자와 비문자는 버린다.
     * 이모지는 서로게이트 쌍으로 표현되며 U+D800~U+DFFF 범위이므로,
     * 코드포인트 단위로 순회해 온전한 쌍은 보존한다.</p>
     *
     * @param collapseBreaks true 면 TAB·LF·CR 을 공백으로 바꾼다 (한 줄 표시용)
     */
    private static String removeIllegalChars(String raw, boolean collapseBreaks) {
        StringBuilder sb = new StringBuilder(raw.length());
        raw.codePoints().forEach(cp -> {
            if (cp == 0x09 || cp == 0x0A || cp == 0x0D) {
                sb.append(collapseBreaks ? ' ' : (char) cp);
                return;
            }
            if (cp < 0x20) {
                return; // 그 밖의 C0 제어문자
            }
            if (cp == 0x7F || (cp >= 0x80 && cp <= 0x9F)) {
                return; // DEL 및 C1 제어문자
            }
            if (cp >= 0xD800 && cp <= 0xDFFF) {
                return; // 짝을 잃은 서로게이트 (codePoints() 는 온전한 쌍을 하나로 넘긴다)
            }
            if (cp == 0xFFFE || cp == 0xFFFF) {
                return; // 비문자
            }
            sb.appendCodePoint(cp);
        });
        return sb.toString();
    }

    /**
     * 절단 결과.
     *
     * @param text      결과 문자열 (절단됐으면 표시가 붙어 있다)
     * @param truncated 절단 발생 여부
     */
    public record Truncated(String text, boolean truncated) {
    }
}
