package com.example.storage.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FileMetaDataUserResponse {
    private String fileName;
    private String fileType;
    private long size;
}
