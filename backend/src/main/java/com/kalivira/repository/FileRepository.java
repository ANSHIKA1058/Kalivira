package com.kalivira.repository;
import com.kalivira.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
public interface FileRepository extends JpaRepository<FileEntity, Long> {
}
