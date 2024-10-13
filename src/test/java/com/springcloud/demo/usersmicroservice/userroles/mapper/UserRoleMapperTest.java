package com.springcloud.demo.usersmicroservice.userroles.mapper;


import com.springcloud.demo.usersmicroservice.exceptions.BadRequestException;
import com.springcloud.demo.usersmicroservice.userroles.dto.UserRoleResponseDTO;
import com.springcloud.demo.usersmicroservice.userroles.model.Roles;
import com.springcloud.demo.usersmicroservice.userroles.model.UserRole;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

class UserRoleMapperTest {

    @Test
    void userRoleToUserRoleResponseDTO(){
        UserRole userRole = UserRole
                .builder()
                .id(UUID.randomUUID())
                .role(Roles.CUSTOMER.name())
                .createdAt(LocalDateTime.now())
                .build();

        UserRoleResponseDTO userRoleResponseDTO = UserRoleMapper.userRoleToUserRoleResponseDTO(userRole);

        assertThat(userRoleResponseDTO.getId()).isEqualTo(userRole.getId());
        assertThat(userRoleResponseDTO.getRole()).isEqualTo(userRole.getRole());
    }

    @Test
    void stringToUserRole(){
        String role = "CUSTOMER";

        UserRole userRoleMapped = UserRoleMapper.stringToUserRole(role);

        assertThat(userRoleMapped.getRole()).isEqualTo(role);
    }

    @Test
    void errorWhenRoleStringIsNotValidRole(){
        String role = "OTHER_ROLE";

        BadRequestException e = Assertions.assertThrows(BadRequestException.class, ()->{
            UserRoleMapper.stringToUserRole(role);
        });

        assertThat(e.getMessage()).contains("Role not valid");
    }
}