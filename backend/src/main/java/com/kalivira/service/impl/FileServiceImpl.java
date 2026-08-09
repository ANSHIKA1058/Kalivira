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
import com.kalivira.exception.InvalidPasswordException;
import com.kalivira.entity.UserEntity;
import com.kalivira.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.List;
import com.kalivira.dto.FileResponseDTO;

@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public String uploadFile(MultipartFile file, String password) {
        try{
            //converting file into bytes
            byte[] fileBytes = file.getBytes();

            System.out.println("Original Size = " + fileBytes.length);

            byte[] encryptedBytes = AESUtil.encrypt(fileBytes, password);

            System.out.println("Encrypted Size = " + encryptedBytes.length);
            //path of storage folder
            Path path = Paths.get("storage", file.getOriginalFilename()+".enc");
            System.out.println("Upload Path = " + path.toAbsolutePath());
            //save encrypted file
            Files.write(path,encryptedBytes);

            FileEntity fileEntity=new FileEntity();
            String email = SecurityContextHolder.getContext()
                    .getAuthentication()
                    .getName();

            UserEntity user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            fileEntity.setUser(user);
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
    public byte[] downloadFile(String filename, String password) {
        try {

            // Get logged-in user's email from JWT
            String email = SecurityContextHolder.getContext()
                    .getAuthentication()
                    .getName();

            // Find logged-in user
            UserEntity user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Find file ONLY if it belongs to this user
            FileEntity fileEntity = fileRepository
                    .findByEncryptedNameAndUser(filename, user)
                    .orElseThrow(() -> new RuntimeException("File not found or access denied"));

            // Now read the physical encrypted file
            Path path = Paths.get("storage", fileEntity.getEncryptedName());

            System.out.println("Download Path = " + path.toAbsolutePath());

            byte[] encryptedBytes = Files.readAllBytes(path);

            System.out.println("Read Encrypted Size = " + encryptedBytes.length);

            // Decrypt
            return AESUtil.decrypt(encryptedBytes, password);

        } catch (Exception e) {

            throw new InvalidPasswordException(
                    "Invalid password or file access denied"
            );
        }
    }


    @Override
    public List<FileResponseDTO> getMyFiles() {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

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
}