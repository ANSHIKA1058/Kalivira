package com.kalivira.service;
import org.springframework.web.multipart.MultipartFile;
public interface FileService {
    String uploadFile(MultipartFile file, String password);
    byte[] downloadFile(String filename, String password);
}
