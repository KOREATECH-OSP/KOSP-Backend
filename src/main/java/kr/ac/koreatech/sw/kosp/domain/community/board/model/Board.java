package kr.ac.koreatech.sw.kosp.domain.community.board.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.ac.koreatech.sw.kosp.global.model.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "board")
public class Board extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(name = "is_recruit_allowed", nullable = false)
    private boolean isRecruitAllowed = false;

    @Builder
    private Board(String name, String description, boolean isRecruitAllowed) {
        this.name = name;
        this.description = description;
        this.isRecruitAllowed = isRecruitAllowed;
    }
}
