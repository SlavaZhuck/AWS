package com.awsbasics.simpleapp.dto;

import lombok.Builder;
import lombok.Value;
import org.springframework.http.HttpStatus;

@Value
@Builder
public class HealthCheckStatusDto {
    
    HttpStatus httpStatus;
}
