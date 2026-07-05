package com.example.storage.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FileMetaDataResponse {

    private Long id;
    private String fileName;
    private String fileType;
    private long size;
    private String filePath;
    private String userName;
}
