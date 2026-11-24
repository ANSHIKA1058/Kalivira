
package com.kalivira.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.util.Arrays;

@Component
public class KmsConnectionTestRunner implements CommandLineRunner {

    private static final Logger log =
            LoggerFactory.getLogger(KmsConnectionTestRunner.class);

    private final KmsDataKeyService kmsDataKeyService;

    public KmsConnectionTestRunner(
            KmsDataKeyService kmsDataKeyService) {
        this.kmsDataKeyService = kmsDataKeyService;
    }

    @Override
    public void run(String... args) {

        byte[] plaintextKey = null;
        byte[] decryptedKey = null;

        try {
            // Generate AES-256 key using AWS KMS
            KmsDataKeyService.GeneratedDataKey generated =
                    kmsDataKeyService.generateDataKey(
                            "kalivira-kms-test",
                            1L
                    );

            plaintextKey = generated.plaintextKey();

            // Decrypt the encrypted key using AWS KMS
            decryptedKey = kmsDataKeyService.decryptDataKey(
                    generated.encryptedKey(),
                    "kalivira-kms-test",
                    1L
            );

            // Compare both keys without printing them
            boolean matched = MessageDigest.isEqual(
                    plaintextKey,
                    decryptedKey
            );

            if (matched) {
                log.info("SUCCESS: AWS KMS GenerateDataKey and Decrypt working!");
            } else {
                log.error("FAILED: Generated and decrypted keys do not match.");
            }

        } catch (Exception e) {
            log.error("AWS KMS TEST FAILED: {}", e.getMessage(), e);

        } finally {
            if (plaintextKey != null) {
                Arrays.fill(plaintextKey, (byte) 0);
            }

            if (decryptedKey != null) {
                Arrays.fill(decryptedKey, (byte) 0);
            }
        }
    }
}
