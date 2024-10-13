package com.springcloud.demo.usersmicroservice.userroles.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AddRoleDTO {

    @NotBlank
    String role;
}
