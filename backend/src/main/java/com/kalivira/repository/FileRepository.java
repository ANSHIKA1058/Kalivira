package com.kalivira.repository;

import com.kalivira.entity.FileEntity;
import com.kalivira.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<FileEntity, Long> {

    Optional<FileEntity> findByEncryptedNameAndUser(
            String encryptedName,
            UserEntity user
    );
    List<FileEntity> findAllByUser(UserEntity user);
}