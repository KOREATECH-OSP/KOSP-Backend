package kr.ac.koreatech.sw.kosp.domain.github.queue.model;

public enum CollectionJobType {
    USER_BASIC,      // 사용자 기본 정보 수집
    USER_EVENTS,     // 사용자 이벤트 수집
    REPO_ISSUES,     // 레포지토리 이슈 수집
    REPO_PRS,        // 레포지토리 PR 수집
    REPO_COMMITS,    // 레포지토리 커밋 수집
    TIMELINE         // 사용자 Timeline 스크래핑 (Issue/PR/Commit 개수)
}
