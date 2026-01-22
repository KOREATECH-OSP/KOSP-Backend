package io.swkoreatech.kosp.domain.community.recruit.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import io.swkoreatech.kosp.domain.community.article.model.Article;
import io.swkoreatech.kosp.domain.community.team.model.Team;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
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

    public void updateStatus(RecruitStatus status) {
        this.status = status;
    }
}
