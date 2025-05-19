package com.awsbasics.simpleapp.controller;

import com.awsbasics.simpleapp.dto.ImageUploadDto;
import com.awsbasics.simpleapp.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static io.netty.util.internal.StringUtil.EMPTY_STRING;
import static java.lang.Boolean.TRUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@RestController
@CrossOrigin
@RequestMapping("v1/images")
public class ImageController {

    private final ImageService imageService;

    @Autowired
    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getImageMetadata(
            @RequestParam(name = "name", required = false, defaultValue = EMPTY_STRING) String name,
            @RequestParam(name = "isRandom", required = false, defaultValue = "false") Boolean isRandom) {
        if (TRUE.equals(isRandom)) {
            return imageService.findRandomImageMetadata();
        }
        return imageService.findImageMetadataByName(name);
    }

    @GetMapping(value = "/{name}", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<?> getImage(@PathVariable String name) {
        return imageService.findImage(name);
    }

    @PostMapping(consumes = MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> save(@ModelAttribute ImageUploadDto downloadClientModel) {
        return imageService.upload(downloadClientModel);
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<?> delete(@PathVariable String name) {
        return imageService.delete(name);
    }
}
