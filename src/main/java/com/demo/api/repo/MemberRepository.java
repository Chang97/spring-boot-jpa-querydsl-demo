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
    long countByActiveTrue();

    // 부분 일치 + 정렬 + 제한
    List<Member> findTop10ByUsernameContainingOrderByIdDesc(String keyword);

    // 대소문자 무시
    List<Member> findByEmailIgnoreCase(String email);

    // 날짜 범위(포함)
    List<Member> findByCreatedAtBetween(Instant from, Instant to);

    // 복합 조건
    List<Member> findByActiveTrueAndUsernameStartingWith(String prefix);
    List<Member> findByActiveTrueOrEmailEndingWith(String domain);

    // 정렬/페이징 파라미터 활용
    List<Member> findByActiveTrue(Sort sort);
    Page<Member>  findByActiveTrue(Pageable pageable);

    // 인터페이스 기반 프로젝션
    interface MemberSummary { Long getId(); String getUsername(); }
    Page<MemberSummary> findByActiveTrueAndUsernameContaining(String q, Pageable pageable);

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

    public record MemberItem(Long id, String username) {}

    @Query("""
        select new com.example.api.MemberItem(m.id, m.username)
        from Member m
        where m.active = true
    """)
    List<MemberItem> findActiveItems();
    
}
