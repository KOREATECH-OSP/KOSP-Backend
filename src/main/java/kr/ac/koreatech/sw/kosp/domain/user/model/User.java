package kr.ac.koreatech.sw.kosp.domain.user.model;

import static lombok.AccessLevel.PROTECTED;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kr.ac.koreatech.sw.kosp.domain.github.model.GithubUser;
import kr.ac.koreatech.sw.kosp.global.model.BaseEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = PROTECTED)
@ToString(exclude = {"password", "githubUser"})
public class User extends BaseEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @OneToOne
    @JoinColumn(name = "github_id")
    private GithubUser githubUser;

    @Builder
    private User(
        Long id,
        String name,
        String kutId,
        String kutEmail,
        String password,
        boolean isDeleted,
        GithubUser githubUser
    ) {
        this.id = id;
        this.name = name;
        this.kutId = kutId;
        this.kutEmail = kutEmail;
        this.password = password;
        this.isDeleted = isDeleted;
        updateGithubUser(githubUser);
    }

    public void encodePassword(PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(password);
    }

    public void updateGithubUser(GithubUser githubUser) {
        this.githubUser = githubUser;
    }

    // UserDetails Implementation

    @Override
    public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
        return java.util.Collections.emptyList();
    }

    @Override
    public String getUsername() {
        return this.kutEmail;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return !this.isDeleted;
    }
}
