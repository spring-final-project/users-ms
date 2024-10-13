package com.springcloud.demo.usersmicroservice.exceptions.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ErrorResponseDTO {
    String message;
    int status;
    List<String> errors;
}
