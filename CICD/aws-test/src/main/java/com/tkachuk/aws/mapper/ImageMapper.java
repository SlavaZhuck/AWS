package com.tkachuk.aws.mapper;

import com.tkachuk.aws.dto.ImageInfoDTO;
import com.tkachuk.aws.entity.Image;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ImageMapper {

    ImageInfoDTO toImageInfoDTO(Image image);

}
