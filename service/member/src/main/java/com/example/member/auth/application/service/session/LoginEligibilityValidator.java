package com.example.member.auth.application.service.session;

import com.example.member.member.domain.entity.Member;
import com.example.member.member.domain.enumtype.MemberStatus;
import com.example.member.member.exception.MemberSuspendedException;
import com.example.member.member.exception.MemberWithdrawnException;
import com.example.member.restriction.application.service.MemberRestrictionService;
import com.example.member.restriction.domain.entity.MemberRestriction;
import com.example.member.restriction.exception.MemberRestrictedException;
import com.example.member.verification.exception.EmailVerificationRequiredException;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoginEligibilityValidator {

    private final MemberRestrictionService memberRestrictionService;

    public void validate(Member member) {
        validateActiveMember(member);
        validateLoginRestriction(member.getMemberId());
    }

    public void validateLoginRestriction(UUID memberId) {
        MemberRestriction memberRestriction = memberRestrictionService.getActiveLoginRestriction(
                memberId,
                LocalDateTime.now()
        );
        if (memberRestriction != null) {
            throw new MemberRestrictedException(memberRestriction.getEndAt());
        }
    }

    public void validateActiveMember(Member member) {
        switch (member.getStatus()) {
            case ACTIVE -> {
            }
            case PENDING_VERIFICATION -> throw new EmailVerificationRequiredException();
            case SUSPENDED -> throw new MemberSuspendedException();
            case WITHDRAWN, DELETED -> throw new MemberWithdrawnException();
        }
    }
}
