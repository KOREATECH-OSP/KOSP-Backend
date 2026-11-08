package io.swkoreatech.kosp.domain.resume.export;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * {@code resume_data}(JSONB) 를 방어적으로 읽는 접근자.
 *
 * <p>{@code ResumeSaveRequest} 로 역직렬화하지 않는 이유는, 저장 시점에 서버 검증이 없어
 * ({@code ResumeSaveRequest} 에 제약 어노테이션이 0개) 타입이 어긋난 값이 실제로 저장될 수
 * 있기 때문이다. 예를 들어 {@code {"links": "oops"}} 를 {@code List<LinkItem>} 으로 바인딩하면
 * {@code MismatchedInputException} 이 나고, 전역 핸들러에 해당 핸들러가 없어 500 이 된다.</p>
 *
 * <p>따라서 모든 접근자는 <b>어떤 입력에도 예외를 던지지 않고</b> 빈 값으로 흡수한다.</p>
 */
public final class ResumeDataReader {

    private final JsonNode root;

    private ResumeDataReader(JsonNode root) {
        this.root = root;
    }

    /**
     * 파싱된 JSON 으로 리더를 만든다.
     *
     * @param root {@code resume_data} 루트 노드. null 이거나 객체가 아니면 빈 리더가 된다.
     */
    public static ResumeDataReader of(JsonNode root) {
        if (root == null || !root.isObject()) {
            return new ResumeDataReader(null);
        }
        return new ResumeDataReader(root);
    }

    /**
     * 문자열 필드를 읽는다. 없거나 문자열이 아니면 빈 문자열.
     *
     * <p>숫자·불리언은 표시 목적상 문자열로 변환해 살린다(예: {@code "date": 2026}).</p>
     */
    public String text(String field) {
        return text(root, field);
    }

    /** 지정한 노드에서 문자열 필드를 읽는다. */
    public static String text(JsonNode node, String field) {
        if (node == null || !node.isObject()) {
            return "";
        }
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return "";
        }
        if (value.isTextual()) {
            return value.textValue();
        }
        if (value.isNumber() || value.isBoolean()) {
            return value.asText();
        }
        // 배열·객체는 표시할 수 없으므로 버린다.
        return "";
    }

    /**
     * 불리언 필드를 읽는다. {@code true} 일 때만 true.
     */
    public boolean isTrue(String field) {
        if (root == null) {
            return false;
        }
        JsonNode value = root.get(field);
        return value != null && value.isBoolean() && value.booleanValue();
    }

    /**
     * 객체 배열 필드를 읽는다.
     *
     * <p>배열이 아니면 빈 목록. 배열이더라도 객체가 아닌 요소(문자열·숫자·null)는 건너뛴다.</p>
     */
    public List<JsonNode> objects(String field) {
        if (root == null) {
            return List.of();
        }
        JsonNode array = root.get(field);
        if (array == null || !array.isArray()) {
            return List.of();
        }
        List<JsonNode> result = new ArrayList<>();
        for (JsonNode element : array) {
            if (element != null && element.isObject()) {
                result.add(element);
            }
        }
        return result;
    }

    /**
     * 문자열 목록 필드를 읽는다.
     *
     * <p>과거 이력서는 {@code "Java, Spring"} 같은 콤마 구분 단일 문자열로 저장돼 있다
     * ({@code StringListDeserializer} 와 같은 사정). 두 형태를 모두 흡수한다.</p>
     */
    public List<String> strings(String field) {
        return strings(root, field);
    }

    /** 지정한 노드에서 문자열 목록을 읽는다. */
    public static List<String> strings(JsonNode node, String field) {
        if (node == null || !node.isObject()) {
            return List.of();
        }
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return List.of();
        }

        if (value.isTextual()) {
            return Arrays.stream(value.textValue().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        }

        if (value.isArray()) {
            List<String> result = new ArrayList<>();
            for (JsonNode element : value) {
                if (element == null || element.isNull()) {
                    continue;
                }
                String asText = element.isTextual() ? element.textValue()
                    : (element.isNumber() || element.isBoolean()) ? element.asText() : null;
                if (asText != null && !asText.isBlank()) {
                    result.add(asText.trim());
                }
            }
            return result;
        }

        return List.of();
    }

    /**
     * 섹션을 출력해야 하는지 판정한다.
     *
     * <p>프론트 {@code ResumeReadOnlyView} 의 규칙과 동일하다:
     * {@code visibleSections} 가 없으면 전체 표시, 있으면 값이 {@code false} 가 아닌 것만 표시.</p>
     *
     * @param sectionId {@code sec-basic}, {@code sec-projects} 등
     */
    public boolean isSectionVisible(String sectionId) {
        if (root == null) {
            return true;
        }
        JsonNode visible = root.get("visibleSections");
        if (visible == null || !visible.isObject()) {
            return true; // undefined 이면 전체 표시
        }
        JsonNode flag = visible.get(sectionId);
        if (flag == null) {
            return true;
        }
        return !(flag.isBoolean() && !flag.booleanValue());
    }

    /**
     * 기간 표시 문자열을 만든다.
     *
     * <p>프론트와 동일하게 {@code startDate}/{@code endDate} 를 우선하고,
     * 없으면 레거시 자유 입력 {@code period} 를 그대로 쓴다.</p>
     */
    public static String period(JsonNode item) {
        String start = text(item, "startDate");
        String end = text(item, "endDate");

        if (!start.isBlank() && !end.isBlank()) {
            return start + " ~ " + end;
        }
        if (!start.isBlank()) {
            return start + " ~ ";
        }
        if (!end.isBlank()) {
            return " ~ " + end;
        }
        return text(item, "period");
    }
}
