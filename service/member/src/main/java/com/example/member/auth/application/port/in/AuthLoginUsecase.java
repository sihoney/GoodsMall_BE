package com.example.member.auth.application.port.in;

import com.example.member.auth.application.dto.command.LoginCommand;
import com.example.member.auth.application.dto.result.AuthTokenResult;
import com.example.member.common.application.dto.AuthSessionMetadata;
import com.example.member.member.domain.entity.Member;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public interface AuthLoginUsecase {

    AuthTokenResult login(@Valid @NotNull LoginCommand command);

    AuthTokenResult loginAuthenticatedMember(Member member, AuthSessionMetadata metadata);
}
