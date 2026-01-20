package kr.ac.koreatech.sw.kosp.domain.admin.point.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import kr.ac.koreatech.sw.kosp.domain.admin.point.dto.request.PointTransactionRequest;
import kr.ac.koreatech.sw.kosp.domain.admin.point.dto.response.PointHistoryResponse;
import kr.ac.koreatech.sw.kosp.domain.point.event.PointChangeEvent;
import kr.ac.koreatech.sw.kosp.domain.point.model.PointSource;
import kr.ac.koreatech.sw.kosp.domain.point.model.PointTransaction;
import kr.ac.koreatech.sw.kosp.domain.point.model.TransactionType;
import kr.ac.koreatech.sw.kosp.domain.point.repository.PointTransactionRepository;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.domain.user.repository.UserRepository;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminPointService 단위 테스트")
class AdminPointServiceTest {

    @InjectMocks
    private AdminPointService adminPointService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PointTransactionRepository pointTransactionRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private User createUser(Long id, Integer point) {
        User user = User.builder()
            .name("테스트유저")
            .kutId("2024" + id)
            .kutEmail("test" + id + "@koreatech.ac.kr")
            .password("encoded_password")
            .roles(new HashSet<>())
            .build();
        ReflectionTestUtils.setField(user, "id", id);
        ReflectionTestUtils.setField(user, "point", point);
        return user;
    }

    private PointTransaction createTransaction(Long id, User user, Integer amount, PointSource source) {
        PointTransaction transaction = PointTransaction.builder()
            .user(user)
            .amount(amount)
            .type(amount > 0 ? TransactionType.GRANT : TransactionType.DEDUCT)
            .source(source)
            .reason("테스트 사유")
            .balanceAfter(user.getPoint() + amount)
            .build();
        ReflectionTestUtils.setField(transaction, "id", id);
        ReflectionTestUtils.setField(transaction, "createdAt", java.time.LocalDateTime.now());
        return transaction;
    }

    @Nested
    @DisplayName("changePoint 메서드")
    class ChangePointTest {

        @Test
        @DisplayName("포인트 변경 이벤트를 발행한다")
        void publishesPointChangeEvent() {
            // given
            User user = createUser(1L, 100);
            given(userRepository.getById(1L)).willReturn(user);
            PointTransactionRequest request = new PointTransactionRequest(50, "테스트 지급");

            // when
            adminPointService.changePoint(1L, request);

            // then
            then(eventPublisher).should().publishEvent(argThat((PointChangeEvent event) ->
                event.user().equals(user) &&
                event.amount().equals(50) &&
                event.reason().equals("테스트 지급") &&
                event.source() == PointSource.ADMIN
            ));
        }

        @Test
        @DisplayName("음수 포인트로 차감 이벤트를 발행한다")
        void publishesDeductEvent() {
            // given
            User user = createUser(1L, 100);
            given(userRepository.getById(1L)).willReturn(user);
            PointTransactionRequest request = new PointTransactionRequest(-30, "테스트 차감");

            // when
            adminPointService.changePoint(1L, request);

            // then
            then(eventPublisher).should().publishEvent(argThat((PointChangeEvent event) ->
                event.amount().equals(-30) &&
                event.source() == PointSource.ADMIN
            ));
        }

        @Test
        @DisplayName("존재하지 않는 사용자는 예외가 발생한다")
        void throwsException_whenUserNotFound() {
            // given
            given(userRepository.getById(999L)).willThrow(GlobalException.class);
            PointTransactionRequest request = new PointTransactionRequest(50, "테스트");

            // when & then
            assertThatThrownBy(() -> adminPointService.changePoint(999L, request))
                .isInstanceOf(GlobalException.class);
        }
    }

    @Nested
    @DisplayName("getPointHistory 메서드")
    class GetPointHistoryTest {

        @Test
        @DisplayName("포인트 거래 내역을 조회한다")
        void returnsPointHistory() {
            // given
            User user = createUser(1L, 150);
            PointTransaction tx1 = createTransaction(1L, user, 100, PointSource.ADMIN);
            PointTransaction tx2 = createTransaction(2L, user, 50, PointSource.CHALLENGE);
            Pageable pageable = PageRequest.of(0, 10);
            Page<PointTransaction> page = new PageImpl<>(List.of(tx1, tx2), pageable, 2);

            given(userRepository.getById(1L)).willReturn(user);
            given(pointTransactionRepository.findByUserOrderByCreatedAtDesc(user, pageable)).willReturn(page);

            // when
            PointHistoryResponse result = adminPointService.getPointHistory(1L, pageable);

            // then
            assertThat(result.userId()).isEqualTo(1L);
            assertThat(result.currentBalance()).isEqualTo(150);
            assertThat(result.transactions()).hasSize(2);
            assertThat(result.totalElements()).isEqualTo(2);
        }

        @Test
        @DisplayName("거래 내역이 없으면 빈 목록을 반환한다")
        void returnsEmptyList_whenNoTransactions() {
            // given
            User user = createUser(1L, 0);
            Pageable pageable = PageRequest.of(0, 10);
            Page<PointTransaction> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            given(userRepository.getById(1L)).willReturn(user);
            given(pointTransactionRepository.findByUserOrderByCreatedAtDesc(user, pageable)).willReturn(emptyPage);

            // when
            PointHistoryResponse result = adminPointService.getPointHistory(1L, pageable);

            // then
            assertThat(result.transactions()).isEmpty();
            assertThat(result.totalElements()).isEqualTo(0);
        }

        @Test
        @DisplayName("존재하지 않는 사용자는 예외가 발생한다")
        void throwsException_whenUserNotFound() {
            // given
            given(userRepository.getById(999L)).willThrow(GlobalException.class);
            Pageable pageable = PageRequest.of(0, 10);

            // when & then
            assertThatThrownBy(() -> adminPointService.getPointHistory(999L, pageable))
                .isInstanceOf(GlobalException.class);
        }
    }
}
