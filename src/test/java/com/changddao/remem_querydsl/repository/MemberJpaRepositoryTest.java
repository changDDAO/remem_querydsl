package com.changddao.remem_querydsl.repository;

import com.changddao.remem_querydsl.dto.MemberSearchCondtion;
import com.changddao.remem_querydsl.dto.MemberTeamDto;
import com.changddao.remem_querydsl.dto.QMemberTeamDto;
import com.changddao.remem_querydsl.entity.Member;
import com.changddao.remem_querydsl.entity.QMember;
import com.changddao.remem_querydsl.entity.QTeam;
import com.changddao.remem_querydsl.entity.Team;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.changddao.remem_querydsl.entity.QMember.*;
import static com.changddao.remem_querydsl.entity.QTeam.*;
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

    @Test
    @DisplayName("동적쿼리 테스트")
    void searchTest(){
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


    //when
        List<MemberTeamDto> memberTeamDtos =
                memberJpaRepository.searchByBuilder(MemberSearchCondtion.builder().ageGoe(13).build());
        //then
        assertThat(memberTeamDtos).hasSize(3);
        assertThat(memberTeamDtos).extracting("age").containsExactly(20, 30, 40);

        List<MemberTeamDto> result = memberJpaRepository
                .searchByBuilder(MemberSearchCondtion.builder().ageGoe(35).ageLoe(40).teamName("teamB").build());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTeamName()).isEqualTo("teamB");
        assertThat(result.get(0).getAge()).isEqualTo(40);
        assertThat(result.get(0).getUsername()).isEqualTo("member4");
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
        MemberSearchCondtion condtion = MemberSearchCondtion.builder().ageGoe(10).ageLoe(20).build();
        List<MemberTeamDto> result = memberJpaRepository.searchByMethod(condtion);
        //when
        assertThat(result).hasSize(2);
        assertThat(result).extracting("age").containsExactly(10,20);
        assertThat(result).extracting("username").containsExactly("member1", "member2");
    //then
    }




}