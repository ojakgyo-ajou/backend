package com.aolda.ojakgyo.repository;

import com.aolda.ojakgyo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {

    // 사용자 이메일로 사용자 조회
    User findByEmail(String email);

    // 사용자 이름으로 사용자 조회
    User findByName(String name);
}
