package com.awsbasics.simpleapp.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class ImageUploadDto {
    @NotEmpty
    private String name;
    @NotNull
    private MultipartFile file;
}
