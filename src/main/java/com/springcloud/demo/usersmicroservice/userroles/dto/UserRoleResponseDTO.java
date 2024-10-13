package com.springcloud.demo.usersmicroservice.userroles.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRoleResponseDTO {
    private String role;
    private UUID id;
    private LocalDateTime createdAt;
}
