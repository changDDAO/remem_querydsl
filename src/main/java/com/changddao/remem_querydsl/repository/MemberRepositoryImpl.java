package com.changddao.remem_querydsl.repository;

import com.changddao.remem_querydsl.dto.MemberSearchCondtion;
import com.changddao.remem_querydsl.dto.MemberTeamDto;
import com.changddao.remem_querydsl.dto.QMemberTeamDto;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.changddao.remem_querydsl.entity.QMember.member;
import static com.changddao.remem_querydsl.entity.QTeam.team;
import static org.springframework.util.StringUtils.hasText;

@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom{
    private final JPAQueryFactory queryFactory;

    @Override
    public List<MemberTeamDto> search(MemberSearchCondtion cond) {
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

    @Override
    public Page<MemberTeamDto> searchPageSimple(MemberSearchCondtion cond, Pageable pageable) {
        QueryResults<MemberTeamDto> results = queryFactory.select(new QMemberTeamDto(
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
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        List<MemberTeamDto> content = results.getResults();
        long total = results.getTotal();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<MemberTeamDto> searchPageComplex(MemberSearchCondtion cond, Pageable pageable) {

        List<MemberTeamDto> content = queryFactory.select(new QMemberTeamDto(
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
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        return new PageImpl<>(content, pageable, content.size());

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
