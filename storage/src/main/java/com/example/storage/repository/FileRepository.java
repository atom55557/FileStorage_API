package com.example.storage.repository;

import com.example.storage.entity.FileMetadata;
import com.example.storage.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<FileMetadata, Long> {
    List<FileMetadata> findByUploadedBy(User user);

    @Query("""
            SELECT f FROM FileMetadata f
            JOIN FETCH f.uploadedBy
            """)
    List<FileMetadata> findAllWithDetails();

    @Query("""
            SELECT f FROM FileMetadata f
            JOIN FETCH f.uploadedBy
            WHERE f.uploadedBy = :user
            """)
    List<FileMetadata> findByUploadedByWithDetails(@Param("user") User currentUser);
}
