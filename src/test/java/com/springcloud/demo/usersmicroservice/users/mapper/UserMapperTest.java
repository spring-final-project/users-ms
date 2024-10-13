package com.springcloud.demo.usersmicroservice.users.mapper;


import com.springcloud.demo.usersmicroservice.userroles.model.Roles;
import com.springcloud.demo.usersmicroservice.users.dto.CreateUserDTO;
import com.springcloud.demo.usersmicroservice.users.dto.UpdateUserDTO;
import com.springcloud.demo.usersmicroservice.users.dto.UserResponseDTO;
import com.springcloud.demo.usersmicroservice.users.model.User;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

class UserMapperTest {

    @Test
    void createUserDtoToUser(){
        CreateUserDTO createUserDTO = new CreateUserDTO("user1","user1@gmail.com","Abcde1234.", Roles.CUSTOMER.name());

        User userMapped = UserMapper.createUserDtoToUser(createUserDTO);

        assertThat(userMapped.getId()).isNull();
        assertThat(userMapped.getName()).isEqualTo(createUserDTO.getName());
        assertThat(userMapped.getEmail()).isEqualTo(createUserDTO.getEmail());
        assertThat(userMapped.getPassword()).isEqualTo(createUserDTO.getPassword());
        assertThat(userMapped.getRoles()).isNull();
    }

    @Test
    void userToUserUpdated(){
        User user = User
                .builder()
                .id(UUID.randomUUID())
                .email("user1@gmail.com")
                .name("User1")
                .lastUpdated(LocalDateTime.now())
                .password("encrypted_password")
                .createdAt(LocalDateTime.now())
                .build();

        UpdateUserDTO updateUserDTO = new UpdateUserDTO("New Name", "new_email@gmail.com","new_password");

        User userUpdated = UserMapper.userToUserUpdated(user,updateUserDTO);

        assertThat(userUpdated.getId()).isEqualTo(user.getId());
        assertThat(userUpdated.getEmail()).isEqualTo(updateUserDTO.getEmail());
        assertThat(userUpdated.getName()).isEqualTo(updateUserDTO.getName());
        assertThat(userUpdated.getPassword()).isEqualTo(updateUserDTO.getPassword());
    }

    @Test
    void userToUserResponseDto(){
        User user = User
                .builder()
                .id(UUID.randomUUID())
                .email("user1@gmail.com")
                .name("User1")
                .lastUpdated(LocalDateTime.now())
                .password("encrypted_password")
                .createdAt(LocalDateTime.now())
                .roles(List.of())
                .build();

        UserResponseDTO userResponseDTO = UserMapper.userToUserResponseDto(user);

        assertThat(userResponseDTO.getId()).isEqualTo(user.getId());
        assertThat(userResponseDTO.getName()).isEqualTo(user.getName());
        assertThat(userResponseDTO.getEmail()).isEqualTo(user.getEmail());
        assertThat(userResponseDTO.getCreatedAt()).isEqualTo(user.getCreatedAt());
        assertThat(userResponseDTO.getLastUpdated()).isEqualTo(user.getLastUpdated());
    }

}