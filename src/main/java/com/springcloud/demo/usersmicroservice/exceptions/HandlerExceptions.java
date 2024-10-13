package com.springcloud.demo.usersmicroservice.exceptions;

import com.springcloud.demo.usersmicroservice.monitoring.TracingExceptions;
import com.springcloud.demo.usersmicroservice.exceptions.dto.ErrorResponseDTO;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.ArrayList;
import java.util.List;

@Hidden
@RestControllerAdvice
@RequiredArgsConstructor
public class HandlerExceptions {

    private final TracingExceptions tracingExceptions;

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponseDTO handleNotFoundExceptions(NotFoundException e){
        tracingExceptions.addExceptionMetadata(e.getMessage());
        return ErrorResponseDTO
                .builder()
                .status(HttpStatus.NOT_FOUND.value())
                .message(e.getMessage())
                .build();
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponseDTO handleForbiddenExceptions(ForbiddenException e){
        tracingExceptions.addExceptionMetadata(e.getMessage());
        return ErrorResponseDTO
                .builder()
                .status(HttpStatus.FORBIDDEN.value())
                .message(e.getMessage())
                .build();
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDTO handleBadRequestException(BadRequestException e){
        tracingExceptions.addExceptionMetadata(e.getMessage());
        return ErrorResponseDTO
                .builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(e.getMessage())
                .build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDTO handleMethodArgumentNotValidException(MethodArgumentNotValidException e){
        List<String> errors = e.getFieldErrors().stream().map(err -> err.getField() + " " + err.getDefaultMessage()).toList();
        tracingExceptions.addExceptionMetadata(e.getMessage());
        return ErrorResponseDTO
                .builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .errors(errors)
                .build();
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDTO handleMethodValidationException(HandlerMethodValidationException e){
        List<String> errors = new ArrayList<>();

        if(e.getDetailMessageArguments() != null){
            for (Object obj:e.getDetailMessageArguments()){
                errors.add(obj.toString());
            }
        }

        tracingExceptions.addExceptionMetadata(e.getMessage());

        return ErrorResponseDTO
                .builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(e.getMessage())
                .errors(errors)
                .build();
    }

    /**
     * Handle errors when not exist body request
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDTO handleHttpMessageNotReadableException(HttpMessageNotReadableException e){
        System.out.println("---- HttpMessageNotReadableException ----");
        String message = e.getMessage().split(":")[0];

        tracingExceptions.addExceptionMetadata(e.getMessage());

        return ErrorResponseDTO
                .builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(message)
                .build();
    }

    /**
     * Handle errors when not exist id user logged header
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDTO handleMissingRequestHeaderException(MissingRequestHeaderException e){
        tracingExceptions.addExceptionMetadata(e.getMessage());

        return ErrorResponseDTO
                .builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(e.getMessage())
                .build();
    }


}
