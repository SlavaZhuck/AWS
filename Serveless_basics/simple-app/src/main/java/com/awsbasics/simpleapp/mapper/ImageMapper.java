package com.awsbasics.simpleapp.mapper;

import com.awsbasics.simpleapp.dto.ImageMetadataDto;
import com.awsbasics.simpleapp.dto.ImageUploadDto;
import com.awsbasics.simpleapp.entity.Image;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ImageMapper {

    ImageMetadataDto toMetadataDto(Image entityModel);

    Image toEntity(ImageUploadDto clientModel);
}
