package com.demo.api.repo;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.demo.api.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByPriceLessThan(BigDecimal max);
    List<Product> findByNameIn(Collection<String> names);
    List<Product> findFirst5ByOrderByIdDesc();
}
