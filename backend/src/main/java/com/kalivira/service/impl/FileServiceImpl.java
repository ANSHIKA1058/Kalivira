package com.kalivira.service.impl;
import com.kalivira.service.FileService;
import com.kalivira.util.AESUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileServiceImpl implements FileService {

    @Override
    public String uploadFile(MultipartFile file, String password) {
        try{
            //converting file into bytes
            byte[] fileBytes = file.getBytes();
            //encryption with aes
            byte[] encryptedBytes= AESUtil.encrypt(fileBytes, password);
            //path of storage folder
            Path path = Paths.get("storage", file.getOriginalFilename()+".enc");
            //save encrypted file
            Files.write(path,encryptedBytes);

            return "File Encrypted Successfully";
        } catch (Exception e){
            e.printStackTrace();
            return "Encryption Failed";
        }

    }
}