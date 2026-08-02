package io.swkoreatech.kosp.domain.resume.dto.request;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * 문자열 목록 필드 역직렬화기 (기술 스택, 개발 직무 등).
 *
 * <p>저장 포맷의 기준은 문자열 배열({@code ["Java","Spring"]})이다.
 * 다만 과거 버전에서는 콤마로 구분한 단일 문자열({@code "Java, Spring"})이나
 * 단일 값으로 저장된 이력서가 존재하므로, 두 형태를 모두 받아 배열로 정규화한다.
 * 이렇게 하지 않으면 기존 데이터를 불러와 다시 저장할 때 400 이 발생해
 * 사용자가 저장을 못 하게 된다.</p>
 */
public class StringListDeserializer extends JsonDeserializer<List<String>> {

    /** {@inheritDoc} */
    @Override
    public List<String> deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonToken token = parser.currentToken();

        if (token == JsonToken.VALUE_NULL) {
            return List.of();
        }

        // 과거 포맷: "Java, Spring" → ["Java", "Spring"]
        if (token == JsonToken.VALUE_STRING) {
            return splitCommaSeparated(parser.getText());
        }

        // 표준 포맷: ["Java", "Spring"]
        if (token == JsonToken.START_ARRAY) {
            List<String> values = new ArrayList<>();
            while (parser.nextToken() != JsonToken.END_ARRAY) {
                String value = parser.getValueAsString();
                if (value != null && !value.isBlank()) {
                    values.add(value.trim());
                }
            }
            return List.copyOf(values);
        }

        return List.of();
    }

    /**
     * JSON 에 {@code null} 이 명시된 경우 Jackson 은 {@link #deserialize} 대신 이 메서드를 호출한다.
     * 빈 목록으로 흡수해 저장이 실패하지 않게 한다.
     */
    @Override
    public List<String> getNullValue(DeserializationContext context) {
        return List.of();
    }

    private List<String> splitCommaSeparated(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return Arrays.stream(raw.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toList();
    }
}
