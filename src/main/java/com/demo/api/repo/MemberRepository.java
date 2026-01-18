package com.demo.api.repo;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.demo.api.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByUsername(String username);
    boolean existsByEmail(String email);

    // 부분 일치 + 정렬 + 제한
    List<Member> findTop10ByUsernameContainingOrderByIdDesc(String keyword);

    // 대소문자 무시
    List<Member> findByEmailIgnoreCase(String email);

    // 날짜 범위(포함) — BaseAuditableEntity.createdAt 기준
    List<Member> findByCreatedAtBetween(Instant from, Instant to);

    // 복합 조건
    List<Member> findByUsernameStartingWith(String prefix);
    List<Member> findByUsernameStartingWithOrEmailEndingWith(String prefix, String domain);

    // 정렬/페이징 — JpaRepository 기본 메서드(findAll)로 대체 가능하지만, 유지 원하면 아래도 사용 가능
    List<Member> findByUsernameContaining(String q, Sort sort);
    Page<Member> findByEmailEndingWith(String domain, Pageable pageable);

    // 인터페이스 기반 프로젝션
    interface MemberSummary { Long getId(); String getUsername(); }
    Page<MemberSummary> findByUsernameContaining(String q, Pageable pageable);

    @Query(
        value = """
        select id, username, email
        from members
        where email like concat('%', :q, '%')
        order by id desc
        """,
        countQuery = "select count(*) from members where email like concat('%', :q, '%')",
        nativeQuery = true
    )
    Page<Object[]> searchNative(@Param("q") String q, Pageable pageable);

    // JPQL + 인터페이스 프로젝션 (별도 DTO 클래스 생성 없이)
    interface MemberItem { Long getId(); String getUsername(); }

    @Query("""
        select m.id as id, m.username as username
        from Member m
        """)
    List<MemberItem> findItems();
}
