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
import com.kalivira.exception.FileAccessDeniedException;
import com.kalivira.entity.UserEntity;
import com.kalivira.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.List;
import com.kalivira.dto.FileResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import com.kalivira.service.S3Service;

@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;


    @Autowired
    private S3Service s3Service;

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

//s3 me upload
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(file.getOriginalFilename() + ".enc")
                    .build();

            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromBytes(encryptedBytes)
            );

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

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        // Get logged-in user
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        // Find file belonging ONLY to logged-in user
        FileEntity fileEntity = fileRepository
                .findByEncryptedNameAndUser(filename, user)
                .orElseThrow(() ->
                        new FileAccessDeniedException(
                                "File not found or access denied"
                        )
                );

        try {

            byte[] encryptedBytes = s3Service.downloadFile(
                    fileEntity.getEncryptedName()
            );

            System.out.println(
                    "Read Encrypted Size = " + encryptedBytes.length
            );
            // Decrypt
            return AESUtil.decrypt(encryptedBytes, password);
        } catch (InvalidPasswordException e) {
            // Wrong password
            throw e;
        } catch (Exception e) {
            // Any other storage/system error
            throw new RuntimeException(
                    "Something went wrong while downloading the file"
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

    @Override
    public void deleteFile(String filename) {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        FileEntity fileEntity = fileRepository
                .findByEncryptedNameAndUser(filename, user)
                .orElseThrow(() ->
                        new RuntimeException("File not found or access denied")
                );

        try {

            s3Service.deleteFile(
                    fileEntity.getEncryptedName()
            );

            fileRepository.delete(fileEntity);

        } catch (Exception e) {

            throw new RuntimeException("Failed to delete file");
        }
    }
}