package com.kalivira.service.impl;
import com.kalivira.service.FileService;
import com.kalivira.util.AESUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.kalivira.entity.FileEntity;
import com.kalivira.repository.FileRepository;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private FileRepository fileRepository;


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

            FileEntity fileEntity=new FileEntity();
            fileEntity.setOriginalName(file.getOriginalFilename());
            fileEntity.setEncryptedName(file.getOriginalFilename()+".enc");
            fileEntity.setFileSize(file.getSize());
            fileEntity.setUploadTime(LocalDateTime.now());
            fileRepository.save(fileEntity);

            return "File Encrypted Successfully";

        }
        catch (Exception e){
            e.printStackTrace();
            return "Encryption Failed";
        }

    }

    @Override
    public byte[] downloadFile(String filename, String password){
        try{
            //encrypted file read
            Path path = Paths.get("storage",filename);
            byte[] encryptedBytes = Files.readAllBytes(path);
            //decrypt
            byte[] decryptedBytes = AESUtil.decrypt(encryptedBytes, password);
            return decryptedBytes;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }


}