package com.changddao.remem_querydsl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
public class MemberSearchCondtion {
    /*회원명, 팀명, 나이 (ageGoe, ageLoe)*/
    private String username;
    private String teamName;
    private Integer ageGoe;
    private Integer ageLoe;


    @Builder
    public MemberSearchCondtion(String username, String teamName, Integer ageGoe, Integer ageLoe) {
        this.username = username;
        this.teamName = teamName;
        this.ageGoe = ageGoe;
        this.ageLoe = ageLoe;
    }
}
