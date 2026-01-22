package io.swkoreatech.kosp.harvester.client.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GraphQLResponse<T> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private Object data;
    private List<Map<String, Object>> errors;

    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }

    @SuppressWarnings("unchecked")
    public <R> R getDataAs(Class<R> type) {
        if (data == null) {
            return null;
        }
        if (type.isInstance(data)) {
            return (R) data;
        }
        return OBJECT_MAPPER.convertValue(data, type);
    }
}
