package com.aolda.ojakgyo.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSignupDto {
    @NotBlank(message = "아이디는 필수 입력값입니다.")
    private String id;
    @NotBlank(message = "이메일은 필수 입력값입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;
    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    private String password;
    @NotBlank(message = "이름은 필수 입력값입니다.")
    private String name;
    private String address;
} 