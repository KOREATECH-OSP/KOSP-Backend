package kr.ac.koreatech.sw.kosp.domain.point.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.util.HashSet;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import kr.ac.koreatech.sw.kosp.domain.point.model.PointSource;
import kr.ac.koreatech.sw.kosp.domain.point.model.PointTransaction;
import kr.ac.koreatech.sw.kosp.domain.point.repository.PointTransactionRepository;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;

@ExtendWith(MockitoExtension.class)
@DisplayName("PointService 단위 테스트")
class PointServiceTest {

    @InjectMocks
    private PointService pointService;

    @Mock
    private PointTransactionRepository pointTransactionRepository;

    private User createUser(Long id, Integer initialPoint) {
        User user = User.builder()
            .name("테스트유저")
            .kutId("2024" + id)
            .kutEmail("test" + id + "@koreatech.ac.kr")
            .password("encoded_password")
            .roles(new HashSet<>())
            .build();
        ReflectionTestUtils.setField(user, "id", id);
        ReflectionTestUtils.setField(user, "point", initialPoint);
        return user;
    }

    @Nested
    @DisplayName("changePoint 메서드")
    class ChangePointTest {

        @Test
        @DisplayName("포인트 지급 시 사용자 포인트가 증가한다")
        void grantsPointSuccessfully() {
            // given
            User user = createUser(1L, 100);

            // when
            pointService.changePoint(user, 50, "테스트 지급", PointSource.ADMIN);

            // then
            assertThat(user.getPoint()).isEqualTo(150);
            then(pointTransactionRepository).should().save(any(PointTransaction.class));
        }

        @Test
        @DisplayName("포인트 차감 시 사용자 포인트가 감소한다")
        void deductsPointSuccessfully() {
            // given
            User user = createUser(1L, 100);

            // when
            pointService.changePoint(user, -30, "테스트 차감", PointSource.ADMIN);

            // then
            assertThat(user.getPoint()).isEqualTo(70);
            then(pointTransactionRepository).should().save(any(PointTransaction.class));
        }

        @Test
        @DisplayName("amount가 0이면 예외가 발생한다")
        void throwsException_whenAmountIsZero() {
            // given
            User user = createUser(1L, 100);

            // when & then
            assertThatThrownBy(() -> pointService.changePoint(user, 0, "테스트", PointSource.ADMIN))
                .isInstanceOf(GlobalException.class);

            then(pointTransactionRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("amount가 null이면 예외가 발생한다")
        void throwsException_whenAmountIsNull() {
            // given
            User user = createUser(1L, 100);

            // when & then
            assertThatThrownBy(() -> pointService.changePoint(user, null, "테스트", PointSource.ADMIN))
                .isInstanceOf(GlobalException.class);

            then(pointTransactionRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("잔액보다 큰 금액을 차감하면 예외가 발생한다")
        void throwsException_whenInsufficientBalance() {
            // given
            User user = createUser(1L, 50);

            // when & then
            assertThatThrownBy(() -> pointService.changePoint(user, -100, "테스트", PointSource.ADMIN))
                .isInstanceOf(GlobalException.class);

            assertThat(user.getPoint()).isEqualTo(50);
            then(pointTransactionRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("잔액과 동일한 금액 차감은 성공한다")
        void deductsExactBalance() {
            // given
            User user = createUser(1L, 100);

            // when
            pointService.changePoint(user, -100, "전액 차감", PointSource.ADMIN);

            // then
            assertThat(user.getPoint()).isEqualTo(0);
            then(pointTransactionRepository).should().save(any(PointTransaction.class));
        }

        @Test
        @DisplayName("CHALLENGE 소스로 포인트를 지급할 수 있다")
        void grantsPointWithChallengeSource() {
            // given
            User user = createUser(1L, 0);

            // when
            pointService.changePoint(user, 200, "챌린지 달성", PointSource.CHALLENGE);

            // then
            assertThat(user.getPoint()).isEqualTo(200);
            then(pointTransactionRepository).should().save(any(PointTransaction.class));
        }

        @Test
        @DisplayName("ACTIVITY 소스로 포인트를 지급할 수 있다")
        void grantsPointWithActivitySource() {
            // given
            User user = createUser(1L, 0);

            // when
            pointService.changePoint(user, 50, "활동 보상", PointSource.ACTIVITY);

            // then
            assertThat(user.getPoint()).isEqualTo(50);
            then(pointTransactionRepository).should().save(any(PointTransaction.class));
        }
    }
}
