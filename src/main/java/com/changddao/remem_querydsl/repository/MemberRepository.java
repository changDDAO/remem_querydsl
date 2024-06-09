package com.changddao.remem_querydsl.repository;

import com.changddao.remem_querydsl.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom, QuerydslPredicateExecutor<Member> {
    /*JPQL : "select m from Member m where m.username = :username"*/
    List<Member> findByUsername(String username);
}
