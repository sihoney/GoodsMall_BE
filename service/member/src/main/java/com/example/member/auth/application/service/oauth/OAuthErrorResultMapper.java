package com.example.member.auth.application.service.oauth;


import com.example.member.common.exception.BusinessException;
import com.example.member.auth.application.dto.result.OAuthResult;
import com.example.member.auth.domain.enumtype.OAuthProvider;
import com.example.member.auth.exception.AuthErrorCode;
import org.springframework.stereotype.Component;

@Component
public class OAuthErrorResultMapper {

    public OAuthResult createErrorResult(
            OAuthProvider provider,
            Exception exception
    ) {
        String prefix = provider.name() + "_OAUTH";

        if (exception instanceof IllegalArgumentException) {
            return OAuthResult.error(
                    prefix + "_INVALID_REQUEST",
                    provider.name() + " OAuth 요청이 올바르지 않거나 만료되었습니다."
            );
        }

        if (exception instanceof BusinessException businessException
                && businessException.getErrorCode() == AuthErrorCode.INVALID_LOGIN) {
            return OAuthResult.error(
                    prefix + "_MEMBER_LOGIN_FAILED",
                    "OAuth 식별자에 연결된 회원 계정으로 로그인할 수 없습니다."
            );
        }

        String message = exception.getMessage();
        if ("OAUTH_EMAIL_REQUIRED".equals(message)
                || (provider.name() + "_OAUTH_EMAIL_REQUIRED").equals(message)) {
            return OAuthResult.error(
                    prefix + "_EMAIL_REQUIRED",
                    provider.name() + " 계정에서 이메일을 제공하지 않아 회원가입할 수 없습니다."
            );
        }
        if ("OAUTH_EMAIL_ALREADY_REGISTERED".equals(message)
                || (provider.name() + "_OAUTH_EMAIL_ALREADY_REGISTERED").equals(message)) {
            return OAuthResult.error(
                    prefix + "_EMAIL_ALREADY_REGISTERED",
                    "이미 가입된 이메일입니다. 이메일/비밀번호로 로그인해주세요."
            );
        }
        if ((provider.name() + "_OAUTH_EMAIL_NOT_VERIFIED").equals(message)) {
            return OAuthResult.error(
                    prefix + "_EMAIL_NOT_VERIFIED",
                    provider.name() + " 계정의 이메일이 검증되지 않아 회원가입할 수 없습니다."
            );
        }
        if ((provider.name() + "_TOKEN_EXCHANGE_FAILED").equals(message)) {
            return OAuthResult.error(
                    prefix + "_TOKEN_EXCHANGE_FAILED",
                    provider.name() + " OAuth 토큰 교환에 실패했습니다."
            );
        }
        if ((provider.name() + "_PROFILE_FETCH_FAILED").equals(message)) {
            return OAuthResult.error(
                    prefix + "_PROFILE_FETCH_FAILED",
                    provider.name() + " 사용자 프로필 조회에 실패했습니다."
            );
        }
        if ("UNSUPPORTED_OAUTH_PROVIDER".equals(message)) {
            return OAuthResult.error(
                    prefix + "_PROVIDER_UNSUPPORTED",
                    "지원하지 않는 OAuth provider입니다."
            );
        }

        return OAuthResult.error(
                prefix + "_UNKNOWN_ERROR",
                provider.name() + " 로그인 처리 중 알 수 없는 오류가 발생했습니다."
        );
    }
}
