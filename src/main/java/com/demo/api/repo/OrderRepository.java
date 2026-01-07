package com.demo.api.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.demo.api.code.OrderStatus;
import com.demo.api.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("""
        select distinct o
        from Order o
        join fetch o.member
        left join fetch o.items i
        left join fetch i.product
        where o.id = :id
    """)
    Optional<Order> findDetail(@Param("id") Long id);

    @EntityGraph(attributePaths = {"member"})
    List<Order> findAllBy();

    List<Order> findByMemberUsernameAndStatusOrderByIdDesc(String username, OrderStatus status);
}
