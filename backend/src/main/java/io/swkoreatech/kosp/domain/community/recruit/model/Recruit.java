package io.swkoreatech.kosp.domain.community.recruit.model;

import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.community.article.model.Article;
import io.swkoreatech.kosp.domain.community.board.model.Board;
import io.swkoreatech.kosp.domain.community.team.model.Team;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 모집 공고 엔티티.
 * {@link Article}을 상속하여 모집 관련 추가 정보를 관리한다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "recruit")
@DiscriminatorValue("RECRUIT")
public class Recruit extends Article {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecruitStatus status;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Builder(builderMethodName = "recruitBuilder")
    private Recruit(User author, Board board, String title, String content, List<String> tags,
                    Team team, RecruitStatus status, LocalDateTime startDate, LocalDateTime endDate) {
        super(author, board, title, content, tags, null);
        this.team = team;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    /**
     * 모집 공고를 수정한다.
     *
     * @param title 제목
     * @param content 내용
     * @param tags 태그 목록
     * @param team 팀
     * @param startDate 모집 시작일
     * @param endDate 모집 종료일
     */
    public void updateRecruit(
        String title,
        String content,
        List<String> tags,
        Team team,
        LocalDateTime startDate,
        LocalDateTime endDate
    ) {
        super.updateArticle(title, content, tags);
        this.team = team;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    /**
     * 모집 상태를 변경한다.
     *
     * @param status 변경할 상태
     */
    public void updateStatus(RecruitStatus status) {
        this.status = status;
    }
}
