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

public interface MemberUsecase {

    CreateMemberResult createMember(CreateMemberCommand command);

    MemberResult getCurrentMember(GetMemberQuery query);

    MemberResult updateCurrentMember(UpdateMemberCommand command);

    MemberResult getMember(GetMemberQuery query);

    ChangePasswordResult changeCurrentMemberPassword(ChangePasswordCommand command);

    WithdrawMemberResult withdrawCurrentMember(WithdrawMemberCommand command);
}
