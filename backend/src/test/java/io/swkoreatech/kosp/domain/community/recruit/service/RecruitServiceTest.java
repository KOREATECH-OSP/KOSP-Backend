package io.swkoreatech.kosp.domain.community.recruit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import io.swkoreatech.kosp.domain.community.article.repository.ArticleBookmarkRepository;
import io.swkoreatech.kosp.domain.community.article.repository.ArticleLikeRepository;
import io.swkoreatech.kosp.domain.community.board.model.Board;
import io.swkoreatech.kosp.domain.community.recruit.dto.request.RecruitRequest;
import io.swkoreatech.kosp.domain.community.recruit.dto.response.RecruitListResponse;
import io.swkoreatech.kosp.domain.community.recruit.dto.response.RecruitResponse;
import io.swkoreatech.kosp.domain.community.recruit.model.Recruit;
import io.swkoreatech.kosp.domain.community.recruit.model.RecruitApply;
import io.swkoreatech.kosp.domain.community.recruit.model.RecruitStatus;
import io.swkoreatech.kosp.domain.community.recruit.repository.RecruitApplyRepository;
import io.swkoreatech.kosp.domain.community.recruit.repository.RecruitRepository;
import io.swkoreatech.kosp.domain.community.team.model.Team;
import io.swkoreatech.kosp.domain.community.team.model.TeamMember;
import io.swkoreatech.kosp.domain.community.team.repository.TeamMemberRepository;
import io.swkoreatech.kosp.domain.community.team.repository.TeamRepository;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.global.exception.GlobalException;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecruitService 단위 테스트")
class RecruitServiceTest {

    @InjectMocks
    private RecruitService recruitService;

    @Mock
    private RecruitRepository recruitRepository;

    @Mock
    private ArticleLikeRepository articleLikeRepository;

    @Mock
    private ArticleBookmarkRepository articleBookmarkRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private RecruitApplyRepository recruitApplyRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    private User createUser(Long id, String name) {
        User user = User.builder()
            .name(name)
            .kutId("2024" + id)
            .kutEmail(name + "@koreatech.ac.kr")
            .password("password")
            .roles(new HashSet<>())
            .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Board createBoard(Long id, String name) {
        Board board = Board.builder()
            .name(name)
            .description(name + " 게시판")
            .build();
        ReflectionTestUtils.setField(board, "id", id);
        ReflectionTestUtils.setField(board, "isRecruitAllowed", true);
        return board;
    }

    private Team createTeam(Long id, String name) {
        Team team = Team.builder()
            .name(name)
            .description(name + " 팀")
            .build();
        ReflectionTestUtils.setField(team, "id", id);
        return team;
    }

    private Recruit createRecruit(Long id, User author, Board board, Team team) {
        Recruit recruit = Recruit.builder()
            .author(author)
            .board(board)
            .title("모집 공고")
            .content("모집합니다")
            .team(team)
            .status(RecruitStatus.OPEN)
            .startDate(LocalDateTime.now())
            .endDate(LocalDateTime.now().plusDays(7))
            .build();
        ReflectionTestUtils.setField(recruit, "id", id);
        ReflectionTestUtils.setField(recruit, "views", 0);
        ReflectionTestUtils.setField(recruit, "createdAt", java.time.LocalDateTime.now());
        return recruit;
    }

    @Nested
    @DisplayName("create 메서드")
    class CreateTest {

        @Test
        @DisplayName("모집 공고를 성공적으로 생성한다")
        void createsRecruit() {
            // given
            User author = createUser(1L, "작성자");
            Board board = createBoard(1L, "모집게시판");
            Team team = createTeam(1L, "테스트팀");
            RecruitRequest request = new RecruitRequest(1L, "모집 제목", "모집 내용", List.of(), 1L, LocalDateTime.now(), LocalDateTime.now().plusDays(7));
            Recruit savedRecruit = createRecruit(1L, author, board, team);
            
            given(teamRepository.getById(1L)).willReturn(team);
            given(recruitRepository.save(any(Recruit.class))).willReturn(savedRecruit);

            // when
            Long recruitId = recruitService.create(author, board, request);

            // then
            assertThat(recruitId).isEqualTo(1L);
            verify(recruitRepository).save(any(Recruit.class));
        }
    }

    @Nested
    @DisplayName("getOne 메서드")
    class GetOneTest {

        @Test
        @DisplayName("모집 공고를 조회하고 조회수가 증가한다")
        void returnsRecruitAndIncreasesViews() {
            // given
            User author = createUser(1L, "작성자");
            User viewer = createUser(2L, "조회자");
            Board board = createBoard(1L, "모집게시판");
            Team team = createTeam(1L, "테스트팀");
            Recruit recruit = createRecruit(1L, author, board, team);
            
            given(recruitRepository.getById(1L)).willReturn(recruit);

            // when
            RecruitResponse response = recruitService.getOne(1L, viewer);

            // then
            assertThat(response.title()).isEqualTo("모집 공고");
            assertThat(recruit.getViews()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("updateStatus 메서드")
    class UpdateStatusTest {

        @Test
        @DisplayName("작성자가 아니면 상태 수정 시 예외가 발생한다")
        void throwsException_whenNotOwner() {
            // given
            User author = createUser(1L, "작성자");
            User other = createUser(2L, "다른 사용자");
            Board board = createBoard(1L, "모집게시판");
            Team team = createTeam(1L, "테스트팀");
            Recruit recruit = createRecruit(1L, author, board, team);
            
            given(recruitRepository.getById(1L)).willReturn(recruit);

            // when & then
            assertThatThrownBy(() -> recruitService.updateStatus(other, 1L, RecruitStatus.CLOSED))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("작성자가 모집 상태를 수정한다")
        void updatesStatus() {
            // given
            User author = createUser(1L, "작성자");
            Board board = createBoard(1L, "모집게시판");
            Team team = createTeam(1L, "테스트팀");
            Recruit recruit = createRecruit(1L, author, board, team);
            
            given(recruitRepository.getById(1L)).willReturn(recruit);

            // when
            recruitService.updateStatus(author, 1L, RecruitStatus.CLOSED);

            // then
            assertThat(recruit.getStatus()).isEqualTo(RecruitStatus.CLOSED);
        }
    }

    @Nested
    @DisplayName("delete 메서드")
    class DeleteTest {

        @Test
        @DisplayName("작성자가 아니면 삭제 시 예외가 발생한다")
        void throwsException_whenNotOwner() {
            // given
            User author = createUser(1L, "작성자");
            User other = createUser(2L, "다른 사용자");
            Board board = createBoard(1L, "모집게시판");
            Team team = createTeam(1L, "테스트팀");
            Recruit recruit = createRecruit(1L, author, board, team);
            
            given(recruitRepository.getById(1L)).willReturn(recruit);

            // when & then
            assertThatThrownBy(() -> recruitService.delete(other, 1L))
                .isInstanceOf(GlobalException.class);
        }

        @Test
        @DisplayName("작성자가 모집 공고를 삭제한다")
        void deletesRecruit() {
            // given
            User author = createUser(1L, "작성자");
            Board board = createBoard(1L, "모집게시판");
            Team team = createTeam(1L, "테스트팀");
            Recruit recruit = createRecruit(1L, author, board, team);
            
            given(recruitRepository.getById(1L)).willReturn(recruit);

            // when
            recruitService.delete(author, 1L);

            // then
            assertThat(recruit.getStatus()).isEqualTo(RecruitStatus.CLOSED);
            verify(recruitRepository).delete(recruit);
        }
    }

    @Nested
    @DisplayName("getList 메서드")
    class GetListTest {

        @Test
        @DisplayName("모집 공고 목록을 페이징하여 조회한다")
        void returnsPagedRecruitList() {
            // given
            User author = createUser(1L, "작성자");
            Board board = createBoard(1L, "모집게시판");
            Team team = createTeam(1L, "테스트팀");
            Recruit recruit = createRecruit(1L, author, board, team);
            Pageable pageable = PageRequest.of(0, 10);
            Page<Recruit> page = new PageImpl<>(List.of(recruit), pageable, 1);
            
            given(recruitRepository.findAll(any(Specification.class), any(Pageable.class))).willReturn(page);

            // when
            RecruitListResponse result = recruitService.getList(board, pageable, author, null);

            // then
            assertThat(result.recruits()).hasSize(1);
        }

        @Test
        @DisplayName("로그인하지 않은 사용자도 목록을 조회할 수 있다")
        void returnsListForAnonymousUser() {
            // given
            User author = createUser(1L, "작성자");
            Board board = createBoard(1L, "모집게시판");
            Team team = createTeam(1L, "테스트팀");
            Recruit recruit = createRecruit(1L, author, board, team);
            Pageable pageable = PageRequest.of(0, 10);
            Page<Recruit> page = new PageImpl<>(List.of(recruit), pageable, 1);
            
            given(recruitRepository.findAll(any(Specification.class), any(Pageable.class))).willReturn(page);

            // when
            RecruitListResponse result = recruitService.getList(board, pageable, null, null);

            // then
            assertThat(result.recruits()).hasSize(1);
        }

        @Test
        @DisplayName("RSQL 필터로 삭제된 모집 필터링")
        void getList_withRsqlFilter_returnsFilteredRecruits() {
            // given
            User author = createUser(1L, "작성자");
            Board board = createBoard(1L, "모집게시판");
            Team team = createTeam(1L, "테스트팀");
            
            Recruit activeRecruit = createRecruit(1L, author, board, team);
            Recruit deletedRecruit = createRecruit(2L, author, board, team);
            ReflectionTestUtils.setField(deletedRecruit, "isDeleted", true);
            
            Pageable pageable = PageRequest.of(0, 10);
            String rsql = "isDeleted==false";
            
            // Mock the Specification-based query
            given(recruitRepository.findAll(any(Specification.class), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(activeRecruit), pageable, 1));
            
            // when
            RecruitListResponse result = recruitService.getList(board, pageable, author, rsql);
            
            // then
            assertThat(result.recruits()).hasSize(1);
            assertThat(result.recruits().get(0).title()).isEqualTo("모집 공고");
        }

        @Test
        @DisplayName("페이지 크기가 100으로 제한된다")
        void getList_withLargePageSize_capsAt100() {
            // given
            User author = createUser(1L, "작성자");
            Board board = createBoard(1L, "모집게시판");
            Team team = createTeam(1L, "테스트팀");
            Recruit recruit = createRecruit(1L, author, board, team);
            
            Pageable largePage = PageRequest.of(0, 999);
            Pageable cappedPage = PageRequest.of(0, 100);
            Page<Recruit> page = new PageImpl<>(List.of(recruit), cappedPage, 1);
            
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            given(recruitRepository.findAll(any(Specification.class), pageableCaptor.capture()))
                .willReturn(page);
            
            // when
            recruitService.getList(board, largePage, author, null);
            
            // then
            Pageable capturedPageable = pageableCaptor.getValue();
            assertThat(capturedPageable.getPageSize()).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("update 메서드")
    class UpdateTest {

        @Test
        @DisplayName("작성자가 모집 공고를 수정한다")
        void updatesRecruit() {
            // given
            User author = createUser(1L, "작성자");
            Board board = createBoard(1L, "모집게시판");
            Team team = createTeam(1L, "테스트팀");
            Recruit recruit = createRecruit(1L, author, board, team);
            RecruitRequest request = new RecruitRequest(1L, "수정된 제목", "수정된 내용", List.of("java"), 1L, LocalDateTime.now(), LocalDateTime.now().plusDays(14));
            
            given(recruitRepository.getById(1L)).willReturn(recruit);
            given(teamRepository.getById(1L)).willReturn(team);

            // when
            recruitService.update(author, 1L, request);

            // then
            assertThat(recruit.getTitle()).isEqualTo("수정된 제목");
            assertThat(recruit.getContent()).isEqualTo("수정된 내용");
        }

        @Test
        @DisplayName("작성자가 아니면 수정 시 예외가 발생한다")
        void throwsException_whenNotOwner() {
            // given
            User author = createUser(1L, "작성자");
            User other = createUser(2L, "다른 사용자");
            Board board = createBoard(1L, "모집게시판");
            Team team = createTeam(1L, "테스트팀");
            Recruit recruit = createRecruit(1L, author, board, team);
            RecruitRequest request = new RecruitRequest(1L, "수정된 제목", "수정된 내용", List.of(), 1L, LocalDateTime.now(), LocalDateTime.now().plusDays(14));
            
            given(recruitRepository.getById(1L)).willReturn(recruit);

            // when & then
            assertThatThrownBy(() -> recruitService.update(other, 1L, request))
                .isInstanceOf(GlobalException.class);
        }
    }

    @Nested
    @DisplayName("getOne 메서드 - 좋아요/북마크")
    class GetOneLikeBookmarkTest {

        @Test
        @DisplayName("RecruitResponse에 canApply 포함")
        void getOne_returnsCanApplyField() {
            // given
            User author = createUser(1L, "작성자");
            User viewer = createUser(2L, "조회자");
            Board board = createBoard(1L, "모집게시판");
            Team team = createTeam(1L, "테스트팀");
            Recruit recruit = createRecruit(1L, author, board, team);
            
            given(recruitRepository.getById(1L)).willReturn(recruit);
            given(recruitApplyRepository.findByRecruitAndUser(recruit, viewer))
                .willReturn(Optional.empty());
            given(teamMemberRepository.existsByTeamAndUserAndIsDeletedFalse(team, viewer))
                .willReturn(false);
            
            // when
            RecruitResponse response = recruitService.getOne(1L, viewer);
            
            // then
            assertThat(response.canApply()).isTrue();
        }

        @Test
        @DisplayName("RecruitResponse에 isDeleted 포함")
        void getOne_returnsIsDeletedField() {
            // given
            User author = createUser(1L, "작성자");
            User viewer = createUser(2L, "조회자");
            Board board = createBoard(1L, "모집게시판");
            Team team = createTeam(1L, "테스트팀");
            Recruit recruit = createRecruit(1L, author, board, team);
            
            given(recruitRepository.getById(1L)).willReturn(recruit);
            
            // when
            RecruitResponse response = recruitService.getOne(1L, viewer);
            
            // then
            assertThat(response.isDeleted()).isFalse();
        }

        @Test
        @DisplayName("로그인한 사용자가 좋아요와 북마크를 한 경우")
        void returnsWithLikeAndBookmark() {
            // given
            User author = createUser(1L, "작성자");
            User viewer = createUser(2L, "조회자");
            Board board = createBoard(1L, "모집게시판");
            Team team = createTeam(1L, "테스트팀");
            Recruit recruit = createRecruit(1L, author, board, team);
            
            given(recruitRepository.getById(1L)).willReturn(recruit);
            given(articleLikeRepository.existsByUserAndArticle(viewer, recruit)).willReturn(true);
            given(articleBookmarkRepository.existsByUserAndArticle(viewer, recruit)).willReturn(true);

            // when
            RecruitResponse response = recruitService.getOne(1L, viewer);

            // then
            assertThat(response.isLiked()).isTrue();
            assertThat(response.isBookmarked()).isTrue();
        }

        @Test
        @DisplayName("비로그인 사용자는 좋아요/북마크가 false다")
        void returnsFalseForAnonymous() {
            // given
            User author = createUser(1L, "작성자");
            Board board = createBoard(1L, "모집게시판");
            Team team = createTeam(1L, "테스트팀");
            Recruit recruit = createRecruit(1L, author, board, team);
            
            given(recruitRepository.getById(1L)).willReturn(recruit);

            // when
            RecruitResponse response = recruitService.getOne(1L, null);

            // then
            assertThat(response.isLiked()).isFalse();
            assertThat(response.isBookmarked()).isFalse();
        }
    }

    @Nested
    @DisplayName("canApply 메서드")
    class CanApplyTest {

        @Test
        @DisplayName("사용자가 이미 지원한 경우 false 반환")
        void returnsFalse_whenUserAlreadyApplied() {
            // given
            User user = createUser(1L, "지원자");
            User author = createUser(2L, "작성자");
            Board board = createBoard(1L, "모집게시판");
            Team team = createTeam(1L, "테스트팀");
            Recruit recruit = createRecruit(1L, author, board, team);
            
            RecruitApply pendingApply = RecruitApply.builder()
                .recruit(recruit)
                .user(user)
                .reason("지원합니다")
                .portfolioUrl("https://github.com/user")
                .build();
            
            given(recruitApplyRepository.findByRecruitAndUser(recruit, user))
                .willReturn(Optional.of(pendingApply));

            // when
            boolean result = recruitService.canApply(user, recruit);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("사용자가 팀 멤버인 경우 false 반환")
        void returnsFalse_whenUserIsTeamMember() {
            // given
            User user = createUser(1L, "팀원");
            User author = createUser(2L, "작성자");
            Board board = createBoard(1L, "모집게시판");
            Team team = createTeam(1L, "테스트팀");
            Recruit recruit = createRecruit(1L, author, board, team);
            
            given(recruitApplyRepository.findByRecruitAndUser(recruit, user))
                .willReturn(Optional.empty());
            given(teamMemberRepository.existsByTeamAndUserAndIsDeletedFalse(team, user))
                .willReturn(true);

            // when
            boolean result = recruitService.canApply(user, recruit);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("사용자가 거절당한 경우 true 반환")
        void returnsTrue_whenUserWasRejected() {
            // given
            User user = createUser(1L, "지원자");
            User author = createUser(2L, "작성자");
            Board board = createBoard(1L, "모집게시판");
            Team team = createTeam(1L, "테스트팀");
            Recruit recruit = createRecruit(1L, author, board, team);
            
            RecruitApply rejectedApply = RecruitApply.builder()
                .recruit(recruit)
                .user(user)
                .reason("지원합니다")
                .portfolioUrl("https://github.com/user")
                .build();
            rejectedApply.updateStatus(RecruitApply.ApplyStatus.REJECTED);
            
            given(recruitApplyRepository.findByRecruitAndUser(recruit, user))
                .willReturn(Optional.of(rejectedApply));
            given(teamMemberRepository.existsByTeamAndUserAndIsDeletedFalse(team, user))
                .willReturn(false);

            // when
            boolean result = recruitService.canApply(user, recruit);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("사용자가 지원하지 않고 멤버도 아닌 경우 true 반환")
        void returnsTrue_whenUserEligible() {
            // given
            User user = createUser(1L, "지원자");
            User author = createUser(2L, "작성자");
            Board board = createBoard(1L, "모집게시판");
            Team team = createTeam(1L, "테스트팀");
            Recruit recruit = createRecruit(1L, author, board, team);
            
            given(recruitApplyRepository.findByRecruitAndUser(recruit, user))
                .willReturn(Optional.empty());
            given(teamMemberRepository.existsByTeamAndUserAndIsDeletedFalse(team, user))
                .willReturn(false);

            // when
            boolean result = recruitService.canApply(user, recruit);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("익명 사용자는 false 반환")
        void returnsFalse_whenUserIsAnonymous() {
            // given
            User author = createUser(1L, "작성자");
            Board board = createBoard(1L, "모집게시판");
            Team team = createTeam(1L, "테스트팀");
            Recruit recruit = createRecruit(1L, author, board, team);

            // when
            boolean result = recruitService.canApply(null, recruit);

            // then
            assertThat(result).isFalse();
        }
    }
}
