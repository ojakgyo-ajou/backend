package com.aolda.ojakgyo.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLoginDto {
    
    @NotBlank(message = "아이디는 필수 입력값입니다.")
    private String id;
    
    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    private String password;
} 