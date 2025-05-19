package com.awsbasics.simpleapp.controller;

import com.awsbasics.simpleapp.dto.HealthCheckStatusDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health-check")
public class HealthCheckController {
    
    @GetMapping
    public ResponseEntity<HealthCheckStatusDto> getStatus() {
        var status = HealthCheckStatusDto.builder().httpStatus(HttpStatus.OK).build();
        return ResponseEntity.ok(status);
    }
}
