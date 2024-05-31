package com.changddao.remem_querydsl.repository;

import com.changddao.remem_querydsl.entity.Member;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberJpaRepository memberJpaRepository;
    private JPAQueryFactory queryFactory;

    @Test
    @DisplayName("멤버 리포지토리 테스트")
    void save(){
    //given
        Member member = Member.builder().username("changho").age(30).build();
        //when
        memberJpaRepository.save(member);
        em.flush();
        em.clear();
        Member findMember = memberJpaRepository.findById(member.getId()).orElseThrow(RuntimeException::new);
        //then
        assertThat(member.getUsername()).isEqualTo(findMember.getUsername());

        List<Member> result = memberJpaRepository.findAll();
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Querydsl 테스트")
    void querydsl_test(){
    //given
        Member member = Member.builder().username("changho").age(30).build();
        memberJpaRepository.save(member);
    //when
        List<Member> result = memberJpaRepository.findByUsername_Querydsl("changho");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("changho");
        assertThat(result).containsExactly(member);

        List<Member> results = memberJpaRepository.findAll_Querydsl();
        assertThat(results).hasSize(1);
        assertThat(results.get(0)).isEqualTo(member);
        assertThat(results).containsExactly(member);
    }
}