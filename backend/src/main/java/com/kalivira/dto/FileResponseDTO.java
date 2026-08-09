package com.kalivira.dto;
import java.time.LocalDateTime;

public class FileResponseDTO {
    private Long id;
    private String originalName;
    private String encryptedName;
    private Long fileSize;
    private LocalDateTime uploadTime;
    public FileResponseDTO() {

    }
    public FileResponseDTO(
            Long id,
            String originalName,
            String encryptedName,
            Long fileSize,
            LocalDateTime uploadTime
    ){
        this.id=id;
        this.originalName=originalName;
        this.encryptedName=encryptedName;
        this.fileSize=fileSize;
        this.uploadTime=uploadTime;
    }

    public Long getId() {
        return id;
    }
    public String getOriginalName() {
        return originalName;
    }
    public String getEncryptedName(){
        return encryptedName;
    }
    public Long getFileSize(){
        return fileSize;
    }
    public LocalDateTime getUploadTime(){
        return uploadTime;
    }
}
