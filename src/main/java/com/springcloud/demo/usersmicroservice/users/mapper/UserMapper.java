package com.springcloud.demo.usersmicroservice.users.mapper;

import com.springcloud.demo.usersmicroservice.userroles.mapper.UserRoleMapper;
import com.springcloud.demo.usersmicroservice.users.dto.CreateUserDTO;
import com.springcloud.demo.usersmicroservice.users.dto.UpdateUserDTO;
import com.springcloud.demo.usersmicroservice.users.dto.UserAuthResponseDTO;
import com.springcloud.demo.usersmicroservice.users.dto.UserResponseDTO;
import com.springcloud.demo.usersmicroservice.users.model.User;

import java.util.Optional;


public class UserMapper {

    static public User createUserDtoToUser(CreateUserDTO createUserDTO) {
        return User
                .builder()
                .email(createUserDTO.getEmail())
                .name(createUserDTO.getName())
                .password(createUserDTO.getPassword())
                .build();
    }

    static public User userToUserUpdated(User user, UpdateUserDTO userDTO) {

        Optional.ofNullable(userDTO.getName()).ifPresent(user::setName);
        Optional.ofNullable(userDTO.getEmail()).ifPresent(user::setEmail);
        Optional.ofNullable(userDTO.getPassword()).ifPresent(user::setPassword);

        return user;
    }

    static public UserResponseDTO userToUserResponseDto(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .roles(user
                        .getRoles()
                        .stream()
                        .map(UserRoleMapper::userRoleToUserRoleResponseDTO)
                        .toList())
                .createdAt(user.getCreatedAt())
                .lastUpdated(user.getLastUpdated())
                .build();

    }

    static public UserAuthResponseDTO userToUserAuthResponseDto(User user) {
        return UserAuthResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(user.getPassword())
                .roles(user
                        .getRoles()
                        .stream()
                        .map(UserRoleMapper::userRoleToUserRoleResponseDTO)
                        .toList())
                .build();

    }
}
