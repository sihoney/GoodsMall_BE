package com.todaylunch.common.security.auth.util;

import com.todaylunch.common.security.auth.dto.AuthenticatedMember;
import com.todaylunch.common.security.auth.enumtype.MemberRole;
import com.todaylunch.common.security.exception.AuthorizationDeniedException;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public final class RoleGuard {

    private RoleGuard() {
    }

    /**
     * 인증된 사용자가 존재하는지 검증합니다.
     * @param authenticatedMember
     */
    public static void requireAuthenticated(AuthenticatedMember authenticatedMember) {
        if (authenticatedMember == null) {
            throw new AuthorizationDeniedException("Authentication is required.");
        }
    }

    /**
     * 인증된 사용자가 requiredRole을 가지고 있는지 검증합니다.
     * @param authenticatedMember
     * @param requiredRole
     */
    public static void requireRole(
            AuthenticatedMember authenticatedMember,
            MemberRole requiredRole
    ) {
        requireAuthenticated(authenticatedMember);
        if (authenticatedMember.role() != requiredRole) {
            throw new AuthorizationDeniedException(requiredRole + " role is required.");
        }
    }

    /**
     * 인증된 사용자가 allowedRoles 중 하나라도 가지고 있는지 검증합니다.
     * @param authenticatedMember
     * @param allowedRoles
     */
    public static void requireAnyRole(
            AuthenticatedMember authenticatedMember,
            MemberRole... allowedRoles
    ) {
        requireAuthenticated(authenticatedMember);
        if (allowedRoles == null || allowedRoles.length == 0) {
            throw new IllegalArgumentException("At least one allowed role is required.");
        }

        boolean allowed = Arrays.stream(allowedRoles)
                .filter(Objects::nonNull)
                .anyMatch(role -> role == authenticatedMember.role());

        if (!allowed) {
            throw new AuthorizationDeniedException("Required role is missing.");
        }
    }

    /**
     * 인증된 사용자가 ADMIN 역할을 가지고 있는지 검증합니다.
     * @param authenticatedMember
     */
    public static void requireAdmin(AuthenticatedMember authenticatedMember) {
        requireRole(authenticatedMember, MemberRole.ADMIN);
    }

    /**
     * 인증된 사용자가 SELLER 또는 ADMIN 역할을 가지고 있는지 검증합니다.
     * @param authenticatedMember
     */
    public static void requireSellerOrAdmin(AuthenticatedMember authenticatedMember) {
        requireAnyRole(authenticatedMember, MemberRole.SELLER, MemberRole.ADMIN);
    }

    /**
     * 인증된 사용자가 리소스 소유자이거나 ADMIN 역할을 가지고 있는지 검증합니다.
     * @param authenticatedMember
     * @param resourceOwnerId
     */
    public static void requireOwnerOrAdmin(
            AuthenticatedMember authenticatedMember,
            UUID resourceOwnerId
    ) {
        requireAuthenticated(authenticatedMember);
        if (resourceOwnerId == null) {
            throw new IllegalArgumentException("resourceOwnerId is required.");
        }

        if (authenticatedMember.role() == MemberRole.ADMIN) {
            return;
        }

        if (!resourceOwnerId.equals(authenticatedMember.memberId())) {
            throw new AuthorizationDeniedException("Resource owner or ADMIN role is required.");
        }
    }
}
