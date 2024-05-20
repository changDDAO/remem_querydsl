package com.changddao.remem_querydsl.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@ToString(of = {"id", "name"})

public class Team {
    @Id
    @GeneratedValue
    @Column(name = "team_id")
    private Long id;
    /*팀 이름*/
    private String name;
    @OneToMany(mappedBy = "team")
    private List<Member> members = new ArrayList<>();

    @Builder
    public Team( String name, Member member) {
        this.name = name;
        if(member != null) {
            this.members.add(member);
            member.changeTeam(this);
        }
    }
}
