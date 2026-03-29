package com.example.member.domain.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.member.domain.enumtype.MemberStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import com.todaylunch.common.security.auth.enumtype.MemberRole;
import org.junit.jupiter.api.Test;

class MemberTest {

    @Test
    void updateAccount_updatesAllMutableFields() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 3, 27, 10, 0);
        LocalDateTime updatedAt = createdAt.plusHours(1);
        Member member = Member.create(
                UUID.randomUUID(),
                "before@test.com",
                "before-password",
                "before-nickname",
                "010-0000-0000",
                "Old address",
                "old-image",
                MemberRole.USER,
                MemberStatus.ACTIVE,
                createdAt,
                createdAt
        );

        member.updateAccount(
                "after@test.com",
                "after-password",
                "after-nickname",
                "010-9999-9999",
                "New address",
                "new-image",
                updatedAt
        );

        assertEquals("after@test.com", member.getEmail());
        assertEquals("after-password", member.getPassword());
        assertEquals("after-nickname", member.getNickname());
        assertEquals("010-9999-9999", member.getPhone());
        assertEquals("New address", member.getAddress());
        assertEquals("new-image", member.getProfileImageUrl());
        assertEquals(updatedAt, member.getUpdatedAt());
    }
}
