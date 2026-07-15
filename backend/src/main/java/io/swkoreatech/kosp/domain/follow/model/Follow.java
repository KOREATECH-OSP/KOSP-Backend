package io.swkoreatech.kosp.domain.follow.model;

import static lombok.AccessLevel.PROTECTED;

import io.swkoreatech.kosp.common.model.BaseEntity;
import io.swkoreatech.kosp.common.user.model.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 팔로우 관계 엔티티.
 *
 * <p>{@code follower}가 {@code following}을 팔로우하는 단방향 관계를 표현한다.</p>
 */
@Getter
@Entity
@Table(
    name = "follow",
    uniqueConstraints = @UniqueConstraint(
        name = "uc_follow_follower_following",
        columnNames = {"follower_id", "following_id"}
    )
)
@NoArgsConstructor(access = PROTECTED)
public class Follow extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 팔로우를 하는 사용자. */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower;

    /** 팔로우를 당하는 사용자. */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id", nullable = false)
    private User following;

    @Builder
    private Follow(User follower, User following) {
        this.follower = follower;
        this.following = following;
    }
}
