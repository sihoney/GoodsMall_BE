package com.example.member.member.application.port.out;

import com.example.member.member.domain.entity.Member;

public interface MemberEventPort {

    void publishMemberSignedUp(Member member);
}
