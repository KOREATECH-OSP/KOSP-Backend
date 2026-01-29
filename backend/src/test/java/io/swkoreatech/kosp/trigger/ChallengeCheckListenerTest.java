package io.swkoreatech.kosp.trigger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import io.swkoreatech.kosp.domain.challenge.service.ChallengeEvaluator;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.domain.user.repository.UserRepository;
import io.swkoreatech.kosp.global.exception.GlobalException;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChallengeCheckListener 단위 테스트")
class ChallengeCheckListenerTest {

    @InjectMocks
    private ChallengeCheckListener listener;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ChallengeEvaluator challengeEvaluator;

    @Mock
    private SetOperations<String, String> setOperations;

    @Mock
    private StreamOperations<String, Object, Object> streamOperations;

    @BeforeEach
    void setUp() {
        given(redisTemplate.opsForSet()).willReturn(setOperations);
        given(redisTemplate.opsForStream()).willReturn(streamOperations);
    }

    private MapRecord<String, Object, Object> createMessage(String userId, String jobExecutionId) {
        Map<Object, Object> payload = new HashMap<>();
        payload.put("userId", userId);
        payload.put("jobExecutionId", jobExecutionId);
        payload.put("calculatedAt", "2026-01-29T10:00:00Z");

        return StreamRecords.newRecord()
            .in("kosp:challenge-check")
            .ofMap(payload)
            .withId(RecordId.autoGenerate());
    }

    @Nested
    @DisplayName("pollMessages 메서드")
    class PollMessagesTest {

        @Test
        @DisplayName("새로운 이벤트를 처리하고 챌린지 평가를 수행한다")
        void processesNewEvent() {
            MapRecord<String, Object, Object> message = createMessage("1", "100");
            User user = User.builder().name("테스터").kutId("2024001").kutEmail("test@koreatech.ac.kr").password("pw").build();

            given(streamOperations.read(any(Consumer.class), any(StreamOffset.class)))
                .willReturn(List.of(message));
            given(setOperations.add(any(), any())).willReturn(1L);
            given(userRepository.getById(1L)).willReturn(user);

            listener.pollMessages();

            verify(setOperations).add("kosp:processed-jobs", "100");
            verify(userRepository).getById(1L);
            verify(challengeEvaluator).evaluate(user);
            verify(streamOperations).acknowledge(any(), any(MapRecord.class));
        }

        @Test
        @DisplayName("중복된 jobExecutionId는 건너뛴다")
        void skipsDuplicateJobExecutionId() {
            MapRecord<String, Object, Object> message = createMessage("1", "100");

            given(streamOperations.read(any(Consumer.class), any(StreamOffset.class)))
                .willReturn(List.of(message));
            given(setOperations.add(any(), any())).willReturn(0L);

            listener.pollMessages();

            verify(setOperations).add("kosp:processed-jobs", "100");
            verify(userRepository, never()).getById(any());
            verify(challengeEvaluator, never()).evaluate(any());
            verify(streamOperations).acknowledge(any(), any(MapRecord.class));
        }

        @Test
        @DisplayName("사용자를 찾을 수 없어도 예외를 삼키고 로그만 남긴다")
        void handlesUserNotFoundGracefully() {
            MapRecord<String, Object, Object> message = createMessage("999", "100");

            given(streamOperations.read(any(Consumer.class), any(StreamOffset.class)))
                .willReturn(List.of(message));
            given(setOperations.add(any(), any())).willReturn(1L);
            given(userRepository.getById(999L)).willThrow(new GlobalException(null));

            listener.pollMessages();

            verify(challengeEvaluator, never()).evaluate(any());
            verify(streamOperations).acknowledge(any(), any(MapRecord.class));
        }

        @Test
        @DisplayName("evaluator에서 예외가 발생해도 메시지를 확인한다")
        void handlesEvaluatorExceptionGracefully() {
            MapRecord<String, Object, Object> message = createMessage("1", "100");
            User user = User.builder().name("테스터").kutId("2024001").kutEmail("test@koreatech.ac.kr").password("pw").build();

            given(streamOperations.read(any(Consumer.class), any(StreamOffset.class)))
                .willReturn(List.of(message));
            given(setOperations.add(any(), any())).willReturn(1L);
            given(userRepository.getById(1L)).willReturn(user);

            listener.pollMessages();

            verify(setOperations).add("kosp:processed-jobs", "100");
            verify(streamOperations).acknowledge(any(), any(MapRecord.class));
        }

        @Test
        @DisplayName("스트림에 메시지가 없으면 아무 작업도 수행하지 않는다")
        void doesNothingWhenNoMessages() {
            given(streamOperations.read(any(Consumer.class), any(StreamOffset.class)))
                .willReturn(null);

            listener.pollMessages();

            verify(setOperations, never()).add(any(), any());
            verify(userRepository, never()).getById(any());
            verify(challengeEvaluator, never()).evaluate(any());
        }
    }
}
