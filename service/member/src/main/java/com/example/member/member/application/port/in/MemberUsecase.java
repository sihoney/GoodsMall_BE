package com.example.member.member.application.port.in;

import com.example.member.auth.application.dto.command.ChangePasswordCommand;
import com.example.member.member.application.dto.command.CreateMemberCommand;
import com.example.member.member.application.dto.command.UpdateMemberCommand;
import com.example.member.member.application.dto.command.WithdrawMemberCommand;
import com.example.member.member.application.dto.query.GetMemberQuery;
import com.example.member.auth.application.dto.result.ChangePasswordResult;
import com.example.member.member.application.dto.result.CreateMemberResult;
import com.example.member.member.application.dto.result.MemberResult;
import com.example.member.member.application.dto.result.WithdrawMemberResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public interface MemberUsecase {

    CreateMemberResult createMember(@Valid @NotNull CreateMemberCommand command);

    MemberResult getCurrentMember(@Valid @NotNull GetMemberQuery query);

    MemberResult updateCurrentMember(@Valid @NotNull UpdateMemberCommand command);

    MemberResult getMember(@Valid @NotNull GetMemberQuery query);

    ChangePasswordResult changeCurrentMemberPassword(@Valid @NotNull ChangePasswordCommand command);

    WithdrawMemberResult withdrawCurrentMember(@Valid @NotNull WithdrawMemberCommand command);
}
