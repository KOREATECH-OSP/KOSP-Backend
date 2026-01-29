package io.swkoreatech.kosp.domain.community.recruit.repository;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import io.swkoreatech.kosp.domain.community.board.model.Board;
import io.swkoreatech.kosp.domain.community.recruit.model.Recruit;
import io.swkoreatech.kosp.domain.community.recruit.model.RecruitApply;
import io.swkoreatech.kosp.domain.community.recruit.model.RecruitStatus;
import io.swkoreatech.kosp.domain.community.team.model.Team;
import io.swkoreatech.kosp.domain.user.model.User;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("RecruitApplyRepository 단위 테스트")
class RecruitApplyRepositoryTest {

    @Autowired
    private RecruitApplyRepository recruitApplyRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("동일 사용자가 동일 모집에 중복 지원 시 예외 발생")
    void save_throwsException_whenDuplicateRecruitUser() {
        User user = User.builder()
            .name("Test User")
            .kutId("2024001")
            .kutEmail("test@koreatech.ac.kr")
            .password("encoded_password")
            .build();
        entityManager.persistAndFlush(user);

        Board board = Board.builder()
            .name("Test Board")
            .description("Test Description")
            .build();
        entityManager.persistAndFlush(board);

        Team team = Team.builder()
            .name("Test Team")
            .description("Test Team Description")
            .build();
        entityManager.persistAndFlush(team);

        Recruit recruit = Recruit.builder()
            .author(user)
            .board(board)
            .title("Test Recruit")
            .content("Test Content")
            .team(team)
            .status(RecruitStatus.OPEN)
            .startDate(LocalDateTime.now())
            .endDate(LocalDateTime.now().plusDays(7))
            .build();
        entityManager.persistAndFlush(recruit);

        RecruitApply firstApply = RecruitApply.builder()
            .recruit(recruit)
            .user(user)
            .reason("First application")
            .portfolioUrl("https://github.com/user")
            .build();
        entityManager.persistAndFlush(firstApply);

        RecruitApply duplicateApply = RecruitApply.builder()
            .recruit(recruit)
            .user(user)
            .reason("Duplicate application")
            .portfolioUrl("https://github.com/user")
            .build();

        assertThatThrownBy(() -> {
            entityManager.persistAndFlush(duplicateApply);
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}
