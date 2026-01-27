package io.swkoreatech.kosp.collection.util;

import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.item.ExecutionContext;

/**
 * Utility class for extracting data from Spring Batch ChunkContext.
 * Provides static helper methods to access execution context and job parameters.
 */
public final class StepContextHelper {

    private StepContextHelper() {
        throw new AssertionError("Utility class");
    }

    /**
     * Extracts ExecutionContext from ChunkContext.
     *
     * @param chunkContext the chunk context
     * @return the execution context
     */
    public static ExecutionContext getExecutionContext(ChunkContext chunkContext) {
        return chunkContext.getStepContext()
            .getStepExecution()
            .getJobExecution()
            .getExecutionContext();
    }

    /**
     * Extracts userId from job parameters.
     *
     * @param chunkContext the chunk context
     * @return the user ID
     */
    public static Long extractUserId(ChunkContext chunkContext) {
        return chunkContext.getStepContext()
            .getStepExecution()
            .getJobParameters()
            .getLong("userId");
    }

    /**
     * Extracts a string value from execution context by key.
     *
     * @param chunkContext the chunk context
     * @param key the key to extract
     * @return the string value, or null if not found
     */
    public static String extractString(ChunkContext chunkContext, String key) {
        ExecutionContext context = getExecutionContext(chunkContext);
        Object value = context.get(key);
        if (value instanceof String) {
            return (String) value;
        }
        return null;
    }

    /**
     * Puts a string value into execution context.
     *
     * @param chunkContext the chunk context
     * @param key the key to store
     * @param value the string value
     */
    public static void putString(ChunkContext chunkContext, String key, String value) {
        ExecutionContext context = getExecutionContext(chunkContext);
        context.putString(key, value);
    }
}
