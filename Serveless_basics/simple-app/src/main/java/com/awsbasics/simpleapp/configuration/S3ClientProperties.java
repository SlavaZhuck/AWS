package com.awsbasics.simpleapp.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "aws.mentoring.s3")
@Component
public class S3ClientProperties {
    private String region;
    private String bucketName;
}
