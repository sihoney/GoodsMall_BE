package com.example.member.common.exception;

public class DuplicateActiveRestrictionException extends RuntimeException {

    public DuplicateActiveRestrictionException() {
        super("An active restriction of the same type already exists for this member.");
    }
}
