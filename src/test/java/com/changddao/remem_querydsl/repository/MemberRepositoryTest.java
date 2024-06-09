package com.changddao.remem_querydsl.repository;

import com.changddao.remem_querydsl.dto.MemberSearchCondtion;
import com.changddao.remem_querydsl.dto.MemberTeamDto;
import com.changddao.remem_querydsl.entity.Member;
import com.changddao.remem_querydsl.entity.QMember;
import com.changddao.remem_querydsl.entity.Team;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

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


    @Test
    @DisplayName("메소드를 생성하여 동적쿼리 작성")
    void search_dynamic_test(){
        //given
        Team teamA = Team.builder().name("teamA").build();
        Team teamB = Team.builder().name("teamB").build();
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = Member.builder().username("member1").age(10).team(teamA).build();
        Member member2 = Member.builder().username("member2").team(teamA).age(20).build();
        Member member3 = Member.builder().username("member3").age(30).team(teamB).build();
        Member member4 = Member.builder().username("member4").age(40).team(teamB).build();

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
        MemberSearchCondtion condtion = MemberSearchCondtion.builder().build();
        PageRequest pageRequest = PageRequest.of(0, 3);
        Page<MemberTeamDto> result = memberRepository.searchPageSimple(condtion, pageRequest);
        //when
        assertThat(result).hasSize(3);
        assertThat(result).extracting("age").containsExactly(10,20,30);
        assertThat(result).extracting("username").containsExactly("member1", "member2","member3");
        //then
    }

    @Test
    @DisplayName("querydsl PredicateExecutor테스트")
    void querydslPredicateExecutor_test(){
    //given
        Team teamA = Team.builder().name("teamA").build();
        Team teamB = Team.builder().name("teamB").build();
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = Member.builder().username("member1").age(10).team(teamA).build();
        Member member2 = Member.builder().username("member2").team(teamA).age(20).build();
        Member member3 = Member.builder().username("member3").age(30).team(teamB).build();
        Member member4 = Member.builder().username("member4").age(40).team(teamB).build();

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
        QMember member = QMember.member;
        Iterable<Member> result = memberRepository.findAll(member.age.between(10, 40)
                .and(member.username.eq("member1")));
        //when
        for (Member el : result) {
            System.out.println("member1 = " + el);
        }

    //then
    }



}