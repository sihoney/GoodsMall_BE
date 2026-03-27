package com.todaylunch.common.security.auth.resolver;

import com.todaylunch.common.security.auth.annotation.CurrentMember;
import com.todaylunch.common.security.auth.constant.AuthHeaders;
import com.todaylunch.common.security.auth.dto.AuthenticatedMember;
import com.todaylunch.common.security.auth.enumtype.MemberRole;
import com.todaylunch.common.security.exception.InvalidTokenException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class CurrentMemberArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentMember.class)
                && parameter.getParameterType().equals(AuthenticatedMember.class);
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request == null) {
            throw new InvalidTokenException();
        }

        String memberIdHeader = request.getHeader(AuthHeaders.MEMBER_ID);
        String roleHeader = request.getHeader(AuthHeaders.MEMBER_ROLE);

        if (memberIdHeader == null || memberIdHeader.isBlank() || roleHeader == null || roleHeader.isBlank()) {
            throw new InvalidTokenException();
        }

        try {
            return new AuthenticatedMember(
                    UUID.fromString(memberIdHeader),
                    MemberRole.valueOf(roleHeader)
            );
        } catch (IllegalArgumentException exception) {
            throw new InvalidTokenException();
        }
    }
}
