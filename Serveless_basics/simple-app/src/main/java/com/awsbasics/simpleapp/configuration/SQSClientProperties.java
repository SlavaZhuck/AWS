package com.awsbasics.simpleapp.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "aws.mentoring.sqs")
@Component
public class SQSClientProperties {
    private String region;
    private String queueUrl;
}
