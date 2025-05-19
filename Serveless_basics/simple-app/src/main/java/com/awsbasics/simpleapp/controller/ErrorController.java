package com.awsbasics.simpleapp.controller;

import com.awsbasics.simpleapp.dto.ErrorDto;
import com.awsbasics.simpleapp.exception.InvalidFileException;
import com.awsbasics.simpleapp.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
@RequiredArgsConstructor
public class ErrorController {

    private final ErrorAttributes errorAttributes;

    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<ErrorDto> handleInvalidFileException(HttpServletRequest request) {
        return ResponseEntity.status(BAD_REQUEST)
                .body(buildErrorDto(request));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorDto> handleNotFoundException(HttpServletRequest request) {
        return ResponseEntity.status(NOT_FOUND)
                .body(buildErrorDto(request));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleException(HttpServletRequest request) {
        return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                .body(buildErrorDto(request));
    }

    private ErrorDto buildErrorDto(HttpServletRequest request) {
        ServletWebRequest servletWebRequest = new ServletWebRequest(request);
        Throwable error = errorAttributes.getError(servletWebRequest);
        String message = getErrorMessage(error);
        return ErrorDto.builder()
                .message(message)
                .build();
    }

    private String getErrorMessage(Throwable e) {
        String message = null;
        if (e != null) {
            message = e.getMessage();
            message = !StringUtils.hasLength(message)
                    && e.getCause() != null && !StringUtils.hasLength(e.getCause().getMessage()) ? e
                    .getCause()
                    .getMessage() : message;
        }
        return message;
    }
}
