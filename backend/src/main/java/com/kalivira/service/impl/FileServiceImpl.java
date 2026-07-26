package com.kalivira.service.impl;

import com.kalivira.service.FileService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileServiceImpl implements FileService {

    @Override
    public String uploadFile(MultipartFile file, String password) {

        return "File Received Successfully";
    }
}