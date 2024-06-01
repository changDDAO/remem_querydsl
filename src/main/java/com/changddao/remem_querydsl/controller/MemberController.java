package com.changddao.remem_querydsl.controller;

import com.changddao.remem_querydsl.dto.MemberSearchCondtion;
import com.changddao.remem_querydsl.dto.MemberTeamDto;
import com.changddao.remem_querydsl.repository.MemberJpaRepository;
import com.changddao.remem_querydsl.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberJpaRepository memberJpaRepository;
    private final MemberRepository memberRepository;

    @GetMapping("/v1/members")
    public List<MemberTeamDto> searchMemberV1(@ModelAttribute MemberSearchCondtion condtion) {
        return memberJpaRepository.searchByMethod(condtion);
    }

    @GetMapping("/v2/members")
    public Page<MemberTeamDto> searchMemberV2(@ModelAttribute MemberSearchCondtion condtion, Pageable pageable) {
        return memberRepository.searchPageSimple(condtion, pageable);
    }
    @GetMapping("/v3/members")
    public Page<MemberTeamDto> searchMemberV3(@ModelAttribute MemberSearchCondtion condtion, Pageable pageable) {
        return memberRepository.searchPageSimple(condtion, pageable);
    }
}
