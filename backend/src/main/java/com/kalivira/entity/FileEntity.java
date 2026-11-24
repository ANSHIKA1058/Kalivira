package com.kalivira.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "files")
public class FileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String originalName;
    private String encryptedName;
    private Long fileSize;
    private LocalDateTime uploadTime;
    // File owner
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    // Encryption metadata for secure password recovery

    private Integer encryptionVersion;
    private String passwordKeySalt;
    private String passwordKeyIv;
    private String passwordWrappedDek;
    private String recoveryKeyIv;
    private String recoveryWrappedDek;

    public FileEntity() {
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getOriginalName() {
        return originalName;
    }
    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }
    public String getEncryptedName() {
        return encryptedName;
    }
    public void setEncryptedName(String encryptedName) {
        this.encryptedName = encryptedName;
    }
    public Long getFileSize() {
        return fileSize;
    }
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
    public LocalDateTime getUploadTime() {
        return uploadTime;
    }
    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }
    public UserEntity getUser() {
        return user;
    }
    public void setUser(UserEntity user) {
        this.user = user;
    }

    public Integer getEncryptionVersion() {
        return encryptionVersion;
    }

    public void setEncryptionVersion(Integer encryptionVersion) {
        this.encryptionVersion = encryptionVersion;
    }

    public String getPasswordKeySalt() {
        return passwordKeySalt;
    }

    public void setPasswordKeySalt(String passwordKeySalt) {
        this.passwordKeySalt = passwordKeySalt;
    }

    public String getPasswordKeyIv() {
        return passwordKeyIv;
    }

    public void setPasswordKeyIv(String passwordKeyIv) {
        this.passwordKeyIv = passwordKeyIv;
    }

    public String getPasswordWrappedDek() {
        return passwordWrappedDek;
    }

    public void setPasswordWrappedDek(String passwordWrappedDek) {
        this.passwordWrappedDek = passwordWrappedDek;
    }

    public String getRecoveryKeyIv() {
        return recoveryKeyIv;
    }

    public void setRecoveryKeyIv(String recoveryKeyIv) {
        this.recoveryKeyIv = recoveryKeyIv;
    }

    public String getRecoveryWrappedDek() {
        return recoveryWrappedDek;
    }

    public void setRecoveryWrappedDek(String recoveryWrappedDek) {
        this.recoveryWrappedDek = recoveryWrappedDek;
    }

}