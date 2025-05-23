package com.tkachuk.aws.controller;

import static com.tkachuk.aws.controller.AWSImageController.REST_VERSION;

import com.tkachuk.aws.dto.ImageUploadDTO;
import com.tkachuk.aws.service.AWSImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = REST_VERSION + "/images")
@RequiredArgsConstructor
public class AWSImageController {

    public static final String REST_VERSION = "v1";

    private final AWSImageService awsImageService;

    @PostMapping
    public ResponseEntity<?> uploadImage(@ModelAttribute ImageUploadDTO imageUploadDTO) {
        return awsImageService.uploadImage(imageUploadDTO);
    }

    @GetMapping("/{fileName}")
    public ResponseEntity<?> getImage(@PathVariable String fileName) {
        return awsImageService.downloadImage(fileName);
    }

    @GetMapping
    public ResponseEntity<?> getAllImageFromDB(
        @RequestParam(name = "name", required = false, defaultValue = "") String imageName,
        @RequestParam(name = "randomEnable", required = false, defaultValue = "false") Boolean randomEnable) {
        return awsImageService.getImagesInfo(imageName, randomEnable);
    }

    @GetMapping("/invoke-lambda")
    public ResponseEntity<?> invokeLambda(){
        return awsImageService.invokeImageLambda();
    }

}
