package com.tkachuk.aws.controller;

import static com.tkachuk.aws.controller.AWSNotificationController.REST_VERSION;

import com.tkachuk.aws.service.AWSNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = REST_VERSION + "/notifications")
@RequiredArgsConstructor
public class AWSNotificationController {

    public static final String REST_VERSION = "v1";

    private final AWSNotificationService awsNotificationService;

    @PostMapping("/subscribe/{email}")
    public ResponseEntity<?> subscribe(@PathVariable String email) {
        return awsNotificationService.subscribe(email);
    }

    @PostMapping("/unsubscribe/{email}")
    public ResponseEntity<?> unsubscribe(@PathVariable String email) {
        return awsNotificationService.unsubscribe(email);
    }

}
