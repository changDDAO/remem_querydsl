package com.changddao.remem_querydsl.controller;

import com.changddao.remem_querydsl.entity.Member;
import com.changddao.remem_querydsl.entity.Team;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
@RequiredArgsConstructor
public class InitMember {
    /*@RequiredArgsConstructor에 의해 의존성 주입*/
    private final InitMemberService initMemberService;

    @PostConstruct
    public void init() {
        initMemberService.init();
    }

    @Component
    static class InitMemberService {
        @PersistenceContext
        private EntityManager em;

        @Transactional
        public void init() {
            Team teamA = Team.builder().name("teamA").build();
            Team teamB = Team.builder().name("teamB").build();

            em.persist(teamA);
            em.persist(teamB);

            for(int i =0 ;i<100;i++) {
                Team selectedTeam = i % 2 == 0 ? teamA : teamB;
                em.persist(Member.builder().username("member"+i).age(i).team(selectedTeam).build());
            }
        }

    }

}
