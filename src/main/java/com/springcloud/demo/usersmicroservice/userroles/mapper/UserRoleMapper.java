package com.springcloud.demo.usersmicroservice.userroles.mapper;

import com.springcloud.demo.usersmicroservice.exceptions.BadRequestException;
import com.springcloud.demo.usersmicroservice.userroles.dto.UserRoleResponseDTO;
import com.springcloud.demo.usersmicroservice.userroles.model.Roles;
import com.springcloud.demo.usersmicroservice.userroles.model.UserRole;

import java.util.Arrays;


public class UserRoleMapper {

    public static UserRoleResponseDTO userRoleToUserRoleResponseDTO(UserRole userRole){
        return UserRoleResponseDTO.builder()
                .id(userRole.getId())
                .role(userRole.getRole())
                .createdAt(userRole.getCreatedAt())
                .build();
    }

    public static UserRole stringToUserRole(String role){
        Roles userRole = Roles.CUSTOMER;
        if(role != null){
            try {
                userRole = Roles.valueOf(role);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Role not valid." + Arrays.toString(Roles.values()) );
            }
        }

        return UserRole.builder().role(userRole.name()).build();
    }
}
