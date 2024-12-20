package com.springcloud.demo.usersmicroservice.users.dto;

import com.springcloud.demo.usersmicroservice.userroles.dto.UserRoleResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserAuthResponseDTO {
    private UUID id;
    private String email;
    private String password;
    private List<UserRoleResponseDTO> roles;
}
