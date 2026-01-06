package com.demo.api.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.demo.api.entity.Order;
import com.demo.api.entity.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByMemberUsernameAndStatusOrderByIdDesc(String username, OrderStatus status);
}
