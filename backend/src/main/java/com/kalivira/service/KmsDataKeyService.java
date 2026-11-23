
package com.kalivira.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.DataKeySpec;
import software.amazon.awssdk.services.kms.model.GenerateDataKeyRequest;
import software.amazon.awssdk.services.kms.model.GenerateDataKeyResponse;
import software.amazon.awssdk.services.kms.model.DecryptRequest;

import java.util.Map;

@Service
public class KmsDataKeyService {

    private final KmsClient kmsClient;

    @Value("${aws.kms.key-arn}")
    private String keyArn;

    public KmsDataKeyService(KmsClient kmsClient) {
        this.kmsClient = kmsClient;
    }

    public record GeneratedDataKey(
            byte[] plaintextKey,
            byte[] encryptedKey
    ) {}

    private Map<String, String> createEncryptionContext(
            String fileContext,
            Long userId
    ) {
        return Map.of(
                "file-context", fileContext,
                "owner-id", userId.toString()
        );
    }

    // Generate a new AES-256 data key
    public GeneratedDataKey generateDataKey(
            String fileContext,
            Long userId
    ) {

        GenerateDataKeyRequest request =
                GenerateDataKeyRequest.builder()
                        .keyId(keyArn)
                        .keySpec(DataKeySpec.AES_256)
                        .encryptionContext(
                                createEncryptionContext(fileContext, userId)
                        )
                        .build();

        GenerateDataKeyResponse response =
                kmsClient.generateDataKey(request);

        return new GeneratedDataKey(
                response.plaintext().asByteArray(),
                response.ciphertextBlob().asByteArray()
        );
    }

    // Recover the AES-256 data key using AWS KMS
    public byte[] decryptDataKey(
            byte[] encryptedKey,
            String fileContext,
            Long userId
    ) {

        DecryptRequest request =
                DecryptRequest.builder()
                        .keyId(keyArn)
                        .ciphertextBlob(
                                SdkBytes.fromByteArray(encryptedKey)
                        )
                        .encryptionContext(
                                createEncryptionContext(fileContext, userId)
                        )
                        .build();

        return kmsClient.decrypt(request)
                .plaintext()
                .asByteArray();
    }
}
