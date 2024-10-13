package com.springcloud.demo.usersmicroservice.userroles.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.springcloud.demo.usersmicroservice.userroles.dto.AddRoleDTO;
import com.springcloud.demo.usersmicroservice.userroles.model.Roles;
import com.springcloud.demo.usersmicroservice.userroles.model.UserRole;
import com.springcloud.demo.usersmicroservice.userroles.repository.UserRoleRepository;
import com.springcloud.demo.usersmicroservice.users.model.User;
import com.springcloud.demo.usersmicroservice.users.repository.UserRepository;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;
import java.util.UUID;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class UserRoleTestIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserRoleRepository userRoleRepository;

    User user1;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();

        UserRole defaultUserRole = UserRole.builder().role(Roles.CUSTOMER.name()).build();
        user1 = User.builder()
                .email("user1@gmail.com")
                .name("user1")
                .password("encrypted_password")
                .build();
        defaultUserRole.setUser(user1);
        user1.setRoles(List.of(defaultUserRole));
        userRepository.save(user1);
    }

    @Nested
    class AddUserRole {

        @Test
        void addUserRole() throws Exception {

            AddRoleDTO addRoleDTO = new AddRoleDTO(Roles.OWNER.name());

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                            .post("/api/users/" + user1.getId() + "/roles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(addRoleDTO))
                            .header("X-UserId", user1.getId().toString())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.CREATED.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.role").value(addRoleDTO.getRole()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").isString())
                    .andReturn();

            String idRoleCreated = JsonPath.parse(result.getResponse().getContentAsString()).read("$.id");

            UserRole userRoleCreated = userRoleRepository.findById(UUID.fromString(idRoleCreated)).orElseThrow();
            assertThat(userRoleCreated.getRole()).isEqualTo(Roles.OWNER.name());
            assertThat(userRoleCreated.getUser().getId()).isEqualTo(user1.getId());

        }

        @Test
        void errorWhenUserAlreadyHasSameRole() throws Exception {

            AddRoleDTO addRoleDTO = new AddRoleDTO(Roles.CUSTOMER.name());

            mockMvc.perform(MockMvcRequestBuilders
                            .post("/api/users/" + user1.getId() + "/roles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(addRoleDTO))
                            .header("X-UserId", user1.getId().toString())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.FORBIDDEN.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").isString());

            List<UserRole> userRoles = userRoleRepository.findAll();
            assertThat(userRoles.size()).isEqualTo(1);

        }

        @Test
        void errorWhenUserNotExist() throws Exception {

            String idUserToAddRole = UUID.randomUUID().toString();
            AddRoleDTO addRoleDTO = new AddRoleDTO(Roles.CUSTOMER.name());

            mockMvc.perform(MockMvcRequestBuilders
                            .post("/api/users/" + idUserToAddRole + "/roles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(addRoleDTO))
                            .header("X-UserId", idUserToAddRole)
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.NOT_FOUND.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").isString());

            List<UserRole> userRoles = userRoleRepository.findAll();
            assertThat(userRoles.size()).isEqualTo(1);

        }

        @Test
        void errorWhenUserIdIsNotValidUUID() throws Exception {

            AddRoleDTO addRoleDTO = new AddRoleDTO(Roles.CUSTOMER.name());

            mockMvc.perform(MockMvcRequestBuilders
                            .post("/api/users/abcde/roles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(addRoleDTO))
                            .header("X-UserId", "abcde")
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").isString());

            List<UserRole> userRoles = userRoleRepository.findAll();
            assertThat(userRoles.size()).isEqualTo(1);

        }
    }

    @Nested
    class DeleteUserRole {

        UserRole newUserRole;

        @BeforeEach
        void setup() {
            newUserRole = UserRole.builder().role(Roles.OWNER.name()).user(user1).build();
            userRoleRepository.save(newUserRole);
        }

        @Test
        void deleteUserRole() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders
                            .delete("/api/users/roles/" + newUserRole.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("X-UserId", user1.getId().toString())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.ok").value(true));

            List<UserRole> userRoles = userRoleRepository.findAll();
            assertThat(userRoles.size()).isEqualTo(1);
            assertThat(userRoles.getFirst().getRole()).isNotEqualTo(newUserRole.getRole());
        }

        @Test
        void errorWhenNotFoundUserRoleById() throws Exception {
            String idUserToDeleteRole = UUID.randomUUID().toString();
            mockMvc.perform(MockMvcRequestBuilders
                            .delete("/api/users/roles/" + idUserToDeleteRole)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("X-UserId", idUserToDeleteRole)
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.NOT_FOUND.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").isString());

            List<UserRole> userRoles = userRoleRepository.findAll();
            assertThat(userRoles.size()).isEqualTo(2);
        }

        @Test
        void errorWhenUserRoleIdIsNotValidUUID() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders
                            .delete("/api/users/roles/abcdefg")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("X-UserId", "abcdefg")
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errors.size()").value(1));

            List<UserRole> userRoles = userRoleRepository.findAll();
            assertThat(userRoles.size()).isEqualTo(2);
        }
    }
}
