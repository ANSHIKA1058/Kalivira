package com.kalivira.controller;
import com.kalivira.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.kalivira.dto.FileResponseDTO;
import com.kalivira.entity.FileEntity;
import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileController {
    @Autowired
    private FileService fileService;

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public String uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("password") String password
    ){
        return fileService.uploadFile(file,password);
    }
    @GetMapping("/download")
    public ResponseEntity<?> downloadFile(
            @RequestParam("filename") String filename,
            @RequestParam("password") String password) {

        try {

            byte[] data = fileService.downloadFile(filename, password);

            ByteArrayResource resource = new ByteArrayResource(data);

            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" +
                                    filename.replace(".enc", "") +
                                    "\""
                    )
                    .contentLength(data.length)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body("Invalid password");

        } catch (Exception e) {

            return ResponseEntity
                    .internalServerError()
                    .body("Something went wrong while downloading the file");
        }
    }

    @GetMapping("/my-files")
    public ResponseEntity<?> getMyFiles() {
        return ResponseEntity.ok(fileService.getMyFiles());
    }
}
