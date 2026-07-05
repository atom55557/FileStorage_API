package com.example.storage.mapper;

import com.example.storage.dto.FileMetaDataUserResponse;
import com.example.storage.dto.UserResponse;
import com.example.storage.entity.User;

import java.util.List;
import java.util.stream.Collectors;

public class UserMapper {

    public static UserResponse toResponse(User user){
        UserResponse response = new UserResponse();

        response.setId(user.getId());
        response.setRole(user.getRole());
        response.setUserName(user.getUsername());
        List<FileMetaDataUserResponse> files = user.getFiles().stream()
                .map(file -> new FileMetaDataUserResponse(file.getFileName(), file.getFileType(), file.getSize()))
                .collect(Collectors.toList());

        response.setFiles(files);

        return response;
    }
}
