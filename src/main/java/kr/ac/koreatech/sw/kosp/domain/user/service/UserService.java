package kr.ac.koreatech.sw.kosp.domain.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.ac.koreatech.sw.kosp.domain.auth.jwt.model.JwtToken;
import kr.ac.koreatech.sw.kosp.domain.auth.jwt.service.JwtService;
import kr.ac.koreatech.sw.kosp.domain.user.dto.request.UserSignUpRequest;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public JwtToken signUp(UserSignUpRequest request) {
        User user = request.toUser(passwordEncoder);
        User savedUser = userRepository.save(user);

        return jwtService.createJwtToken(savedUser.getId());
    }

}
