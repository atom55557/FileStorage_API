package com.example.storage.service;

import com.example.storage.entity.FileMetadata;
import com.example.storage.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private final FileRepository fileRepository;

    //Sadece izin verilen MIME türlerinin listesi (Güvenli Liste)
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "image/jpeg",
            "image/png",
            "application/pdf"
    );

    public List<FileMetadata> getAllFiles(){
        return fileRepository.findAll();
    }

    public FileMetadata saveFile(MultipartFile file) throws IOException {

        // Dosya türü kontrolü
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Geçersiz dosya türü! Sadece JPEG, PNG ve PDF dosyaları yüklenebilir.");
        }

        // Ekstra Güvenlik (Uzantı kontrolü)
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String lowerCaseName = originalFilename.toLowerCase();
            if (lowerCaseName.endsWith(".exe") || lowerCaseName.endsWith(".bat") || lowerCaseName.endsWith(".sh")) {
                throw new IllegalArgumentException("Güvenlik nedeniyle çalıştırılabilir (.exe, .bat, .sh) dosyalar yüklenemez!");
            }
        }

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
    //Dosya İndirme Metodu
    public Resource loadFileAsResource(long id) throws MalformedURLException {
        // 1. Veritabanından dosya bilgilerini getir, bulamazsa hata fırlat
        FileMetadata metadata = fileRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Dosya veritabanında bulunamadı. ID:"+id));

        // 2. Dosyanın diskteki yolunu al ve Resource nesnesine çevir
        Path filePath = Paths.get(metadata.getFilePath());
        Resource resource = new UrlResource(filePath.toUri());

        // 3. Dosya gerçekten diskte var mı kontrol et
        if (resource.exists() && resource.isReadable()) {
            return resource;
        } else {
            throw new RuntimeException("Dosya diskte fiziksel olarak bulunamadı veya okunamıyor.");
        }

    }

    // Dosya Silme Metodu
    public void deleteFile(Long id) throws IOException {
        // 1. Veritabanından bilgileri getir
        FileMetadata metadata = fileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Silinecek dosya bulunamadı. ID: " + id));

        // 2. Önce Windows diskindeki fiziksel dosyayı sil
        Path filePath = Paths.get(metadata.getFilePath());
        Files.deleteIfExists(filePath);

        // 3. Sonra veritabanındaki kaydı sil
        fileRepository.delete(metadata);
    }

}
