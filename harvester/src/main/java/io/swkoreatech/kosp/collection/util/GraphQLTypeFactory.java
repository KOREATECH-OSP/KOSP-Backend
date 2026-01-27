package io.swkoreatech.kosp.collection.util;

import io.swkoreatech.kosp.client.dto.GraphQLResponse;

/**
 * Utility factory for creating typed GraphQL response classes.
 * 
 * <p>This class provides a generic method to create Class references for GraphQL response
 * deserialization, handling Java's type erasure through unchecked casting.
 */
public final class GraphQLTypeFactory {

    private GraphQLTypeFactory() {
        throw new AssertionError("Utility class");
    }

    /**
     * Creates a typed Class reference for GraphQL response deserialization.
     * 
     * <p>This method performs an unchecked cast due to Java's type erasure.
     * The cast is safe as long as the GraphQL response matches the expected type T.
     *
     * @param <T> the expected response data type
     * @return the typed GraphQLResponse class
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<GraphQLResponse<T>> responseType() {
        return (Class<GraphQLResponse<T>>) (Class<?>) GraphQLResponse.class;
    }
}
