package com.awsbasics.simpleapp.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ImageMetadataDto {
    private Long id;
    private String name;
    private Long size;
    private String fileExtension;
    private LocalDateTime lastUpdate;
}
