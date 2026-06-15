package com.example.member.member.infrastructure.messaging;

import com.example.member.member.application.event.MemberSignedUpEvent;
import com.example.member.member.application.port.out.MemberEventPort;
import com.example.member.member.domain.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberEventPublisher implements MemberEventPort {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publishMemberSignedUp(Member member) {
        applicationEventPublisher.publishEvent(new MemberSignedUpEvent(
                member.getMemberId(),
                member.getEmail()
        ));
    }
}
