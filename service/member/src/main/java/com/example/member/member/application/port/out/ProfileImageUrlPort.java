package com.example.member.member.application.port.out;

public interface ProfileImageUrlPort {

    boolean isSupportedKey(String objectKey);

    String resolve(String objectKey);
}
