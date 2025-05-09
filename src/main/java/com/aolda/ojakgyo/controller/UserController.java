package com.aolda.ojakgyo.controller;

import com.aolda.ojakgyo.dto.user.UserLoginDto;
import com.aolda.ojakgyo.dto.user.UserSignupDto;
import com.aolda.ojakgyo.entity.User;
import com.aolda.ojakgyo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@Valid @RequestBody UserSignupDto signupDto) {
        userService.signup(signupDto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody UserLoginDto loginDto) {
        userService.login(loginDto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        userService.logout();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        User user = userService.getCurrentUser();
        return ResponseEntity.ok(user);
    }

    @GetMapping("/check-login")
    public ResponseEntity<Boolean> isLoggedIn() {
        boolean isLoggedIn = userService.isLoggedIn();
        return ResponseEntity.ok(isLoggedIn);
    }
} 