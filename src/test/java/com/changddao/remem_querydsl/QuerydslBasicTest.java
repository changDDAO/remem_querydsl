package com.changddao.remem_querydsl;

import com.changddao.remem_querydsl.entity.Member;
import com.changddao.remem_querydsl.entity.QMember;
import com.changddao.remem_querydsl.entity.Team;
import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.awt.print.Pageable;
import java.util.List;

import static com.changddao.remem_querydsl.entity.QMember.member;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;
    private JPAQueryFactory queryFactory;

    @BeforeEach
    public void before() {
        queryFactory = new JPAQueryFactory(em);
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

    //when
        Member findMember = queryFactory.select(member).from(member)
                .where(member.username.eq("member1")).fetchOne();
        //then
        assertThat(findMember).isNotNull();
        assertThat(findMember.getAge()).isEqualTo(10);
    }

    @Test
    @DisplayName("QueryDsl_검색")
    void search(){
    //given
        Member findMember = queryFactory.selectFrom(member)
                .where(member.username.eq("member1").and(member.age.eq(10))).fetchOne();
    //when
        assertThat(findMember).isNotNull();
        assertThat(findMember.getAge()).isEqualTo(10);
        assertThat(findMember.getUsername()).isEqualTo("member1");
    //then
    }

    @Test
    @DisplayName("querydsl을 이용한 다양한 조회방법")
    void resultFetch(){
    //given
        /*List로 조회*/
        List<Member> result1 = queryFactory.selectFrom(member).fetch();
        /*단건 조회
        * but, 단건이 아니기 때문에 Exception 발생!!
        * */
//        Member result2 = queryFactory.selectFrom(member).fetchOne();

        /*조회 결과에 따른 첫번째 member 조회*/
        Member fetchFirst = queryFactory.selectFrom(member).fetchFirst();


        //when
    //then
    }
    @Test
    @DisplayName("페이징_쿼리")
    void pagingQuery(){
        /*페이징 쿼리*/
        /*queryDsl의 fetchResult의 경우 count를 하기위해선 count용 쿼리를 만들어서 실행해야 하는데, 카운트를 하려는 select 쿼리를 기반으로 count 쿼리를 만들어 실행한다.
        위의 전문을 보면 이런 식인 것 같다.
                SELECT COUNT(*) FROM (<original query>).

        그런데 이게 단순한 쿼리에서는 잘 동작하는데, 복잡한 쿼리(다중그룹 쿼리)에서는 잘 작동하지 않는다고 한다.

                찾아보니 groupby having 절을 사용하는 등의 복잡한 쿼리 문에서 예외가 떠버리는 듯.

                더불어 대부분의 dialect에서는 count쿼리가 유효하지만 JPQL에서는 아니란다. 더 찾아보니 모든 dialect에서 지원하는 것도 아니라고 한다.

        그렇기 때문에 카운트하려면 그냥 fetch() 를 쓰고 따로 자바쪽에서 count를 세서 사용하라는 것 같다.
        -> fetchResults() 및 fetchCount()가 deprecated 된 이유.
        */
    //given
        List<Member> result = queryFactory.selectFrom(member).where(member.age.gt(10)).fetch();
        PageRequest pageRequest = PageRequest.of(1, 10, Sort.by("age"));
        //when
    //then
//        return new PageImpl<>(result,pageRequest,result.size());
    }
}
