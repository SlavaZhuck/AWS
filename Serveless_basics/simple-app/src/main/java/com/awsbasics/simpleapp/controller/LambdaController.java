package com.awsbasics.simpleapp.controller;

import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

@RestController
@RequestMapping("/lambda")
public class LambdaController {

    @GetMapping("/invoke")
    public String invokeLambda() {
        // Create an AWS Lambda client
        LambdaClient lambdaClient = LambdaClient.create();

        // Specify the Lambda function name or ARN
        String functionName = "DataConsistencyFunction";

        // Create an InvokeRequest without a payload
        InvokeRequest invokeRequest = InvokeRequest.builder()
                .functionName(functionName)
                .build();

        // Invoke the Lambda function
        InvokeResponse invokeResponse = lambdaClient.invoke(invokeRequest);

        // Get the response payload (if any)
        String responsePayload = invokeResponse.payload().asUtf8String();

        // Return the response
        return responsePayload.isEmpty() ? "Lambda function invoked successfully!" : responsePayload;
    }
}