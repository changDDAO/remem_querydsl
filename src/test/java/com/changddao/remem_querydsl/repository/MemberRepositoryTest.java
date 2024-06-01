package com.changddao.remem_querydsl.repository;

import com.changddao.remem_querydsl.entity.Member;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@Transactional
class MemberRepositoryTest {
    @PersistenceContext
    EntityManager em;
    @Autowired
    private MemberRepository memberRepository;


    @Test
    @DisplayName("멤버 리포지토리 테스트")
    void save(){
        //given
        Member member = Member.builder().username("changho").age(30).build();
        //when
        memberRepository.save(member);
        em.flush();
        em.clear();
        Member findMember = memberRepository.findById(member.getId()).orElseThrow(RuntimeException::new);
        //then
        assertThat(member.getUsername()).isEqualTo(findMember.getUsername());

        List<Member> result = memberRepository.findAll();
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Querydsl 테스트")
    void querydsl_test(){
        //given
        Member member = Member.builder().username("changho").age(30).build();
        memberRepository.save(member);
        //when
        List<Member> result = memberRepository.findByUsername("changho");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsername()).isEqualTo("changho");
        assertThat(result).containsExactly(member);

        List<Member> results = memberRepository.findAll();
        assertThat(results).hasSize(1);
        assertThat(results.get(0)).isEqualTo(member);
        assertThat(results).containsExactly(member);
    }
}