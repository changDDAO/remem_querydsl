package com.changddao.remem_querydsl.repository;

import com.changddao.remem_querydsl.entity.Member;
import com.changddao.remem_querydsl.entity.QMember;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.changddao.remem_querydsl.entity.QMember.*;

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
    public List<Member> findAll_Querydsl(){
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
}
