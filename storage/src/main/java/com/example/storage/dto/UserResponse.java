package com.example.storage.dto;

import com.example.storage.entity.FileMetadata;
import com.example.storage.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String userName;

    private Role role;
    private List<FileMetaDataUserResponse> files;
}
