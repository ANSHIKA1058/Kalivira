package com.kalivira.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface S3Service {

    String uploadFile(MultipartFile file) throws IOException;

    byte[] downloadFile(String fileName);

    void deleteFile(String fileName);
}