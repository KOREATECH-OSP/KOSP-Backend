package io.swkoreatech.kosp.collection.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.swkoreatech.kosp.client.dto.GraphQLResponse;

@DisplayName("GraphQLTypeFactory 단위 테스트")
class GraphQLTypeFactoryTest {

    @Test
    @DisplayName("responseType()은 GraphQLResponse 클래스를 반환한다")
    void responseType_returnsGraphQLResponseClass() {
        // when
        Class<GraphQLResponse<String>> result = GraphQLTypeFactory.responseType();

        // then
        assertThat(result).isEqualTo(GraphQLResponse.class);
    }
}
