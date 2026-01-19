package com.demo.api.repo;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.demo.api.code.OrderStatus;
import com.demo.api.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order>, OrderQueryRepository {
    
    // 상세 화면: fetch join(페이징 X)
    @Query("""
        select distinct o
        from Order o
        join fetch o.member
        left join fetch o.items i
        left join fetch i.product
        where o.id = :id
    """)
    Optional<Order> findDetail(@Param("id") Long id);

    // 목록: ID 페이지 → IN 조회(두 번 조회 전략)
    @Query("select o.id from Order o where o.status = :s")
    Page<Long> findIdsByStatus(@Param("s") OrderStatus s, Pageable pageable);

    @Query("""
        select o from Order o
        join fetch o.member
        where o.id in :ids
    """)
    List<Order> findWithMemberByIdIn(@Param("ids") Collection<Long> ids);

    @EntityGraph(attributePaths = {"member"})
    List<Order> findAllBy();

    // 필요 시 다대일만 미리 로딩
    @EntityGraph(attributePaths = {"member"})
    Page<Order> findAll(Specification<Order> spec, Pageable pageable);

    List<Order> findByMemberUsernameAndStatusOrderByIdDesc(String username, OrderStatus status);

    // 연관 객체 속성 접근
    List<Order> findByMemberUsername(String username);
    List<Order> findByMember_Username(String username); // 언더스코어도 허용

    // Enum + 기간 + 페이징
    Page<Order> findByStatusAndCreatedAtBetween(
        OrderStatus status, Instant from, Instant to, Pageable pageable);

    // 컬렉션 비/유무
    List<Order> findByItemsIsEmpty();
    List<Order> findByItemsIsNotEmpty();
}
