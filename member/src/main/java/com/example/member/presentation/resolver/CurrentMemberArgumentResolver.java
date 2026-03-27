package com.example.member.presentation.resolver;

import com.example.member.domain.enumtype.MemberRole;
import com.example.member.common.exception.InvalidTokenException;
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
        // 컨트롤러 메서드의 파라미터가 @CurrentMember 애노테이션이 붙어있고, 타입이 AuthenticatedMember인지 확인
        return parameter.hasParameterAnnotation(CurrentMember.class)
                && parameter.getParameterType().equals(AuthenticatedMember.class);
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,          // 컨트롤러 메서드의 파라미터에 실제 값을 주입하는 역할
            ModelAndViewContainer mavContainer, // 컨트롤러 메서드의 모델과 뷰 정보를 담고 있는 객체
            NativeWebRequest webRequest,        // 현재 요청에 대한 정보를 담고 있는 객체
            WebDataBinderFactory binderFactory  // 컨트롤러 메서드의 파라미터에 바인딩할 때 필요한 객체
    ) {
        // HTTP 요청에서 사용자 정보를 추출하여 AuthenticatedMember 객체로 반환하는 로직

        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class); 
        if (request == null) {
            throw new InvalidTokenException();
        }

        // 요청 헤더에서 사용자 ID와 역할 정보를 추출
        String memberIdHeader = request.getHeader(MEMBER_ID_HEADER);
        String roleHeader = request.getHeader(MEMBER_ROLE_HEADER);

        if (memberIdHeader == null || memberIdHeader.isBlank() || roleHeader == null || roleHeader.isBlank()) {
            throw new InvalidTokenException();
        }

        try {
            // 추출한 정보를 바탕으로 AuthenticatedMember 객체를 생성하여 반환
            return new AuthenticatedMember(
                    UUID.fromString(memberIdHeader),
                    MemberRole.valueOf(roleHeader)
            );
        } catch (IllegalArgumentException exception) {
            throw new InvalidTokenException();
        }
    }
}
