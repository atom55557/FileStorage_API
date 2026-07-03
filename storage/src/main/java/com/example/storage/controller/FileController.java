package com.example.storage.controller;

import com.example.storage.entity.FileMetadata;
import com.example.storage.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file" ) MultipartFile file){
            try{
                FileMetadata metadata = fileService.saveFile(file);
                return  ResponseEntity.ok(metadata);
            }catch (IOException e){
                return ResponseEntity.status(500).body("Dosya yüklenirken hata oluştu: " + e.getMessage());
            }
    }
}
