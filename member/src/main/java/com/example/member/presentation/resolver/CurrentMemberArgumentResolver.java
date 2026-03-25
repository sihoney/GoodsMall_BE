package com.example.member.presentation.resolver;

import com.example.member.domain.enumtype.MemberRole;
import com.example.member.domain.exception.InvalidTokenException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class CurrentMemberArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String MEMBER_ID_HEADER = "X-Member-Id";
    private static final String MEMBER_ROLE_HEADER = "X-Member-Role";

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

        String memberIdHeader = request.getHeader(MEMBER_ID_HEADER);
        String roleHeader = request.getHeader(MEMBER_ROLE_HEADER);

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
