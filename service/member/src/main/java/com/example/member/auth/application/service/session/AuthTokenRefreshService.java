package com.example.member.auth.application.service.session;

import com.example.member.auth.application.dto.command.TokenRefreshCommand;
import com.example.member.auth.application.dto.result.AuthTokenResult;
import com.example.member.auth.application.port.in.AuthTokenRefreshUsecase;
import com.example.member.auth.exception.AuthErrorCode;
import com.example.member.auth.infrastructure.redis.auth.AuthSession;
import com.example.member.auth.infrastructure.redis.auth.ParsedRefreshToken;
import com.example.member.auth.infrastructure.redis.auth.RefreshTokenStore;
import com.example.member.auth.infrastructure.security.jwt.JwtTokenProvider;
import com.example.member.common.application.dto.AuthSessionMetadata;
import com.example.member.common.exception.BusinessException;
import com.example.member.member.application.port.out.MemberPersistencePort;
import com.example.member.member.domain.entity.Member;
import com.todaylunch.common.security.exception.InvalidTokenException;
import java.time.Duration;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@RequiredArgsConstructor
public class AuthTokenRefreshService implements AuthTokenRefreshUsecase {

    private final MemberPersistencePort memberPersistencePort;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenStore refreshTokenStore;
    private final LoginEligibilityValidator loginEligibilityValidator;

    @Override
    public AuthTokenResult refresh(TokenRefreshCommand command) {
        String refreshToken = command.refreshToken().trim();
        jwtTokenProvider.validateRefreshToken(refreshToken);

        ParsedRefreshToken parsedRefreshToken = jwtTokenProvider.parseRefreshToken(refreshToken);

        loginEligibilityValidator.validateLoginRestriction(parsedRefreshToken.memberId());

        AuthSession authSession = refreshTokenStore.findBySessionId(parsedRefreshToken.sessionId())
                .orElseThrow(() -> new BusinessException(AuthErrorCode.INVALID_TOKEN));

        if (!Objects.equals(authSession.memberId(), parsedRefreshToken.memberId())) {
            throw new InvalidTokenException();
        }

        if (!Objects.equals(authSession.refreshTokenId(), parsedRefreshToken.refreshTokenId())) {
            refreshTokenStore.deleteAllSessions(parsedRefreshToken.memberId());
            throw new InvalidTokenException();
        }

        Member member = memberPersistencePort.findById(parsedRefreshToken.memberId())
                .orElseThrow(InvalidTokenException::new);

        loginEligibilityValidator.validateActiveMember(member);

        String newAccessToken = jwtTokenProvider.createAccessToken(member, parsedRefreshToken.sessionId());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(member, parsedRefreshToken.sessionId());
        ParsedRefreshToken rotatedRefreshToken = jwtTokenProvider.parseRefreshToken(newRefreshToken);

        refreshTokenStore.updateRefreshTokenId(
                parsedRefreshToken.sessionId(),
                rotatedRefreshToken.refreshTokenId(),
                Duration.ofMillis(jwtTokenProvider.getRefreshExpiration()),
                metadataOrEmpty(command.authSessionMetadata())
        );

        return new AuthTokenResult(
                newAccessToken,
                newRefreshToken,
                "Bearer",
                jwtTokenProvider.getAccessExpiration(),
                jwtTokenProvider.getRefreshExpiration(),
                parsedRefreshToken.sessionId()
        );
    }

    private AuthSessionMetadata metadataOrEmpty(AuthSessionMetadata metadata) {
        return metadata == null ? AuthSessionMetadata.empty() : metadata;
    }
}
