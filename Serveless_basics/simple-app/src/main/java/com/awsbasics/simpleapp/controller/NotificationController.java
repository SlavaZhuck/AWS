package com.awsbasics.simpleapp.controller;

import com.awsbasics.simpleapp.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/subscriptions/{email}")
    public ResponseEntity<?> subscribeEmail(@PathVariable String email) {
        return notificationService.subscribeEmail(email);
    }

    @DeleteMapping("/subscriptions/{email}")
    public ResponseEntity<?> unsubscribeEmail(@PathVariable String email) {
        return notificationService.unsubscribeEmail(email);
    }
}
