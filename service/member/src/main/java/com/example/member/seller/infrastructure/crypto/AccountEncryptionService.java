package com.example.member.seller.infrastructure.crypto;

import com.example.member.verification.config.AccountVerificationCryptoProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountEncryptionService {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;

    private final AccountVerificationCryptoProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();

    public String encrypt(String plainText) {
        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, buildKey(), new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(iv) + ":" + Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception exception) {
            throw new IllegalStateException("계좌번호 암호화에 실패했습니다.", exception);
        }
    }

    public String decrypt(String encryptedValue) {
        try {
            String[] parts = encryptedValue.split(":", 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException("암호화된 계좌번호 형식이 올바르지 않습니다.");
            }

            byte[] iv = Base64.getDecoder().decode(parts[0]);
            byte[] encrypted = Base64.getDecoder().decode(parts[1]);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, buildKey(), new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception exception) {
            throw new IllegalStateException("계좌번호 복호화에 실패했습니다.", exception);
        }
    }

    private SecretKeySpec buildKey() throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = digest.digest(properties.secretKey().getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(keyBytes, "AES");
    }
}
