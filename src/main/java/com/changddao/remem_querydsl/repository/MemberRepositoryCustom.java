package com.changddao.remem_querydsl.repository;

import com.changddao.remem_querydsl.dto.MemberSearchCondtion;
import com.changddao.remem_querydsl.dto.MemberTeamDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MemberRepositoryCustom {
    List<MemberTeamDto> search(MemberSearchCondtion condtion);
    Page<MemberTeamDto> searchPageSimple(MemberSearchCondtion condtion, Pageable pageable);
    Page<MemberTeamDto> searchPageComplex(MemberSearchCondtion condtion, Pageable pageable);


}
