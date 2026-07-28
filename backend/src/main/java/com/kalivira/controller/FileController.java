package com.kalivira.controller;
import com.kalivira.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
@RestController
@RequestMapping("/api/files")
public class FileController {
    @Autowired
    private FileService fileService;

    @PostMapping("/upload")
    public String uploadFile(@RequestParam MultipartFile file,@RequestParam String password){
        return fileService.uploadFile(file, password);
    }
    @GetMapping("/download")
    public byte[] downloadFile(
            @RequestParam String filename,
            @RequestParam String password) {
        return fileService.downloadFile(filename, password);
    }

}
