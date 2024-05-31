package com.changddao.remem_querydsl.repository;

import com.changddao.remem_querydsl.dto.MemberSearchCondtion;
import com.changddao.remem_querydsl.dto.MemberTeamDto;
import com.changddao.remem_querydsl.dto.QMemberTeamDto;
import com.changddao.remem_querydsl.entity.Member;
import com.changddao.remem_querydsl.entity.QMember;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

import static com.changddao.remem_querydsl.entity.QMember.*;
import static com.changddao.remem_querydsl.entity.QTeam.team;
import static org.springframework.util.StringUtils.*;

@Repository
@RequiredArgsConstructor
public class MemberJpaRepository {
    @PersistenceContext
    private EntityManager em;
    private final JPAQueryFactory queryFactory;

    public void save(Member member) {
        em.persist(member);
    }

    public Optional<Member> findById(Long id) {
        Member findMember = em.find(Member.class, id);
        return Optional.ofNullable(findMember);
    }

    /*JPA를 이용한 findAll*/
    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class).getResultList();
    }

    /*querydsl을 이용한 findAll*/
    public List<Member> findAll_Querydsl() {
        return queryFactory.selectFrom(member).fetch();
    }

    /*JPA를 이용하여 username을 갖는 멤버 리스트 가지고 오기*/
    public List<Member> findByUsername(String username) {
        List<Member> result = em.createQuery("select m from Member" +
                        " m where m.username = :username", Member.class).setParameter("username", username)
                .getResultList();
        return result;
    }

    /*Querydsl 이용하여 username을 갖는 멤버 리스트 가지고 오기*/
    public List<Member> findByUsername_Querydsl(String username) {
        List<Member> result = queryFactory.selectFrom(member)
                .where(member.username.eq(username))
                .fetch();
        return result;
    }

    public List<MemberTeamDto> searchByBuilder(MemberSearchCondtion cond) {
        //given
        BooleanBuilder builder = new BooleanBuilder();
        if (hasText(cond.getUsername())) {
            builder.and(member.username.eq(cond.getUsername()));
        }
        if (hasText(cond.getTeamName())) {
            builder.and(team.name.eq(cond.getTeamName()));
        }
        if (cond.getAgeLoe() != null) {
            builder.and(member.age.loe(cond.getAgeLoe()));
        }
        if (cond.getAgeGoe() != null) {
            builder.and(member.age.goe(cond.getAgeGoe()));
        }
        return queryFactory.select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                )).from(member)
                .leftJoin(member.team, team)
                .where(builder)
                .fetch();
        //when
        //then
    }

    /*username
teamName
 ageGoe;
 ageLoe;*/
    public List<MemberTeamDto> searchByMethod(MemberSearchCondtion cond) {
        //given

        return queryFactory.select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                )).from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(cond.getUsername())
                        , teamNameEq(cond.getTeamName())
                        , ageGoe(cond.getAgeGoe())
                        , ageLoe(cond.getAgeLoe()))
                .fetch();
        //when
        //then
    }

    private BooleanExpression usernameEq(String username) {
        return hasText(username) ? member.username.eq(username) : null;
    }

    private BooleanExpression teamNameEq(String teamName) {
        return hasText(teamName) ? team.name.eq(teamName) : null;
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe != null ? member.age.loe(ageLoe) : null;
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe != null ? member.age.goe(ageGoe) : null;
    }

}
