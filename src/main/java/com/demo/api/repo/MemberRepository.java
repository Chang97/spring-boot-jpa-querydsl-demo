package com.demo.api.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.demo.api.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

}
