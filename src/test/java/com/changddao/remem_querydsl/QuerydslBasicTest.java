package com.changddao.remem_querydsl;

import com.changddao.remem_querydsl.dto.MemberDto;
import com.changddao.remem_querydsl.dto.QMemberDto;
import com.changddao.remem_querydsl.dto.UserDto;
import com.changddao.remem_querydsl.entity.Member;
import com.changddao.remem_querydsl.entity.QMember;
import com.changddao.remem_querydsl.entity.QTeam;
import com.changddao.remem_querydsl.entity.Team;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.*;
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
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.awt.print.Pageable;
import java.util.List;

import static com.changddao.remem_querydsl.entity.QMember.member;
import static com.changddao.remem_querydsl.entity.QTeam.*;
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
    void startJPQL() {
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
    void querydslTest() {
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
    void search() {
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
    void resultFetch() {
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
    void pagingQuery() {
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

    @Test
    @DisplayName("정렬 테스트")
    void sort() {
        //given
        em.persist(Member.builder().username(null).age(100).build());
        em.persist(Member.builder().username("member5").age(100).build());
        em.persist(Member.builder().username("member6").age(100).build());
        //when
        List<Member> result = queryFactory.selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();
        //then
        assertThat(result).isNotNull();
        assertThat(result.get(result.size() - 1).getUsername()).isNull();
    }

    @Test
    @DisplayName("페이징 테스트")
    void paging1() {
        //given
        List<Member> result = queryFactory.selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetch();
        //when
        int size = result.size();
        //then
        assertThat(size).isEqualTo(2);
    }

    @Test
    @DisplayName("패이징테스트2")
    void paging2() {
        //given
        em.persist(Member.builder().username(null).age(100).build());
        em.persist(Member.builder().username("member5").age(100).build());
        em.persist(Member.builder().username("member6").age(100).build());
        QueryResults<Member> results = queryFactory.selectFrom(member)
                .where(member.age.eq(100))
                .offset(1)
                .limit(2)
                .fetchResults();

        //when
        long total = results.getTotal();
        assertThat(total).isEqualTo(3L);
        //then
    }

    @Test
    @DisplayName("집합함수")
    void aggregation() {
        //given
        List<Tuple> result = queryFactory.select(member.age.max(), member.age.avg(), member.age.sum(), member.age.min())
                .from(member)
                .fetch();
        //when
        Tuple tuple = result.get(0);
        /*10,20,30,40*/
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
        //then

    }

    /* 팀의 이름과 각 팀의 평균연령을 구해라.
     * */
    @Test
    @DisplayName("group함수 사용")
    void group() {
        //given
        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();
        //when
        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);
        assertThat(teamA.get(member.age.avg())).isNotNull();
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);
        assertThat(teamB.get(member.age.avg())).isNotNull();
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);

        //then
    }

    /*팀A에 소속된 모든 회원
     * */
    @Test
    @DisplayName("조인_테스트")
    void join() {
        //given
        List<Member> result = queryFactory.selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();
        //when
        assertThat(result).extracting("username")
                .containsExactly("member1", "member2");
        //then
    }

    /*
    세타조인(회원의 이름이 팀이름과 같은 회원 조회)
    * */
    @Test
    @DisplayName("연관관계가 없는 조인 테스트")
    void thetaJoin() {
        //given
        em.persist(Member.builder().username("teamA").build());
        em.persist(Member.builder().username("teamB").build());
        //when
        List<Member> result = queryFactory.select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();
        //then
        assertThat(result).extracting("username")
                .containsExactly("teamA", "teamB");
    }

    /*
     * 예) 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
     * */
    @Test
    @DisplayName("join on에서 필터링 테스트")
    void joinFiltering() {
        //given
        List<Tuple> result = queryFactory.select(member, team)
                .from(member)
                .leftJoin(member.team, team).on(team.name.eq("teamA"))
                .fetch();
        //when
        assertThat(result.size()).isEqualTo(4);
        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
        //then
    }

    /*
    연관관계가 없는 엔티티 외부 조인
    회원의 이름이 팀 이름과 같은 대상 외부조인
    * */
    @Test
    @DisplayName("연관관계가 없는 조인 테스트")
    void join_on_no_relation() {
        //given
        em.persist(Member.builder().username("teamA").build());
        em.persist(Member.builder().username("teamB").build());
        //when
        List<Tuple> result = queryFactory.select(member, team)
                .from(member)
                .leftJoin(team).on(member.username.eq(team.name))
                .fetch();

        //then
        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    @DisplayName("페치조인이 아닌거")
    void fetchJoinNO() {
        //given
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();
        //when
        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        /*Member Entity에 Team이 load 되었는지 check*/
        assertThat(loaded).isFalse();
        //then
    }

    @Test
    @DisplayName("페치조인 적용")
    void fetchJoinUse() {
        //given
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();
        //when
        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        /*Member Entity에 Team이 load 되었는지 check*/
        assertThat(loaded).isTrue();
        //then
    }

    /*나이가 가장 많은 회원 조회*/
    @Test
    @DisplayName("서브쿼리_테스트")
    void subquery() {
//given
        QMember memberSub = new QMember("memberSub");
        List<Member> result = queryFactory.selectFrom(member)
                .where(member.age.eq(JPAExpressions.select(memberSub.age.max()).from(memberSub))).fetch();
//when
        assertThat(result).extracting("age").containsExactly(40);
//then
    }

    /*나이가 평균 이상인 회원*/
    @Test
    @DisplayName("서브쿼리 평균이상")
    void subqueryGoe() {
//given
        QMember memberSub = new QMember("memberSub");
        List<Member> result = queryFactory.selectFrom(member)
                .where(member.age.goe(JPAExpressions.select(memberSub.age.avg()).from(memberSub))).fetch();
//when
        assertThat(result).extracting("username").containsExactly("member3", "member4");
//then
    }


    /*나이가 평균 이상인 회원*/
    @Test
    @DisplayName("서브쿼리 평균이상")
    void subqueryIn() {
//given
        QMember memberSub = new QMember("memberSub");
        List<Member> result = queryFactory.selectFrom(member)
                .where(member.age.in(
                        JPAExpressions.select(memberSub.age)
                                .from(memberSub)
                                .where(memberSub.age.goe(10))
                ))
                .fetch();
//when
        assertThat(result).extracting("username").containsExactly("member1", "member2", "member3", "member4");
//then
    }

    @Test
    @DisplayName("where절 서브쿼리 테스트")
    void whereSubQuery() {
        //given
        QMember memberSub = new QMember("memberSub");

        List<Tuple> result = queryFactory.select(member.username,
                        JPAExpressions.select(memberSub.age.avg())
                                .from(memberSub))
                .from(member)
                .fetch();
        //when
        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
        //then
    }

    @Test
    @DisplayName("case절 테스트")
    void basicCase() {
        //given
        List<String> results = queryFactory.select(member.age
                        .when(10).then("열살")
                        .when(20).then("스무살")
                        .otherwise("기타")
                ).from(member)
                .fetch();
        //when
        for (String result : results) {
            System.out.println("result = " + result);
        }
        //then
    }

    @Test
    @DisplayName("복잡할때 case절 test")
    void complexCase() {
        //given
        List<String> result = queryFactory.select(new CaseBuilder()
                        .when(member.age.between(0, 20)).then("0~20살")
                        .when(member.age.between(21, 30)).then("21~30살")
                        .otherwise("31살이상")
                ).from(member)
                .fetch();

        //when
        for (String s : result) {
            System.out.println("s = " + s);
        }
        //then
    }

    @Test
    @DisplayName("문자_더하기")
    void concat() {
        //given
        List<String> result = queryFactory.select(member.username.concat("_").concat(member.age.stringValue()))
                .from(member)
                .fetch();
        //when
        for (String s : result) {
            System.out.println("s = " + s);
        }
        //then
    }

    @Test
    @DisplayName("튜플_프로젝션")
    void tupleProjection() {
        //given
        List<Tuple> result = queryFactory
                .select(member.username, member.age)
                .from(member)
                .fetch();
        //when
        for (Tuple tuple : result) {
            String username = tuple.get(member.username);
            Integer age = tuple.get(member.age);
            System.out.println("username = " + username);
            System.out.println("age = " + age);
        }
        //then
    }

    @Test
    @DisplayName("JPQL을 이용하여 DTO 조회")
    void findDtoByJPQL() {
        //given
        List<MemberDto> result = em.createQuery("select new com.changddao.remem_querydsl.dto.MemberDto(m.username, m.age)  from Member m", MemberDto.class)
                .getResultList();
        //when
        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
        //then
    }

    @Test
    @DisplayName("Projection을 이용한 Dto 조회")
    void findDtoBySetter() {
        //given
        List<MemberDto> result = queryFactory.select(Projections.bean(MemberDto.class,
                        member.username,
                        member.age)
                ).from(member)
                .fetch();
        //when
        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
        //then
    }

    @Test
    @DisplayName("Projection을 이용한 Dto 조회2")
    void findDtoByField() {
        //given
        List<MemberDto> result = queryFactory.select(Projections.fields(MemberDto.class,
                        member.username,
                        member.age)
                ).from(member)
                .fetch();
        //when
        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
        //then
    }

    @Test
    @DisplayName("Projection을 이용한 Dto 조회3")
    void findDtoByConstructor() {
        //given
        List<MemberDto> result = queryFactory.select(Projections.constructor(MemberDto.class,
                        member.username,
                        member.age)
                ).from(member)
                .fetch();
        //when
        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }
    /*필드로 조회할때 발생할 수 있는 필드명이 다른 것을 alias를 사용하여 매칭 시키는 방법*/
    @Test
    @DisplayName("alias 사용하기")
    void findUserDto(){
    //given
        QMember memberSub = new QMember("memberSub");
        List<UserDto> result = queryFactory.select(Projections.fields(UserDto.class,
                        ExpressionUtils.as(member.username, "name"),
                        ExpressionUtils.as(JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub), "age")
                ))
                .from(member)
                .fetch();
        //when
        for (UserDto userDto : result) {
            System.out.println("userDto = " + userDto);
        }
    //then
    }

    @Test
    @DisplayName("Constructor을 이용한 dto조회")
    void findByConstructor() {
        //given
        List<UserDto> result = queryFactory.select(Projections.constructor(UserDto.class
                        , member.username
                        , member.age))
                .from(member)
                .fetch();

        //when
        for(UserDto userDto : result) {
            System.out.println("userDto = " + userDto);
        }
        //then
    }

    @Test
    @DisplayName("쿼리 프로젝션")
    void queryProjection(){
    //given
        List<MemberDto> result = queryFactory.select(new QMemberDto(member.username, member.age))
                .from(member)
                .fetch();
        //when
        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);

        }
    //then
    }
    @Test
    @DisplayName("QueryDsl 동적쿼리")
    void dynamicQuery_BooleanBuilder(){
    //given
        String username = "member1";
        Integer age = null;
        //when
        List<Member> result = getMemberSearch(username, age);
        //then
        assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> getMemberSearch(String usernameCond, Integer ageCond) {
        BooleanBuilder builder = new BooleanBuilder();
        if(usernameCond != null) {
            builder.and(member.username.eq(usernameCond));
        }
        if(ageCond != null) {
            builder.and(member.age.eq(ageCond));
        }
        return queryFactory.select(member)
                .from(member)
                .where(builder).fetch();
    }

    @Test
    @DisplayName("QueryDsl 동적쿼리2")
    void dynamicQuery_BooleanBuilder2(){
        //given
        String username = "member1";
        Integer age = null;
        //when
        List<Member> result = getMemberSearch2(username, age);
        //then
        assertThat(result.size()).isEqualTo(1);
    }


    @Test
    @DisplayName("QueryDsl 동적쿼리3")
    void dynamicQuery_BooleanBuilder3(){
        //given
        String username = "member1";
        Integer age = null;
        //when
        List<Member> result = getMemberSearch3(username, age);
        //then
        assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> getMemberSearch2(String usernameCond, Integer ageCond) {
        List<Member> result = queryFactory.select(member)
                .from(member)
                .where(usernameEq(usernameCond), ageEq(ageCond))
                .fetch();
        return result;
    }

    private List<Member> getMemberSearch3(String usernameCond, Integer ageCond) {
        List<Member> result = queryFactory.select(member)
                .from(member)
                .where(allEq(usernameCond, ageCond))
                .fetch();
        return result;
    }

    private BooleanExpression ageEq(Integer ageCond) {
        return ageCond != null ? member.age.eq(ageCond) : null;

    }

    private BooleanExpression usernameEq(String usernameCond) {
        return usernameCond != null ? member.username.eq(usernameCond) : null;
    }

    private BooleanExpression allEq(String usernameCond, Integer ageCond) {
        return usernameEq(usernameCond).and(ageEq(ageCond));
    }

    @Test
    @DisplayName("벌크 업데이트")
    @Commit
    void bulkUpdate(){
    //given
        /*(영속성 컨텍스트)member1=10 -> 1 (DB) member1*/
        /*(영속성 컨텍스트)member2=20 -> 2 (DB) member2*/
        /*(영속성 컨텍스트)member3=30 -> 3 (DB) member3*/
        /*(영속성 컨텍스트)member4=40 -> 4 (DB) member4*/
        long count = queryFactory
                .update(member)
                .set(member.username, "비회원")
                .where(member.age.lt(28))
                .execute();
        /*(영속성 컨텍스트)member1=10 -> 1 (DB) 비회원*/
        /*(영속성 컨텍스트)member2=20 -> 2 (DB) 비회원*/
        /*(영속성 컨텍스트)member3=30 -> 3 (DB) member3*/
        /*(영속성 컨텍스트)member4=40 -> 4 (DB) member4*/
        //when
        assertThat(count).isEqualTo(2);
        List<Member> result = queryFactory.select(member)
                .from(member)
                .fetch();
        //then
        /*DB에 바로 업데이트 쿼리를 날리기 때문에 영속성 컨텍스트에 담겨있는 member Entity는 값변경이
        이뤄지지 않는다.
        그러므로 벌크업데이트 후에는 필히 영속성컨텍스트를 DB와 동기화하고 비워줄 필요가 있다.
        em.flush();
        em.clear();
        * */
        for (Member member1 : result) {
            System.out.println("member1 = " + member1);
        }

    }
    @Test
    @DisplayName("벌크 업데이트")
//    .multiply() 곱하기 , divide() 나누기
    void bulkAdd(){
    //given
        long count = queryFactory.update(member)
                .set(member.age, member.age.add(1))
                .execute();
        //when
        assertThat(count).isEqualTo(4);
    //then
    }
    @Test
    @DisplayName("벌크 삭제")
    void bulkDelete(){
        //given
        long count = queryFactory.update(member)
                .set(member.age, member.age.add(1))
                .execute();
        //when
        assertThat(count).isEqualTo(4);
        //then
    }
    @Test
    @DisplayName("sql함수사용")
    void sqlFunctions(){
    //given
        List<String> result = queryFactory
                .select(Expressions.stringTemplate(
                        "function('replace',{0}, {1}, {2})",
                        member.username, "member", "M"))
                .from(member)
                .fetch();
        //when
        for (String s : result) {
            System.out.println("s = " + s);
        }
    //then
    }
}




