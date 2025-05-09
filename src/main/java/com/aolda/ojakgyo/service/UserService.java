package com.aolda.ojakgyo.service;

import com.aolda.ojakgyo.exception.CustomException;
import com.aolda.ojakgyo.exception.ErrorCode;
import com.aolda.ojakgyo.repository.UserRepository;
import com.aolda.ojakgyo.dto.user.UserSignupDto;
import com.aolda.ojakgyo.dto.user.UserLoginDto;
import com.aolda.ojakgyo.entity.User;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final HttpSession session;

    public void signup(UserSignupDto signupDto) {

        if (userRepository.findById(signupDto.getId()).isPresent()) {
            throw new CustomException(ErrorCode.USER_ARE_EXIST);
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(signupDto.getPassword());

        // 유저 생성 및 저장
        User user = User.builder()
                .userId(signupDto.getId())
                .email(signupDto.getEmail())
                .password(encodedPassword)
                .name(signupDto.getName())
                .address(signupDto.getAddress())
                .build();

        userRepository.save(user);
    }

    public void login(UserLoginDto loginDto) {
        // 유저 조회
        User user = userRepository.findById(loginDto.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_USER_INFO));

        // 비밀번호 확인
        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_USER_INFO);
        }

        // 세션에 유저 정보 저장
        session.setAttribute("USER", user);
    }

    public void logout() {
        // 세션 무효화
        session.invalidate();
    }

    public User getCurrentUser() {
        User user = (User) session.getAttribute("USER");
        if (user == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        return user;
    }

    public boolean isLoggedIn() {
        return session.getAttribute("USER") != null;
    }
}
