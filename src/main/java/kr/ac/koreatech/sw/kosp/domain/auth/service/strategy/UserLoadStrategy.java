package kr.ac.koreatech.sw.kosp.domain.auth.service.strategy;

import org.springframework.security.core.userdetails.UserDetails;

public interface UserLoadStrategy {
    UserDetails loadUser(String id);
}
