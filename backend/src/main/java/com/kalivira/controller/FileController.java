package com.kalivira.controller;
import com.kalivira.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Resource> downloadFile(
            @RequestParam String filename,
            @RequestParam String password){
        byte[] data = fileService.downloadFile(filename, password);
        ByteArrayResource resource = new ByteArrayResource(data);
        return ResponseEntity.ok().
                header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\""+filename.replace(".enc", "")+"\"")
                .contentLength(data.length)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }


}
