package kr.ac.koreatech.sw.kosp.domain.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.ac.koreatech.sw.kosp.domain.user.dto.request.UserSignupRequest;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signup(UserSignupRequest request) {
        // 1. User 생성
        User user = request.toUser();
        user.encodePassword(passwordEncoder);
        User savedUser = userRepository.save(user);

        log.info("✅ 사용자 생성 완료: userId={}, kutEmail={}", savedUser.getId(), savedUser.getKutEmail());
    }

}
