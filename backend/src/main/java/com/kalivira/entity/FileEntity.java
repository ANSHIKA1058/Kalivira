package com.kalivira.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity
@Table(name= "files")

public class FileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String originalName;
    private String encryptedName;
    private Long fileSize;
    private LocalDateTime uploadTime;
    public FileEntity(){
    }
    public Long getId(){
        return id;
    }
    public void setId(Long id){
        this.id=id;
    }
    public String getOriginalName(){
        return originalName;
    }
    public void setOriginalName(String originalName){
        this.originalName=originalName;
    }
    public String getEncryptedName(){
        return encryptedName;
    }
    public void setEncryptedName(String encryptedName){
        this.encryptedName=encryptedName;
    }
    public Long getFileSize(){
        return fileSize;
    }
    public void setFileSize(Long fileSize){
        this.fileSize=fileSize;
    }
    public LocalDateTime getUploadTime(){
        return uploadTime;
    }
    public void setUploadTime(LocalDateTime uploadTime){
        this.uploadTime=uploadTime;
    }
}
