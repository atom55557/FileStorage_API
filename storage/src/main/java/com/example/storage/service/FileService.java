package com.example.storage.service;

import com.example.storage.entity.FileMetadata;
import com.example.storage.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private final FileRepository fileRepository;
    public FileMetadata saveFile(MultipartFile file) throws IOException {
        // Yükleme klasörü yoksa oluştur
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Benzersiz dosya adı oluşturma (Aynı isimde dosya yüklenirse çakışmasın diye)
        String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir, uniqueFileName);

        // Dosyayı diske kaydet
        Files.write(filePath, file.getBytes());

        // Bilgileri veritabanına kaydet
        FileMetadata metadata = new FileMetadata();
        metadata.setFileName(file.getOriginalFilename());
        metadata.setFileType(file.getContentType());
        metadata.setSize(file.getSize());
        metadata.setFilePath(filePath.toString());

        return fileRepository.save(metadata);
    }

}
