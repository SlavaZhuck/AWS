package com.tkachuk.aws.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "aws.app.sqs")
@Component
public class SQSClientProperties {

    private String region;
    private String queueUrl;

}
