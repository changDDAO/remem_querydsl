package com.changddao.remem_querydsl;

import com.changddao.remem_querydsl.entity.Member;
import com.changddao.remem_querydsl.entity.QMember;
import com.changddao.remem_querydsl.entity.Team;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static com.changddao.remem_querydsl.entity.QMember.member;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;

    @BeforeEach
    public void before() {
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

    }
    @Test
    @DisplayName("JPQL")
    void startJPQL(){
    //given
        /*Member 1을 찾아라.*/
        Member findMember = em.createQuery("select m from Member m where m.username= :username", Member.class)
                .setParameter("username", "member1").getSingleResult();
    //when
        assertThat(findMember).isNotNull();
        assertThat(findMember.getAge()).isEqualTo(10);

    //then
    }

    @Test
    @DisplayName("Querydsl Test")
    void querydslTest(){
    //given
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
    //when
        Member findMember = queryFactory.select(member).from(member)
                .where(member.username.eq("member1")).fetchOne();
        //then
        assertThat(findMember).isNotNull();
        assertThat(findMember.getAge()).isEqualTo(10);
    }
}
