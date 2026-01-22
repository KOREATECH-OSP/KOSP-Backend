package io.swkoreatech.kosp.harvester.client.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GraphQLResponse<T> {

    private T data;
    private List<Map<String, Object>> errors;

    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }
}
