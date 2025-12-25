package kr.ac.koreatech.sw.kosp.domain.community.recruit.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import kr.ac.koreatech.sw.kosp.domain.community.article.model.Article;
import kr.ac.koreatech.sw.kosp.domain.community.board.model.Board;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "recruitment")
@DiscriminatorValue("RECRUIT")
public class Recruitment extends Article {

    @Column(name = "team_id", nullable = false)
    private Long teamId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecruitmentStatus status;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Builder(builderMethodName = "recruitBuilder")
    public Recruitment(
        User author,
        Board board,
        String title,
        String content,
        List<String> tags,
        Long teamId,
        RecruitmentStatus status,
        LocalDateTime startDate,
        LocalDateTime endDate
    ) {
        super(board, author, title, content, tags);
        this.teamId = teamId;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void updateRecruit(
        String title,
        String content,
        List<String> tags,
        Long teamId,
        LocalDateTime startDate,
        LocalDateTime endDate
    ) {
        super.update(title, content, tags);
        this.teamId = teamId;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void updateStatus(RecruitmentStatus status) {
        this.status = status;
    }
}
