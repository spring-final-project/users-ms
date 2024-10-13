package com.springcloud.demo.usersmicroservice.users.dto;

import com.springcloud.demo.usersmicroservice.userroles.dto.UserRoleResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDTO {
    private UUID id;
    private String name;
    private String email;
    private List<UserRoleResponseDTO> roles;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdated;
}
