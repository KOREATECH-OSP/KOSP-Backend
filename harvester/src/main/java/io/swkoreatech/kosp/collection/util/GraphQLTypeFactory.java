package io.swkoreatech.kosp.collection.util;

import io.swkoreatech.kosp.client.dto.GraphQLResponse;

/**
 * 타입이 지정된 GraphQL 응답 클래스를 생성하는 유틸리티 팩토리.
 *
 * <p>GraphQL 응답 역직렬화를 위한 Class 참조를 생성하는 제네릭 메서드를 제공하며,
 * Java의 타입 소거(type erasure)를 unchecked 캐스팅으로 처리한다.
 */
public final class GraphQLTypeFactory {

    private GraphQLTypeFactory() {
        throw new AssertionError("Utility class");
    }

    /**
     * GraphQL 응답 역직렬화를 위한 타입이 지정된 Class 참조를 생성한다.
     *
     * <p>Java의 타입 소거로 인해 unchecked 캐스팅을 수행한다.
     * GraphQL 응답이 예상 타입 T와 일치하는 한 해당 캐스팅은 안전하다.
     *
     * @param <T> 예상되는 응답 데이터 타입
     * @return 타입이 지정된 GraphQLResponse 클래스
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<GraphQLResponse<T>> responseType() {
        return (Class<GraphQLResponse<T>>)(Class<?>)GraphQLResponse.class;
    }
}
