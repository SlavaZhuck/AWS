package com.tkachuk.aws.configuration;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling//for Scheduled in listener
@RequiredArgsConstructor
public class AWSConfiguration {

    private final S3ClientProperties s3ClientProperties;
    private final SNSClientProperties snsClientProperties;
    private final SQSClientProperties sqsClientProperties;

    @Bean
    public AmazonS3 amazonS3() {
        return AmazonS3Client.builder()
            .withRegion(s3ClientProperties.getRegion())
            .build();
    }

    @Bean
    public AmazonSNS amazonSNS() {
        return AmazonSNSClient.builder()
            .withRegion(snsClientProperties.getRegion())
            .build();
    }

    @Bean
    public AmazonSQS amazonSQS() {
        return AmazonSQSClient.builder()
            .withRegion(sqsClientProperties.getRegion())
            .build();
    }

    @Bean
    public AWSLambda awsLambda() {
        return AWSLambdaClientBuilder.standard()
            .withRegion(sqsClientProperties.getRegion())
            .build();
    }

}
