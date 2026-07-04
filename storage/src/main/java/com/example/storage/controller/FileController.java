package com.example.storage.controller;

import com.example.storage.entity.FileMetadata;
import com.example.storage.service.FileService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @GetMapping
    public ResponseEntity<List<FileMetadata>> getAll(){
        List<FileMetadata> files = fileService.getAllFiles();
        return ResponseEntity.ok(files);
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file" ) MultipartFile file){
            try{
                FileMetadata metadata = fileService.saveFile(file);
                return  ResponseEntity.ok(metadata);
            }catch (IllegalArgumentException e) {
                return ResponseEntity.status(400).body(e.getMessage());
            }catch (IOException e){
                return ResponseEntity.status(500).body("Dosya yüklenirken hata oluştu: " + e.getMessage());
            }
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id){
        try {
            Resource resource = fileService.loadFileAsResource(id);

            // Dosyanın içerik tipini (MIME) tespit etme
            Path path = Paths.get(resource.getFile().getAbsolutePath());
            String contentType = Files.probeContentType(path);
            if (contentType == null) {
                contentType = "application/octet-stream"; // Bilinmeyen türler için genel binary formatı
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    // attachment; filename="..." ifadesi tarayıcıya indirme penceresi açtırır
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename().substring(37) + "\"")
                    //.substring(37) UUID yapısının (36 karakter + alt tire) adını temizleyip orijinal adı indirmeyi sağlar.
                    .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.status(500).build();
        } catch (IOException e) {
            return ResponseEntity.status(404).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).build();
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteFile(@PathVariable Long id) {
        try {
            fileService.deleteFile(id);
            return ResponseEntity.ok("Dosya başarıyla silindi. ID: " + id);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Dosya diskten silinirken hata oluştu.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }


}
