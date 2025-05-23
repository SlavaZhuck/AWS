package com.tkachuk.aws.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "aws.app.lambda")
@Component
public class LambdaClientProperties {

    private String region;
    private String lambdaName;

}
