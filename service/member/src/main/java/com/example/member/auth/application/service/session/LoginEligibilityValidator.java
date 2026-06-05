package com.example.member.auth.application.service.session;

import com.example.member.member.domain.entity.Member;
import com.example.member.member.domain.enumtype.MemberStatus;
import com.example.member.common.exception.BusinessException;
import com.example.member.verification.exception.VerificationErrorCode;
import com.example.member.member.exception.MemberErrorCode;

import com.example.member.restriction.application.service.MemberRestrictionService;
import com.example.member.restriction.domain.entity.MemberRestriction;
import com.example.member.restriction.exception.RestrictionErrorCode;
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
            throw new BusinessException(
                    RestrictionErrorCode.MEMBER_RESTRICTED,
                    "회원은 " + memberRestriction.getEndAt() + "까지 이용이 제한됩니다."
            );
        }
    }

    public void validateActiveMember(Member member) {
        switch (member.getStatus()) {
            case ACTIVE -> {
            }
            case PENDING_VERIFICATION -> throw new BusinessException(VerificationErrorCode.EMAIL_VERIFICATION_REQUIRED);
            case SUSPENDED -> throw new BusinessException(MemberErrorCode.MEMBER_SUSPENDED);
            case WITHDRAWN, DELETED -> throw new BusinessException(MemberErrorCode.MEMBER_WITHDRAWN);
        }
    }
}
