package io.swkoreatech.kosp.harvester.user;

import static lombok.AccessLevel.PROTECTED;

import io.swkoreatech.kosp.common.github.model.GithubUser;
import io.swkoreatech.kosp.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = PROTECTED)
@SuperBuilder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "kut_id", unique = true, nullable = false)
    private String kutId;

    @Column(name = "kut_email", unique = true, nullable = false)
    private String kutEmail;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @OneToOne
    @JoinColumn(name = "github_id")
    private GithubUser githubUser;

    public boolean hasGithubUser() {
        return githubUser != null;
    }
}
