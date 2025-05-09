package com.aolda.ojakgyo.repository;

import com.aolda.ojakgyo.entity.Cart;
import com.aolda.ojakgyo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    List<Cart> findByUser(User user);
    
    Optional<Cart> findByUserAndProductId(User user, Long productId);
    
    @Query("SELECT c FROM Cart c JOIN FETCH c.product WHERE c.user = :user")
    List<Cart> findByUserWithProduct(@Param("user") User user);
    
    void deleteByUserAndProductId(User user, Long productId);
} 