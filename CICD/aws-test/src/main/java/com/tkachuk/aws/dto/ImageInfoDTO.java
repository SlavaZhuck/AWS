package com.tkachuk.aws.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.ToString;

@Data
public class ImageInfoDTO {

    private Long id;
    private String name;
    private LocalDateTime updatedAt;
    private String fileExtension;
    private long size;



}
