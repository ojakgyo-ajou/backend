package com.aolda.ojakgyo.repository;

import com.aolda.ojakgyo.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
} 