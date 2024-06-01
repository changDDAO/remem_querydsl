package com.changddao.remem_querydsl.repository;

import com.changddao.remem_querydsl.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long> {
    /*JPQL : "select m from Member m where m.username = :username"*/
    List<Member> findByUsername(String username);
}
