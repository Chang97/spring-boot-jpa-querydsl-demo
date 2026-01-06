package com.demo.api.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.demo.api.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

}
