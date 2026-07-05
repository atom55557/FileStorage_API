package com.example.storage.mapper;

import com.example.storage.dto.FileMetaDataResponse;
import com.example.storage.entity.FileMetadata;

public class FileMetaDataMapper {

    public static FileMetaDataResponse toResponse(FileMetadata fileMetadata){

        FileMetaDataResponse response = new FileMetaDataResponse();
        response.setId(fileMetadata.getId());
        response.setSize(fileMetadata.getSize());
        response.setFileName(fileMetadata.getFileName());
        response.setFileType(fileMetadata.getFileType());
        response.setFilePath(fileMetadata.getFilePath());
        response.setUserName(fileMetadata.getUploadedBy().getUsername());

        return response;
    }
}
