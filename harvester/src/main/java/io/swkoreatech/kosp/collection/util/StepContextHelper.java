package io.swkoreatech.kosp.collection.util;

import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.item.ExecutionContext;

/**
 * Spring Batch {@link ChunkContext}에서 데이터를 추출하기 위한 유틸리티 클래스.
 *
 * <p>실행 컨텍스트(ExecutionContext)와 잡 파라미터에 접근하기 위한
 * 정적 헬퍼 메서드를 제공한다.
 */
public final class StepContextHelper {

    private StepContextHelper() {
        throw new AssertionError("Utility class");
    }

    /**
     * ChunkContext에서 ExecutionContext를 추출한다.
     *
     * @param chunkContext 청크 컨텍스트
     * @return 실행 컨텍스트
     */
    public static ExecutionContext getExecutionContext(ChunkContext chunkContext) {
        return chunkContext.getStepContext()
            .getStepExecution()
            .getJobExecution()
            .getExecutionContext();
    }

    /**
     * 잡 파라미터에서 userId를 추출한다.
     *
     * @param chunkContext 청크 컨텍스트
     * @return 사용자 ID
     */
    public static Long extractUserId(ChunkContext chunkContext) {
        return chunkContext.getStepContext()
            .getStepExecution()
            .getJobParameters()
            .getLong("userId");
    }

    /**
     * 실행 컨텍스트에서 키에 해당하는 문자열 값을 추출한다.
     *
     * @param chunkContext 청크 컨텍스트
     * @param key          추출할 키
     * @return 문자열 값, 존재하지 않으면 null
     */
    public static String extractString(ChunkContext chunkContext, String key) {
        ExecutionContext context = getExecutionContext(chunkContext);
        Object value = context.get(key);
        if (value instanceof String) {
            return (String)value;
        }
        return null;
    }

    /**
     * 실행 컨텍스트에 문자열 값을 저장한다.
     *
     * @param chunkContext 청크 컨텍스트
     * @param key          저장할 키
     * @param value        문자열 값
     */
    public static void putString(ChunkContext chunkContext, String key, String value) {
        ExecutionContext context = getExecutionContext(chunkContext);
        context.putString(key, value);
    }
}
