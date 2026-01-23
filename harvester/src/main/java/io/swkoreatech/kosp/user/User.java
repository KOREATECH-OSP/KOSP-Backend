package io.swkoreatech.kosp.user;

import static lombok.AccessLevel.PROTECTED;

import io.swkoreatech.kosp.common.user.BaseUser;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = PROTECTED)
@SuperBuilder
public class User extends BaseUser {
}
