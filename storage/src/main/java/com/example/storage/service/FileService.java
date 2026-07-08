package com.example.storage.service;

import com.example.storage.dto.FileMetaDataResponse;
import com.example.storage.entity.FileMetadata;
import com.example.storage.entity.User;
import com.example.storage.mapper.FileMetaDataMapper;
import com.example.storage.repository.FileRepository;
import com.example.storage.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.AccessDeniedException;
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
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUserName(userName)
                .orElseThrow(() -> new RuntimeException("Aktif kullanıcı veritabanında bulunamadı."));
    }


    private boolean isAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }


    //Sadece izin verilen MIME türlerinin listesi (Güvenli Liste)
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "image/jpeg",
            "image/png",
            "application/pdf"
    );
    @Transactional
    public List<FileMetaDataResponse> getAllFiles(){
        if (isAdmin()) {
            return fileRepository.findAllWithDetails().stream().map(FileMetaDataMapper::toResponse).toList();
        }
        User currentUser = getCurrentUser();

        return fileRepository.findByUploadedByWithDetails(currentUser).stream().map(FileMetaDataMapper::toResponse).toList();
    }

    @Transactional
    public FileMetaDataResponse saveFile(MultipartFile file) throws IOException {
        User currentUser = getCurrentUser();

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
        metadata.setUploadedBy(currentUser);
        fileRepository.save(metadata);

        return FileMetaDataMapper.toResponse(metadata);
    }

    //Dosya İndirme Metodu
    @Transactional
    public Resource loadFileAsResource(long id) throws MalformedURLException {
        // 1. Veritabanından dosya bilgilerini getir, bulamazsa hata fırlat
        FileMetadata metadata = fileRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Dosya veritabanında bulunamadı. ID:"+id));

        User currentUser = getCurrentUser();

        if(!isAdmin() && !metadata.getUploadedBy().getId().equals(currentUser.getId())){
            throw new RuntimeException("Bu işlem için yetkiniz yok.");
        }

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
    @Transactional
    public void deleteFile(Long id) throws IOException {
        // 1. Veritabanından bilgileri getir
        FileMetadata metadata = fileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Silinecek dosya bulunamadı. ID: " + id));
        User currentUser = getCurrentUser();

        if(!isAdmin() && !metadata.getUploadedBy().getId().equals(currentUser.getId())){
            throw new AccessDeniedException("Bu işlem için yetkiniz yok.");
        }

        // 2. Önce Windows diskindeki fiziksel dosyayı sil
        Path filePath = Paths.get(metadata.getFilePath());
        Files.deleteIfExists(filePath);

        // 3. Sonra veritabanındaki kaydı sil
        fileRepository.delete(metadata);
    }

}
