package com.awsbasics.simpleapp.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.awsbasics.simpleapp.exception.InvalidFileException;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static java.util.Optional.of;

@Service
@Log4j2
public class FileService {

    public InputStream getInputStream(MultipartFile multipartFile) {
        InputStream inputStream = null;
        try {
            inputStream = multipartFile.getInputStream();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return inputStream;
    }

    public String getFileExtension(String fileName) {
        return of(fileName)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(fileName.lastIndexOf(".") + 1))
                .orElseThrow(InvalidFileException::new);
    }

    public byte[] readBitmap(S3Object s3Object) {
        byte[] objectBytes = new byte[1024];
        try (S3ObjectInputStream s3is = s3Object.getObjectContent();
             ByteArrayOutputStream os = new ByteArrayOutputStream()) {

            byte[] read_buf = new byte[1024];
            int read_len;
            while ((read_len = s3is.read(read_buf)) > 0) {
                os.write(read_buf, 0, read_len);
            }
            objectBytes = os.toByteArray();
        } catch (AmazonServiceException e) {
            log.error(e.getErrorMessage());
            System.exit(1);
        } catch (IOException e) {
            log.error(e.getMessage());
            System.exit(1);
        }
        return objectBytes;
    }
}
