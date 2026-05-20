package com.example.member.member.application.port.out;

import com.example.member.member.domain.entity.Member;

// 회원 탈퇴 가능 여부를 검증하는 포트
public interface MemberWithdrawalCheckPort {

    void validateWithdrawable(Member member, String authorizationHeader);

}
