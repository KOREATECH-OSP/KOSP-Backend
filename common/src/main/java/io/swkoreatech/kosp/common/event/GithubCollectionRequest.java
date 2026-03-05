package io.swkoreatech.kosp.common.event;

/**
 * GitHub 활동 데이터 수집 요청 이벤트.
 *
 * <p>특정 사용자의 GitHub 활동 정보를 수집하도록 요청할 때 사용된다.</p>
 *
 * @param userId 수집 대상 사용자 ID
 */
public record GithubCollectionRequest(
    Long userId
) {
}
