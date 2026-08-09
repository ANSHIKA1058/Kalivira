package com.kalivira.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import com.kalivira.exception.InvalidPasswordException;
import javax.crypto.AEADBadTagException;

public class AESUtil {

    private static final int SALT_LENGTH = 16;       // 128 bits
    private static final int IV_LENGTH = 12;         // 96 bits - for GCM
    private static final int KEY_LENGTH = 256;       // AES-256
    private static final int TAG_LENGTH = 128;       // GCM authentication tag
    private static final int ITERATIONS = 600_000;

    // Generate AES-256 key from password using PBKDF2
    private static SecretKey generateKey(String password, byte[] salt)
            throws Exception {

        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                salt,
                ITERATIONS,
                KEY_LENGTH
        );

        SecretKeyFactory factory =
                SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

        byte[] keyBytes = factory.generateSecret(spec).getEncoded();

        return new SecretKeySpec(keyBytes, "AES");
    }

    // Encrypt file data
    public static byte[] encrypt(byte[] data, String password)
            throws Exception {

        SecureRandom secureRandom = new SecureRandom();

        // Generate random salt
        byte[] salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(salt);

        // Generate random IV
        byte[] iv = new byte[IV_LENGTH];
        secureRandom.nextBytes(iv);

        // Generate AES-256 key
        SecretKey secretKey = generateKey(password, salt);

        // AES-256-GCM
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

        GCMParameterSpec gcmSpec =
                new GCMParameterSpec(TAG_LENGTH, iv);

        cipher.init(
                Cipher.ENCRYPT_MODE,
                secretKey,
                gcmSpec
        );

        // Encrypt + authentication tag
        byte[] encryptedData = cipher.doFinal(data);

        // Store:
        // [salt][iv][encrypted data + authentication tag]
        ByteBuffer buffer = ByteBuffer.allocate(
                SALT_LENGTH +
                        IV_LENGTH +
                        encryptedData.length
        );

        buffer.put(salt);
        buffer.put(iv);
        buffer.put(encryptedData);

        return buffer.array();
    }

    // Decrypt file data

    public static byte[] decrypt(byte[] encryptedData, String password)
            throws Exception {

        try {

            ByteBuffer buffer = ByteBuffer.wrap(encryptedData);

            // Extract salt
            byte[] salt = new byte[SALT_LENGTH];
            buffer.get(salt);

            // Extract IV
            byte[] iv = new byte[IV_LENGTH];
            buffer.get(iv);

            // Extract encrypted content + authentication tag
            byte[] cipherText = new byte[buffer.remaining()];
            buffer.get(cipherText);

            // Generate same AES-256 key
            SecretKey secretKey = generateKey(password, salt);

            // AES-GCM
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            GCMParameterSpec gcmSpec =
                    new GCMParameterSpec(TAG_LENGTH, iv);

            cipher.init(
                    Cipher.DECRYPT_MODE,
                    secretKey,
                    gcmSpec
            );

            // Correct password -> decrypted data
            // Wrong password -> AEADBadTagException
            return cipher.doFinal(cipherText);

        } catch (AEADBadTagException e) {

            throw new InvalidPasswordException(
                    "Invalid password"
            );
        }
    }
}