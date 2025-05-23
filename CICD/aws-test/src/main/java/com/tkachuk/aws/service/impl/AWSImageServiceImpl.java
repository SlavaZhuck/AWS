package com.tkachuk.aws.service.impl;

import static com.tkachuk.aws.utils.FileUtils.getFileExtension;
import static com.tkachuk.aws.utils.FileUtils.getInputStream;
import static com.tkachuk.aws.utils.FileUtils.toByteArray;
import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.tkachuk.aws.configuration.LambdaClientProperties;
import com.tkachuk.aws.configuration.S3ClientProperties;
import com.tkachuk.aws.dto.ImageInfoDTO;
import com.tkachuk.aws.dto.ImageUploadDTO;
import com.tkachuk.aws.entity.Image;
import com.tkachuk.aws.exception.ImageNotFoundException;
import com.tkachuk.aws.exception.InvalidImageDateException;
import com.tkachuk.aws.mapper.ImageMapper;
import com.tkachuk.aws.repository.ImageRepository;
import com.tkachuk.aws.service.AWSImageService;
import com.tkachuk.aws.service.AWSNotificationService;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class AWSImageServiceImpl implements AWSImageService {

    private static final String MSG_FORMAT_ERROR_WITH_INPUT_STREAM = "Error with inputStream for file -> %s";
    private static final String MSG_FORMAT_ERROR_WHEN_IMAGE_NOT_FOUND = "Image not found by name -> %s";
    private static final String LAMBDA_PAYLOAD = "{ \"detail-type\": \"AWSProjectApplication\" }";

    private final AmazonS3 amazonS3;
    private final AWSLambda awsLambda;

    private final S3ClientProperties s3ClientProperties;
    private final LambdaClientProperties lambdaClientProperties;

    private final ImageRepository imageRepository;
    private final ImageMapper imageMapper;
    private final AWSNotificationService awsNotificationService;

    @Override
    public ResponseEntity<?> uploadImage(ImageUploadDTO dto) {
        checkIsImageDTOValid(dto);
        InputStream inputStream = getInputStream(dto.getImage());
        if (isNull(inputStream)) {
            return ResponseEntity
                .internalServerError()
                .body(format(MSG_FORMAT_ERROR_WITH_INPUT_STREAM, dto.getImage().getOriginalFilename()));
        }

        var image = dto.getImage();
        var putObjectRequest = new PutObjectRequest(
            s3ClientProperties.getBucketName(),
            dto.getName(),
            inputStream,
            createObjectMetaData(dto.getName(), image.getContentType(), image.getSize()));

        log.debug("Put file -> {}, to s3 -> {}", dto.getName(), s3ClientProperties.getBucketName());
        amazonS3.putObject(putObjectRequest);

        Image newImage = createImageEntity(
            dto.getName(),
            getFileExtension(image.getContentType()),
            dto.getImage().getSize()
        );

        Image save = imageRepository.save(newImage);
        ImageInfoDTO imageInfoDTO = imageMapper.toImageInfoDTO(save);
        awsNotificationService.sendMessageToSQS(imageInfoDTO);

        return new ResponseEntity<>(imageInfoDTO, CREATED);
    }

    @Override
    public ResponseEntity<?> downloadImage(String fileName) {
        Image image = imageRepository.findByName(fileName)
            .orElseThrow(() -> new ImageNotFoundException(format(MSG_FORMAT_ERROR_WHEN_IMAGE_NOT_FOUND, fileName)));

        S3Object object = amazonS3.getObject(s3ClientProperties.getBucketName(), image.getName());
        byte[] bytes = toByteArray(object);

        return ResponseEntity
            .ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
            .body(bytes);
    }

    @Override
    public ResponseEntity<?> getImagesInfo(String fileName, Boolean randomEnable) {
        if (TRUE.equals(randomEnable)) {
            return imageRepository.findRandomImage()
                .map(image -> new ResponseEntity<>(imageMapper.toImageInfoDTO(image), OK))
                .orElseThrow(() -> new ImageNotFoundException("Images not found"));
        }

        if (isNull(fileName) || fileName.isBlank()) {
            return new ResponseEntity<>(imageRepository.findAll(), OK);
        }

        return imageRepository.findByName(fileName)
            .map(image -> new ResponseEntity<>(imageMapper.toImageInfoDTO(image), OK))
            .orElseThrow(() -> new ImageNotFoundException(format("Image with name %S not found", fileName)));
    }

    @Override
    public ResponseEntity<?> invokeImageLambda() {
        InvokeRequest invokeRequest = new InvokeRequest()
            .withFunctionName(lambdaClientProperties.getLambdaName())
            .withPayload(LAMBDA_PAYLOAD);

        var invokeResult = awsLambda.invoke(invokeRequest);

        return new ResponseEntity<>(new String(invokeResult.getPayload().array(), StandardCharsets.UTF_8), OK);
    }

    private void checkIsImageDTOValid(ImageUploadDTO dto) {
        if (isNull(dto.getImage())) {
            throw new InvalidImageDateException("Image cannot be null");
        }
        if (isNull(dto.getName())) {
            throw new InvalidImageDateException("Image name cannot be null");
        }
    }

    private ObjectMetadata createObjectMetaData(String name, String type, long size) {
        var objectMetadata = new ObjectMetadata();
        objectMetadata.addUserMetadata("name", name);
        objectMetadata.setContentType(type);
        objectMetadata.setContentLength(size);

        return objectMetadata;
    }

    private Image createImageEntity(String name, String fileExtension, long size) {
        Image image = new Image();
        image.setName(name);
        image.setFileExtension(fileExtension);
        image.setSize(size);

        return image;
    }

}
