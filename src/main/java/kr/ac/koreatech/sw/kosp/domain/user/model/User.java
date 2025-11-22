package kr.ac.koreatech.sw.kosp.domain.user.model;

import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kr.ac.koreatech.sw.kosp.global.model.BaseEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Entity
@Table(name = "user")
@NoArgsConstructor(access = PROTECTED)
@ToString(exclude = "password")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Size(max = 50)
    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @NotNull
    @Column(name = "kut_id", unique = true, nullable = false)
    private String kutId;

    @NotNull
    @Size(max = 255)
    @Column(name = "kut_email", unique = true, nullable = false)
    private String kutEmail;

    @NotNull
    @Column(name = "password", nullable = false)
    private String password;

    @NotNull
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Builder
    private User(
        Integer id,
        String name,
        String kutId,
        String kutEmail,
        String password,
        boolean isDeleted
    ) {
        this.id = id;
        this.name = name;
        this.kutId = kutId;
        this.kutEmail = kutEmail;
        this.password = password;
        this.isDeleted = isDeleted;
    }
}
