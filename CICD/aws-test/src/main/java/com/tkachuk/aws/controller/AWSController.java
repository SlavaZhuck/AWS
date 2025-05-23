package com.tkachuk.aws.controller;

import com.amazonaws.util.EC2MetadataUtils;
import com.amazonaws.util.EC2MetadataUtils.InstanceInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AWSController {

    @GetMapping("/")
    public String index() {
        return "Hello from AWS!";
    }

    @GetMapping("/v1/test")
    public InstanceInfo test() {
        return EC2MetadataUtils.getInstanceInfo();
    }

    @GetMapping("/v1/test2")
    public String test2() {
        return String.format("Availability Zone: %s, Instance Type: %s",
            EC2MetadataUtils.getAvailabilityZone(),
            EC2MetadataUtils.getInstanceType());
    }

}
