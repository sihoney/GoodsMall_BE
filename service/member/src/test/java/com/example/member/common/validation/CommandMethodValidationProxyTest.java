package com.example.member.common.validation;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.member.auth.application.dto.command.LoginCommand;
import com.example.member.auth.application.port.in.AuthLoginUsecase;
import com.example.member.auth.application.service.session.AuthLoginService;
import com.example.member.auth.application.service.session.AuthTokenIssuer;
import com.example.member.auth.application.service.session.LoginEligibilityValidator;
import com.example.member.member.application.port.out.MemberPersistencePort;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

@SpringJUnitConfig(CommandMethodValidationProxyTest.TestConfig.class)
class CommandMethodValidationProxyTest {

    private final AuthLoginUsecase authLoginUsecase;
    private final MemberPersistencePort memberPersistencePort;

    @Autowired
    CommandMethodValidationProxyTest(
            AuthLoginUsecase authLoginUsecase,
            MemberPersistencePort memberPersistencePort
    ) {
        this.authLoginUsecase = authLoginUsecase;
        this.memberPersistencePort = memberPersistencePort;
    }

    @Test
    void usecaseCallThroughSpringProxy_invalidCommand_throwsConstraintViolationBeforeServiceLogic() {
        LoginCommand invalidCommand = new LoginCommand("", "password", null);

        assertThrows(
                ConstraintViolationException.class,
                () -> authLoginUsecase.login(invalidCommand)
        );
        verify(memberPersistencePort, never()).findByEmail("");
    }

    @Configuration
    static class TestConfig {

        @Bean
        static MethodValidationPostProcessor methodValidationPostProcessor() {
            return new MethodValidationPostProcessor();
        }

        @Bean
        AuthLoginUsecase authLoginUsecase(
                MemberPersistencePort memberPersistencePort,
                PasswordEncoder passwordEncoder,
                LoginEligibilityValidator loginEligibilityValidator,
                AuthTokenIssuer authTokenIssuer
        ) {
            return new AuthLoginService(
                    memberPersistencePort,
                    passwordEncoder,
                    loginEligibilityValidator,
                    authTokenIssuer
            );
        }

        @Bean
        MemberPersistencePort memberPersistencePort() {
            return mock(MemberPersistencePort.class);
        }

        @Bean
        PasswordEncoder passwordEncoder() {
            return mock(PasswordEncoder.class);
        }

        @Bean
        LoginEligibilityValidator loginEligibilityValidator() {
            return mock(LoginEligibilityValidator.class);
        }

        @Bean
        AuthTokenIssuer authTokenIssuer() {
            return mock(AuthTokenIssuer.class);
        }
    }
}
