package io.swkoreatech.kosp.client.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.Getter;

/**
 * GitHub GraphQL API의 범용 응답 DTO.
 *
 * <p>GraphQL 응답의 data 필드와 errors 필드를 포함하며,
 * {@link #getDataAs(Class)} 메서드를 통해 타입 안전한 데이터 변환을 지원한다.
 *
 * @param <T> 응답 데이터 타입
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GraphQLResponse<T> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .registerModule(new JavaTimeModule());

    private Object data;
    private List<Map<String, Object>> errors;

    /**
     * 응답에 에러가 존재하는지 확인한다.
     *
     * @return 에러가 존재하면 true
     */
    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }

    /**
     * 응답 데이터를 지정된 타입으로 변환하여 반환한다.
     *
     * @param <R>  변환 대상 타입
     * @param type 변환할 클래스 타입
     * @return 변환된 데이터 객체, data가 null이면 null
     */
    @SuppressWarnings("unchecked")
    public <R> R getDataAs(Class<R> type) {
        if (data == null) {
            return null;
        }
        if (type.isInstance(data)) {
            return (R)data;
        }
        return OBJECT_MAPPER.convertValue(data, type);
    }
}
