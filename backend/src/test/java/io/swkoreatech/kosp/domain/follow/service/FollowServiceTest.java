package io.swkoreatech.kosp.domain.follow.service;

import static io.swkoreatech.kosp.global.common.fixture.TestUserFixture.createUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.title.repository.UserTitleRepository;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.common.user.repository.UserRepository;
import io.swkoreatech.kosp.domain.follow.dto.response.FollowUserListResponse;
import io.swkoreatech.kosp.domain.follow.model.Follow;
import io.swkoreatech.kosp.domain.follow.repository.FollowRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("FollowService 단위 테스트 - 팔로워/팔로잉 목록")
class FollowServiceTest {

    @InjectMocks
    private FollowService followService;

    @Mock
    private FollowRepository followRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserTitleRepository userTitleRepository;

    private Follow follow(Long id, User follower, User following) {
        Follow f = Follow.builder().follower(follower).following(following).build();
        ReflectionTestUtils.setField(f, "id", id);
        return f;
    }

    @Nested
    @DisplayName("getFollowers 메서드")
    class GetFollowersTest {

        @Test
        @DisplayName("팔로워 목록을 페이지 메타와 함께 반환한다")
        void returnsFollowersWithPageMeta() {
            // given
            User owner = createUser(1L, "주인");
            User followerA = createUser(2L, "A");
            User followerB = createUser(3L, "B");
            Page<Follow> page = new PageImpl<>(
                List.of(follow(11L, followerA, owner), follow(12L, followerB, owner)),
                PageRequest.of(0, 20),
                2
            );
            given(followRepository.findFollowerPage(eqId(1L), any(Pageable.class))).willReturn(page);
            given(userTitleRepository.findAllDisplayTitlesByUserIds(anyCollection())).willReturn(List.of());

            // when
            FollowUserListResponse result = followService.getFollowers(null, 1L, 0, 20);

            // then
            assertThat(result.users()).hasSize(2);
            assertThat(result.meta().totalItems()).isEqualTo(2);
            assertThat(result.users().get(0).userId()).isEqualTo(2L);
        }

        @Test
        @DisplayName("비로그인 조회 시 isFollowing/isMe 는 모두 false 다")
        void marksNotFollowing_whenViewerIsNull() {
            // given
            User owner = createUser(1L, "주인");
            Page<Follow> page = new PageImpl<>(
                List.of(follow(11L, createUser(2L, "A"), owner)), PageRequest.of(0, 20), 1);
            given(followRepository.findFollowerPage(eqId(1L), any(Pageable.class))).willReturn(page);
            given(userTitleRepository.findAllDisplayTitlesByUserIds(anyCollection())).willReturn(List.of());

            // when
            FollowUserListResponse result = followService.getFollowers(null, 1L, 0, 20);

            // then
            assertThat(result.users().get(0).isFollowing()).isFalse();
            assertThat(result.users().get(0).isMe()).isFalse();
            then(followRepository).should(never()).findFollowingIdsIn(any(), anyCollection());
        }

        @Test
        @DisplayName("조회자 본인은 isMe=true, 이미 팔로우한 사용자는 isFollowing=true 로 내려준다")
        void marksMeAndFollowing_whenViewerLoggedIn() {
            // given
            User viewer = createUser(2L, "조회자");
            User owner = createUser(1L, "주인");
            User other = createUser(3L, "C");
            Page<Follow> page = new PageImpl<>(
                List.of(follow(11L, viewer, owner), follow(12L, other, owner)),
                PageRequest.of(0, 20), 2);
            given(followRepository.findFollowerPage(eqId(1L), any(Pageable.class))).willReturn(page);
            given(userTitleRepository.findAllDisplayTitlesByUserIds(anyCollection())).willReturn(List.of());
            given(followRepository.findFollowingIdsIn(eqId(2L), anyCollection())).willReturn(List.of(3L));

            // when
            FollowUserListResponse result = followService.getFollowers(viewer, 1L, 0, 20);

            // then
            assertThat(result.users().get(0).isMe()).isTrue();
            assertThat(result.users().get(0).isFollowing()).isFalse();
            assertThat(result.users().get(1).isMe()).isFalse();
            assertThat(result.users().get(1).isFollowing()).isTrue();
        }

        @Test
        @DisplayName("팔로워가 없으면 빈 목록과 총 0건을 반환한다")
        void returnsEmpty_whenNoFollowers() {
            // given
            given(followRepository.findFollowerPage(eqId(1L), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

            // when
            FollowUserListResponse result = followService.getFollowers(null, 1L, 0, 20);

            // then
            assertThat(result.users()).isEmpty();
            assertThat(result.meta().totalItems()).isZero();
        }

        @Test
        @DisplayName("size 가 상한(100)을 넘으면 100으로 잘린다")
        void clampsPageSize() {
            // given
            given(followRepository.findFollowerPage(eqId(1L), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(), PageRequest.of(0, 100), 0));

            // when
            followService.getFollowers(null, 1L, 0, 500);

            // then
            then(followRepository).should()
                .findFollowerPage(1L, PageRequest.of(0, 100));
        }
    }

    @Nested
    @DisplayName("follow 메서드")
    class FollowTest {

        @Test
        @DisplayName("자기 자신은 팔로우할 수 없다")
        void throwsException_whenFollowingSelf() {
            // given
            User me = createUser(1L);

            // when & then
            assertThatThrownBy(() -> followService.follow(me, 1L))
                .isInstanceOf(GlobalException.class);
        }
    }

    /** 가독성을 위한 Long 인자 매처 대체 (기본값 그대로 사용). */
    private static Long eqId(Long id) {
        return org.mockito.ArgumentMatchers.eq(id);
    }
}
