package io.swkoreatech.kosp.domain.challenge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import io.swkoreatech.kosp.domain.admin.challenge.dto.AdminChallengeListResponse;
import io.swkoreatech.kosp.domain.admin.challenge.dto.AdminChallengeResponse;
import io.swkoreatech.kosp.domain.challenge.dto.request.ChallengeRequest;
import io.swkoreatech.kosp.domain.challenge.dto.response.ChallengeListResponse;
import io.swkoreatech.kosp.domain.challenge.model.Challenge;
import io.swkoreatech.kosp.domain.challenge.model.ChallengeHistory;
import io.swkoreatech.kosp.domain.challenge.repository.ChallengeHistoryRepository;
import io.swkoreatech.kosp.domain.challenge.repository.ChallengeRepository;
import io.swkoreatech.kosp.domain.github.repository.GithubUserStatisticsRepository;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.global.exception.GlobalException;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChallengeService 단위 테스트")
class ChallengeServiceTest {

    @InjectMocks
    private ChallengeService challengeService;

    @Mock
    private ChallengeRepository challengeRepository;

    @Mock
    private ChallengeHistoryRepository challengeHistoryRepository;

    @Mock
    private GithubUserStatisticsRepository statisticsRepository;

    private Challenge createChallenge(Long id, String name, Integer tier) {
        Challenge challenge = Challenge.builder()
            .name(name)
            .description(name + " 설명")
            .condition("T(Math).min(totalCommits * 100 / 10, 100)")
            .tier(tier)
            .imageUrl("https://image.url/" + id)
            .point(100)
            .build();
        ReflectionTestUtils.setField(challenge, "id", id);
        return challenge;
    }

    private User createUser(Long id) {
        User user = User.builder()
            .name("테스터")
            .kutId("2024" + id)
            .kutEmail("user" + id + "@koreatech.ac.kr")
            .password("encoded_password")
            .roles(new HashSet<>())
            .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private ChallengeHistory createHistory(Long id, User user, Challenge challenge, boolean achieved) {
        ChallengeHistory history = ChallengeHistory.builder()
            .user(user)
            .challenge(challenge)
            .isAchieved(achieved)
            .progressAtAchievement(100)
            .build();
        ReflectionTestUtils.setField(history, "id", id);
        return history;
    }

    @Nested
    @DisplayName("getAllChallenges 메서드")
    class GetAllChallengesTest {

        @Test
        @DisplayName("챌린지가 없으면 빈 목록을 반환한다")
        void returnsEmptyList_whenNoChallenges() {
            // given
            given(challengeRepository.findAll()).willReturn(Collections.emptyList());

            // when
            AdminChallengeListResponse result = challengeService.getAllChallenges();

            // then
            assertThat(result.challenges()).isEmpty();
        }

        @Test
        @DisplayName("모든 챌린지 목록을 반환한다")
        void returnsAllChallenges() {
            // given
            Challenge c1 = createChallenge(1L, "첫 커밋", 1);
            Challenge c2 = createChallenge(2L, "10커밋", 2);
            given(challengeRepository.findAll()).willReturn(List.of(c1, c2));

            // when
            AdminChallengeListResponse result = challengeService.getAllChallenges();

            // then
            assertThat(result.challenges()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("getChallenge 메서드")
    class GetChallengeTest {

        @Test
        @DisplayName("존재하지 않는 챌린지를 조회하면 예외가 발생한다")
        void throwsException_whenChallengeNotFound() {
            // given
            given(challengeRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> challengeService.getChallenge(999L))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("존재하는 챌린지를 조회한다")
        void returnsChallenge_whenExists() {
            // given
            Challenge challenge = createChallenge(1L, "첫 커밋", 1);
            given(challengeRepository.findById(1L)).willReturn(Optional.of(challenge));

            // when
            AdminChallengeResponse result = challengeService.getChallenge(1L);

            // then
            assertThat(result.name()).isEqualTo("첫 커밋");
            assertThat(result.tier()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("createChallenge 메서드")
    class CreateChallengeTest {

        @Test
        @DisplayName("유효한 SpEL 조건으로 챌린지를 생성한다")
        void createsChallenge_withValidCondition() {
            // given
            ChallengeRequest request = new ChallengeRequest(
                "새 챌린지",
                "설명",
                "T(Math).min(totalCommits * 100 / 10, 100)",
                1,
                "https://image.url",
                100
            );

            // when
            challengeService.createChallenge(request);

            // then
            verify(challengeRepository).save(any(Challenge.class));
        }

        @Test
        @DisplayName("잘못된 SpEL 조건으로 챌린지 생성 시 예외가 발생한다")
        void throwsException_whenInvalidCondition() {
            // given
            ChallengeRequest request = new ChallengeRequest(
                "잘못된 챌린지",
                "설명",
                "((( invalid spel",
                1,
                "https://image.url",
                100
            );

            // when & then
            assertThatThrownBy(() -> challengeService.createChallenge(request))
                .isInstanceOf(GlobalException.class);
        }
    }

    @Nested
    @DisplayName("deleteChallenge 메서드")
    class DeleteChallengeTest {

        @Test
        @DisplayName("존재하지 않는 챌린지를 삭제하면 예외가 발생한다")
        void throwsException_whenChallengeNotFound() {
            // given
            given(challengeRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> challengeService.deleteChallenge(999L))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("챌린지를 성공적으로 삭제한다")
        void deletesChallenge() {
            // given
            Challenge challenge = createChallenge(1L, "삭제할 챌린지", 1);
            given(challengeRepository.findById(1L)).willReturn(Optional.of(challenge));

            // when
            challengeService.deleteChallenge(1L);

            // then
            verify(challengeRepository).delete(challenge);
        }
    }

    @Nested
    @DisplayName("updateChallenge 메서드")
    class UpdateChallengeTest {

        @Test
        @DisplayName("존재하지 않는 챌린지를 수정하면 예외가 발생한다")
        void throwsException_whenChallengeNotFound() {
            // given
            given(challengeRepository.findById(999L)).willReturn(Optional.empty());
            ChallengeRequest request = new ChallengeRequest("수정", "설명", "T(Math).min(totalCommits * 100 / 10, 100)", 1, null, 100);

            // when & then
            assertThatThrownBy(() -> challengeService.updateChallenge(999L, request))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("챌린지를 성공적으로 수정한다")
        void updatesChallenge() {
            // given
            Challenge challenge = createChallenge(1L, "기존 챌린지", 1);
            given(challengeRepository.findById(1L)).willReturn(Optional.of(challenge));
            ChallengeRequest request = new ChallengeRequest("수정된 챌린지", "새 설명", "T(Math).min(totalCommits * 100 / 10, 100)", 2, null, 200);

            // when
            challengeService.updateChallenge(1L, request);

            // then
            assertThat(challenge.getName()).isEqualTo("수정된 챌린지");
            assertThat(challenge.getTier()).isEqualTo(2);
            assertThat(challenge.getPoint()).isEqualTo(200);
        }

        @Test
        @DisplayName("조건이 변경되면 SpEL을 재검증한다")
        void validatesSpel_whenConditionChanged() {
            // given
            Challenge challenge = createChallenge(1L, "기존 챌린지", 1);
            given(challengeRepository.findById(1L)).willReturn(Optional.of(challenge));
            ChallengeRequest request = new ChallengeRequest("수정된 챌린지", "새 설명", "T(Math).min(totalPrs * 100 / 5, 100)", 2, null, 200);

            // when
            challengeService.updateChallenge(1L, request);

            // then
            assertThat(challenge.getCondition()).isEqualTo("T(Math).min(totalPrs * 100 / 5, 100)");
        }

        @Test
        @DisplayName("잘못된 조건으로 변경하면 예외가 발생한다")
        void throwsException_whenChangingToInvalidCondition() {
            // given
            Challenge challenge = createChallenge(1L, "기존 챌린지", 1);
            given(challengeRepository.findById(1L)).willReturn(Optional.of(challenge));
            ChallengeRequest request = new ChallengeRequest("수정된 챌린지", "새 설명", "((( invalid", 2, null, 200);

            // when & then
            assertThatThrownBy(() -> challengeService.updateChallenge(1L, request))
                .isInstanceOf(GlobalException.class);
        }
    }

    @Nested
    @DisplayName("getChallenges 메서드")
    class GetChallengesTest {

        @Test
        @DisplayName("티어가 null이면 모든 챌린지를 조회한다")
        void returnsAllChallenges_whenTierIsNull() {
            // given
            User user = createUser(1L);
            Challenge c1 = createChallenge(1L, "챌린지1", 1);
            Challenge c2 = createChallenge(2L, "챌린지2", 2);
            given(challengeRepository.findAll()).willReturn(List.of(c1, c2));
            given(challengeHistoryRepository.findAllByUserId(1L)).willReturn(Collections.emptyList());

            // when
            ChallengeListResponse result = challengeService.getChallenges(user, null);

            // then
            assertThat(result.challenges()).hasSize(2);
        }

        @Test
        @DisplayName("특정 티어의 챌린지만 조회한다")
        void returnsChallengesByTier() {
            // given
            User user = createUser(1L);
            Challenge c1 = createChallenge(1L, "티어1 챌린지", 1);
            given(challengeRepository.findByTier(1)).willReturn(List.of(c1));
            given(challengeHistoryRepository.findAllByUserId(1L)).willReturn(Collections.emptyList());

            // when
            ChallengeListResponse result = challengeService.getChallenges(user, 1);

            // then
            assertThat(result.challenges()).hasSize(1);
            verify(challengeRepository).findByTier(1);
            verify(challengeRepository, never()).findAll();
        }

        @Test
        @DisplayName("완료한 챌린지의 진행도를 반영한다")
        void reflectsCompletedChallengeProgress() {
            // given
            User user = createUser(1L);
            Challenge challenge = createChallenge(1L, "완료 챌린지", 1);
            ChallengeHistory history = createHistory(1L, user, challenge, true);
            
            given(challengeRepository.findAll()).willReturn(List.of(challenge));
            given(challengeHistoryRepository.findAllByUserId(1L)).willReturn(List.of(history));

            // when
            ChallengeListResponse result = challengeService.getChallenges(user, null);

            // then
            assertThat(result.challenges().get(0).isCompleted()).isTrue();
            assertThat(result.summary().completedCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("챌린지가 없으면 진행률이 0이다")
        void overallProgressIsZero_whenNoChallenges() {
            // given
            User user = createUser(1L);
            given(challengeRepository.findAll()).willReturn(Collections.emptyList());
            given(challengeHistoryRepository.findAllByUserId(1L)).willReturn(Collections.emptyList());

            // when
            ChallengeListResponse result = challengeService.getChallenges(user, null);

            // then
            assertThat(result.summary().overallProgress()).isEqualTo(0.0);
        }
    }
}
