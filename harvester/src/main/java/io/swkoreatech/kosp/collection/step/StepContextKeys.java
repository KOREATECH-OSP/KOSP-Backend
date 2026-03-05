package io.swkoreatech.kosp.collection.step;

/**
 * Spring Batch 스텝 간 데이터 교환을 위한 ExecutionContext 키 상수 인터페이스.
 *
 * <p>Spring Batch의 ExecutionContext에서 데이터를 저장하고 조회하기 위한
 * 표준화된 키를 정의한다. 상수를 사용하여 매직 스트링을 방지하고
 * 배치 작업 파이프라인 전체에서 일관성을 보장한다.
 *
 * <p>사용 예시:
 * <pre>
 * // 컨텍스트에 쓰기
 * context.putString(StepContextKeys.GITHUB_LOGIN, login);
 * context.putString(StepContextKeys.GITHUB_TOKEN, token);
 *
 * // 컨텍스트에서 읽기
 * String login = context.getString(StepContextKeys.GITHUB_LOGIN);
 * </pre>
 *
 * @see org.springframework.batch.item.ExecutionContext
 */
public interface StepContextKeys {

    String GITHUB_LOGIN = "githubLogin";

    String GITHUB_TOKEN = "githubToken";

    String GITHUB_NODE_ID = "githubNodeId";

    String DISCOVERED_REPOS = "discoveredRepos";
}
