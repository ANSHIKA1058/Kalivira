
package com.kalivira.service.impl;

import com.kalivira.dto.FileResponseDTO;
import com.kalivira.entity.FileEntity;
import com.kalivira.entity.UserEntity;
import com.kalivira.exception.FileAccessDeniedException;
import com.kalivira.exception.InvalidPasswordException;
import com.kalivira.repository.FileRepository;
import com.kalivira.repository.UserRepository;
import com.kalivira.service.EmailService;
import com.kalivira.service.FileService;
import com.kalivira.service.KmsDataKeyService;
import com.kalivira.service.S3Service;
import com.kalivira.util.AESUtil;
import com.kalivira.util.FileCryptoUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    private static final int KMS_ENCRYPTION_VERSION = 2;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private S3Client s3Client;

    @Autowired
    private S3Service s3Service;

    @Autowired
    private KmsDataKeyService kmsDataKeyService;

    @Autowired
    private EmailService emailService;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    private final SecureRandom secureRandom = new SecureRandom();

    private UserEntity getLoggedInUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private boolean isStrongPassword(String password) {
        return password != null
                && password.length() >= 8
                && password.matches(".*[A-Z].*")
                && password.matches(".*[a-z].*")
                && password.matches(".*[0-9].*")
                && password.matches(".*[^A-Za-z0-9].*");
    }

    // --------------------------------------------------
    // UPLOAD
    // --------------------------------------------------

    @Override
    public String uploadFile(MultipartFile file, String password) {

        if (!isStrongPassword(password)) {
            return "Weak password. Password must contain at least 8 characters, uppercase, lowercase, number, and special character.";
        }

        byte[] dek = null;
        String encryptedFileName = null;

        try {
            UserEntity user = getLoggedInUser();

            byte[] fileBytes = file.getBytes();

            encryptedFileName = UUID.randomUUID() + ".enc";

            // Generate a new AES-256 key through AWS KMS
            KmsDataKeyService.GeneratedDataKey generated =
                    kmsDataKeyService.generateDataKey(
                            encryptedFileName,
                            user.getId()
                    );

            dek = generated.plaintextKey();

            // Encrypt the actual file using the generated DEK
            byte[] encryptedBytes =
                    FileCryptoUtil.encryptFile(fileBytes, dek);

            // Wrap the DEK using the user's encryption password
            FileCryptoUtil.WrappedDek wrapped =
                    FileCryptoUtil.wrapDek(dek, password);

            // Save encrypted file locally
            Path storagePath = Paths.get("storage");
            Files.createDirectories(storagePath);

            Files.write(
                    storagePath.resolve(encryptedFileName),
                    encryptedBytes
            );

            // Upload encrypted file to S3
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(encryptedFileName)
                    .build();

            s3Client.putObject(
                    request,
                    RequestBody.fromBytes(encryptedBytes)
            );

            // Save metadata in database
            FileEntity entity = new FileEntity();

            entity.setUser(user);
            entity.setOriginalName(file.getOriginalFilename());
            entity.setEncryptedName(encryptedFileName);
            entity.setFileSize(file.getSize());
            entity.setUploadTime(LocalDateTime.now());

            entity.setEncryptionVersion(KMS_ENCRYPTION_VERSION);

            entity.setPasswordKeySalt(wrapped.salt());
            entity.setPasswordKeyIv(wrapped.iv());
            entity.setPasswordWrappedDek(wrapped.encryptedDek());

            // KMS-encrypted DEK is the recovery copy
            entity.setRecoveryWrappedDek(
                    Base64.getEncoder().encodeToString(
                            generated.encryptedKey()
                    )
            );

            entity.setRecoveryKeyIv(null);

            fileRepository.save(entity);

            return "File Encrypted Successfully";

        } catch (Exception e) {
            e.printStackTrace();
            return "Encryption Failed";
        } finally {
            if (dek != null) {
                Arrays.fill(dek, (byte) 0);
            }
        }
    }

    // --------------------------------------------------
    // DOWNLOAD
    // --------------------------------------------------

    @Override
    public byte[] downloadFile(Long fileId, String password) {

        UserEntity user = getLoggedInUser();

        FileEntity entity = fileRepository
                .findByIdAndUser(fileId, user)
                .orElseThrow(() ->
                        new FileAccessDeniedException(
                                "File not found or access denied"
                        )
                );

        byte[] dek = null;

        try {
            byte[] encryptedBytes =
                    s3Service.downloadFile(entity.getEncryptedName());

            // Legacy files: old AESUtil encryption
            if (entity.getEncryptionVersion() == null
                    || entity.getEncryptionVersion() == 1) {

                return AESUtil.decrypt(encryptedBytes, password);
            }

            // New files: password unlocks the DEK
            dek = FileCryptoUtil.unwrapDek(
                    entity.getPasswordWrappedDek(),
                    entity.getPasswordKeySalt(),
                    entity.getPasswordKeyIv(),
                    password
            );

            return FileCryptoUtil.decryptFile(encryptedBytes, dek);

        } catch (InvalidPasswordException e) {
            throw e;

        } catch (GeneralSecurityException e) {
            throw new InvalidPasswordException("Invalid password");

        } catch (Exception e) {
            throw new RuntimeException(
                    "Something went wrong while downloading the file",
                    e
            );
        } finally {
            if (dek != null) {
                Arrays.fill(dek, (byte) 0);
            }
        }
    }

    // --------------------------------------------------
    // GET MY FILES
    // --------------------------------------------------

    @Override
    public List<FileResponseDTO> getMyFiles() {

        UserEntity user = getLoggedInUser();

        List<FileEntity> files = fileRepository.findAllByUser(user);

        return files.stream()
                .map(file -> new FileResponseDTO(
                        file.getId(),
                        file.getOriginalName(),
                        file.getEncryptedName(),
                        file.getFileSize(),
                        file.getUploadTime()
                ))
                .toList();
    }

    // --------------------------------------------------
    // DELETE
    // --------------------------------------------------

    @Override
    public void deleteFile(Long fileId, String password) {

        UserEntity user = getLoggedInUser();

        FileEntity entity = fileRepository
                .findByIdAndUser(fileId, user)
                .orElseThrow(() ->
                        new FileAccessDeniedException(
                                "File not found or access denied"
                        )
                );

        byte[] dek = null;
        byte[] decryptedBytes = null;

        try {
            byte[] encryptedBytes =
                    s3Service.downloadFile(entity.getEncryptedName());

            if (entity.getEncryptionVersion() == null
                    || entity.getEncryptionVersion() == 1) {

                decryptedBytes = AESUtil.decrypt(encryptedBytes, password);

            } else {

                dek = FileCryptoUtil.unwrapDek(
                        entity.getPasswordWrappedDek(),
                        entity.getPasswordKeySalt(),
                        entity.getPasswordKeyIv(),
                        password
                );

                decryptedBytes =
                        FileCryptoUtil.decryptFile(encryptedBytes, dek);
            }

            // Delete only after successful password verification
            s3Service.deleteFile(entity.getEncryptedName());

            fileRepository.delete(entity);

        } catch (InvalidPasswordException e) {
            throw e;

        } catch (GeneralSecurityException e) {
            throw new InvalidPasswordException("Invalid password");

        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file", e);

        } finally {
            if (dek != null) {
                Arrays.fill(dek, (byte) 0);
            }

            if (decryptedBytes != null) {
                Arrays.fill(decryptedBytes, (byte) 0);
            }
        }
    }

    // --------------------------------------------------
    // REQUEST FILE RECOVERY OTP
    // --------------------------------------------------

    @Override
    public String requestFileRecoveryOtp() {

        UserEntity user = getLoggedInUser();

        String otp = String.valueOf(
                secureRandom.nextInt(100000, 1000000)
        );

        try {
            user.setFileRecoveryOtpHash(hashOtp(otp));
            user.setFileRecoveryOtpExpiry(
                    LocalDateTime.now().plusMinutes(5)
            );

            userRepository.save(user);

            emailService.sendFileRecoveryOtpEmail(
                    user.getEmail(),
                    otp
            );

            return "File recovery OTP sent to your registered email.";

        } catch (Exception e) {

            user.setFileRecoveryOtpHash(null);
            user.setFileRecoveryOtpExpiry(null);
            userRepository.save(user);

            throw new RuntimeException(
                    "Unable to send recovery OTP",
                    e
            );
        }
    }

    // --------------------------------------------------
    // RESET FILE ENCRYPTION PASSWORD
    // --------------------------------------------------

    @Override
    @Transactional
    public String resetFileEncryptionPassword(
            String otp,
            String newPassword
    ) {

        UserEntity user = getLoggedInUser();

        if (otp == null || !otp.matches("\\d{6}")) {
            throw new IllegalArgumentException("Invalid OTP");
        }

        if (!isStrongPassword(newPassword)) {
            throw new IllegalArgumentException(
                    "New password must contain at least 8 characters, uppercase, lowercase, number, and special character."
            );
        }

        String storedHash = user.getFileRecoveryOtpHash();

        if (storedHash == null
                || user.getFileRecoveryOtpExpiry() == null
                || LocalDateTime.now().isAfter(
                user.getFileRecoveryOtpExpiry()
        )
                || !MessageDigest.isEqual(
                storedHash.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                hashOtp(otp).getBytes(java.nio.charset.StandardCharsets.UTF_8)
        )) {

            throw new IllegalArgumentException(
                    "Invalid or expired recovery OTP"
            );
        }

        List<FileEntity> files = fileRepository.findAllByUser(user);

        int recoveredFiles = 0;

        for (FileEntity entity : files) {

            // Legacy AESUtil files cannot be recovered through KMS
            if (entity.getEncryptionVersion() == null
                    || entity.getEncryptionVersion() == 1) {
                continue;
            }

            byte[] dek = null;

            try {
                // Recover the DEK using AWS KMS
                byte[] encryptedDek = Base64.getDecoder().decode(
                        entity.getRecoveryWrappedDek()
                );

                dek = kmsDataKeyService.decryptDataKey(
                        encryptedDek,
                        entity.getEncryptedName(),
                        user.getId()
                );

                // Wrap the same DEK with the new password
                FileCryptoUtil.WrappedDek wrapped =
                        FileCryptoUtil.wrapDek(dek, newPassword);

                entity.setPasswordKeySalt(wrapped.salt());
                entity.setPasswordKeyIv(wrapped.iv());
                entity.setPasswordWrappedDek(wrapped.encryptedDek());

                fileRepository.save(entity);

                recoveredFiles++;

            } catch (Exception e) {
                throw new RuntimeException(
                        "Failed to recover file: " + entity.getId(),
                        e
                );

            } finally {
                if (dek != null) {
                    Arrays.fill(dek, (byte) 0);
                }
            }
        }

        if (recoveredFiles == 0) {
            throw new IllegalStateException(
                    "No KMS-recoverable files found. Older files require their original encryption password."
            );
        }

        // OTP can be used only once
        user.setFileRecoveryOtpHash(null);
        user.setFileRecoveryOtpExpiry(null);

        userRepository.save(user);

        return "Encryption password updated successfully for "
                + recoveredFiles + " recoverable file(s).";
    }

    // --------------------------------------------------
    // OTP HASHING
    // --------------------------------------------------

    private String hashOtp(String otp) {

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    otp.getBytes(java.nio.charset.StandardCharsets.UTF_8)
            );

            return Base64.getEncoder().encodeToString(hash);

        } catch (Exception e) {
            throw new RuntimeException("Unable to hash OTP", e);
        }
    }
}
