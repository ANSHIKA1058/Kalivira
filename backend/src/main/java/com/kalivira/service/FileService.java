package com.kalivira.service;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import com.kalivira.dto.FileResponseDTO;
public interface FileService {
    String uploadFile(MultipartFile file, String password);
    byte[] downloadFile(String filename, String password);
    List<FileResponseDTO> getMyFiles();
}
