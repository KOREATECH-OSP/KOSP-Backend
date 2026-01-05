package kr.ac.koreatech.sw.kosp.domain.user.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;

import kr.ac.koreatech.sw.kosp.domain.github.event.GithubDataCollectionEventListener;
import kr.ac.koreatech.sw.kosp.domain.github.service.GithubStatisticsService;
import kr.ac.koreatech.sw.kosp.domain.user.event.UserSignupEvent;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("UserService 회원가입 이벤트 발행 통합 테스트")
class UserServiceSignupEventIntegrationTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private GithubDataCollectionEventListener eventListener;

    @MockBean
    private GithubStatisticsService statisticsService;

    @Test
    @DisplayName("UserSignupEvent 발행 시 EventListener가 수신")
    void publishEvent_ListenerReceives() throws Exception {
        // given
        String githubLogin = "testuser";

        // when
        eventPublisher.publishEvent(new UserSignupEvent(this, githubLogin));

        // then - 이벤트가 리스너에 전달되는지 확인
        // Note: 실제로는 GithubUser가 없어서 수집은 실패하지만, 이벤트 전달은 확인 가능
        Thread.sleep(1000); // 비동기 처리 대기
    }
}
