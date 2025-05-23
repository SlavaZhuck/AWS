package com.tkachuk.aws.utils;

import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.multipart.MultipartFile;

@Log4j2
public final class FileUtils {

    private static final char DELIMITER = '/';

    private FileUtils() {

    }

    public static InputStream getInputStream(MultipartFile file) {
        try {
            return file.getInputStream();
        } catch (IOException ex) {
            log.error("Error with inputStream", ex);
            return null;
        }
    }

    public static String getFileExtension(String fileContentType) {
        return Optional.ofNullable(fileContentType)
            .map(contentType -> contentType.substring(contentType.indexOf(DELIMITER) + 1))
            .orElse(null);

    }

    public static byte[] toByteArray(S3Object s3Object) {
        try (S3ObjectInputStream objectContent = s3Object.getObjectContent()) {
            return IOUtils.toByteArray(objectContent);
        } catch (IOException ex) {
            log.error("Error while reading s3Object", ex);
            return null;
        }

    }
}
