package com.kalivira.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;

@RestController
public class S3TestController {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public S3TestController(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @GetMapping("/api/s3-test")
    public String testS3Connection() {

        s3Client.headBucket(
                HeadBucketRequest.builder()
                        .bucket(bucketName)
                        .build()
        );

        return "S3 connection successful!";
    }
}