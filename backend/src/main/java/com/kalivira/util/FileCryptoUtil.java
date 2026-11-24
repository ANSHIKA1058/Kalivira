
package com.kalivira.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

public class FileCryptoUtil {

    private static final int SALT_LENGTH = 16;
    private static final int IV_LENGTH = 12;
    private static final int ITERATIONS = 600000;
    private static final int KEY_LENGTH = 256;
    private static final int GCM_TAG_LENGTH = 128;

    private static final SecureRandom secureRandom = new SecureRandom();

    public record WrappedDek(
            String salt,
            String iv,
            String encryptedDek
    ) {}

    private static byte[] deriveKey(String password, byte[] salt)
            throws GeneralSecurityException {

        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                salt,
                ITERATIONS,
                KEY_LENGTH
        );

        try {
            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            return factory.generateSecret(spec).getEncoded();

        } finally {
            spec.clearPassword();
        }
    }

    public static WrappedDek wrapDek(
            byte[] dek,
            String password
    ) throws GeneralSecurityException {

        byte[] salt = new byte[SALT_LENGTH];
        byte[] iv = new byte[IV_LENGTH];

        secureRandom.nextBytes(salt);
        secureRandom.nextBytes(iv);

        byte[] key = deriveKey(password, salt);

        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    new SecretKeySpec(key, "AES"),
                    new GCMParameterSpec(GCM_TAG_LENGTH, iv)
            );

            byte[] encryptedDek = cipher.doFinal(dek);

            Base64.Encoder encoder = Base64.getEncoder();

            return new WrappedDek(
                    encoder.encodeToString(salt),
                    encoder.encodeToString(iv),
                    encoder.encodeToString(encryptedDek)
            );

        } finally {
            Arrays.fill(key, (byte) 0);
        }
    }

    public static byte[] unwrapDek(
            String encryptedDek,
            String salt,
            String iv,
            String password
    ) throws GeneralSecurityException {

        Base64.Decoder decoder = Base64.getDecoder();

        byte[] saltBytes = decoder.decode(salt);
        byte[] ivBytes = decoder.decode(iv);
        byte[] encryptedBytes = decoder.decode(encryptedDek);

        byte[] key = deriveKey(password, saltBytes);

        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            cipher.init(
                    Cipher.DECRYPT_MODE,
                    new SecretKeySpec(key, "AES"),
                    new GCMParameterSpec(GCM_TAG_LENGTH, ivBytes)
            );

            return cipher.doFinal(encryptedBytes);

        } finally {
            Arrays.fill(key, (byte) 0);
        }
    }


    public static byte[] encryptFile(byte[] fileBytes, byte[] dek)
            throws GeneralSecurityException {

        byte[] iv = new byte[12];
        secureRandom.nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

        cipher.init(
                Cipher.ENCRYPT_MODE,
                new SecretKeySpec(dek, "AES"),
                new GCMParameterSpec(128, iv)
        );

        byte[] encryptedData = cipher.doFinal(fileBytes);

        // Store IV at the beginning of encrypted file
        byte[] result = new byte[iv.length + encryptedData.length];

        System.arraycopy(iv, 0, result, 0, iv.length);
        System.arraycopy(
                encryptedData, 0,
                result, iv.length,
                encryptedData.length
        );

        return result;
    }

    public static byte[] decryptFile(byte[] encryptedFile, byte[] dek)
            throws GeneralSecurityException {

        if (encryptedFile == null || encryptedFile.length < 28) {
            throw new GeneralSecurityException("Invalid encrypted file");
        }

        byte[] iv = Arrays.copyOfRange(encryptedFile, 0, 12);

        byte[] encryptedData = Arrays.copyOfRange(
                encryptedFile, 12, encryptedFile.length
        );

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

        cipher.init(
                Cipher.DECRYPT_MODE,
                new SecretKeySpec(dek, "AES"),
                new GCMParameterSpec(128, iv)
        );

        return cipher.doFinal(encryptedData);
    }

}
