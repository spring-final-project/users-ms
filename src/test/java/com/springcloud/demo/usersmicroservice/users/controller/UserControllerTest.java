package com.springcloud.demo.usersmicroservice.users.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springcloud.demo.usersmicroservice.dto.SimpleResponseDTO;
import com.springcloud.demo.usersmicroservice.monitoring.TracingExceptions;
import com.springcloud.demo.usersmicroservice.userroles.dto.UserRoleResponseDTO;
import com.springcloud.demo.usersmicroservice.userroles.model.Roles;
import com.springcloud.demo.usersmicroservice.users.dto.CreateUserDTO;
import com.springcloud.demo.usersmicroservice.users.dto.UpdateUserDTO;
import com.springcloud.demo.usersmicroservice.users.dto.UserResponseDTO;
import com.springcloud.demo.usersmicroservice.users.service.UserService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.mockito.BDDMockito.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private TracingExceptions tracingExceptions;

    @Nested
    class Create {

        @Test
        void createUser() throws Exception {
            CreateUserDTO createUserDTO = new CreateUserDTO("Gonza", "gonzalo@gmail.com", "Abcd1234.", null);
            UserRoleResponseDTO userRoleCreated = UserRoleResponseDTO.builder().id(UUID.fromString("a399bded-044d-4ffc-b2e7-70cefb21ed25")).role(Roles.CUSTOMER.name()).createdAt(LocalDateTime.now()).build();
            UserResponseDTO userSaved = UserResponseDTO.builder().id(UUID.fromString("d0dd04ab-a40a-48a1-877e-8833e66342fb")).name(createUserDTO.getName()).email(createUserDTO.getEmail()).roles(List.of(userRoleCreated)).createdAt(LocalDateTime.now()).lastUpdated(LocalDateTime.now()).build();

            given(userService.create(any(CreateUserDTO.class))).willReturn(userSaved);

            mockMvc.perform(MockMvcRequestBuilders
                            .post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(createUserDTO))
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.CREATED.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(createUserDTO.getEmail()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(createUserDTO.getName()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.password").doesNotExist());
        }

        @Test
        void errorWhenNotSendEmail() throws Exception {
            CreateUserDTO createUserDTO = new CreateUserDTO("Gonza", null, "Abcd1234.", null);

            mockMvc.perform(MockMvcRequestBuilders
                            .post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(createUserDTO))
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.email").doesNotExist())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.name").doesNotExist())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errors", Matchers.not(Matchers.emptyIterable())));

            verify(userService, never()).create(any());
        }

        @Test
        void errorWhenPasswordIsVeryBasic() throws Exception {
            CreateUserDTO createUserDTO = new CreateUserDTO("Gonza", "gonza@gmail.com", "abcd1234", null);

            mockMvc.perform(MockMvcRequestBuilders
                            .post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(createUserDTO))
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.email").doesNotExist())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.name").doesNotExist())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errors", Matchers.not(Matchers.emptyIterable())));

            verify(userService, never()).create(any());
        }
    }

    @Nested
    class FindAll {

        @Test
        void findAllUsers() throws Exception {
            UserRoleResponseDTO userRoleCreated1 = UserRoleResponseDTO.builder().id(UUID.fromString("a399bded-044d-4ffc-b2e7-70cefb21ed24")).role(Roles.CUSTOMER.name()).createdAt(LocalDateTime.now()).build();
            UserResponseDTO userSaved1 = UserResponseDTO.builder().id(UUID.fromString("d0dd04ab-a40a-48a1-877e-8833e66342fa")).name("user1").email("user1@gmail.com").roles(List.of(userRoleCreated1)).createdAt(LocalDateTime.now()).lastUpdated(LocalDateTime.now()).build();
            UserRoleResponseDTO userRoleCreated2 = UserRoleResponseDTO.builder().id(UUID.fromString("a399bded-044d-4ffc-b2e7-70cefb21ed25")).role(Roles.CUSTOMER.name()).createdAt(LocalDateTime.now()).build();
            UserResponseDTO userSaved2 = UserResponseDTO.builder().id(UUID.fromString("d0dd04ab-a40a-48a1-877e-8833e66342fb")).name("user2").email("user2@gmail.co").roles(List.of(userRoleCreated2)).createdAt(LocalDateTime.now()).lastUpdated(LocalDateTime.now()).build();

            given(userService.findAll(any())).willReturn(List.of(userSaved1, userSaved2));

            mockMvc.perform(MockMvcRequestBuilders.get("/api/users").contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.is(userSaved1.getId().toString())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[1].id", Matchers.is(userSaved2.getId().toString())));

            verify(userService).findAll(argThat(filters -> filters.getPage() == null && filters.getLimit() == null && filters.getQ() == null));
        }

        @Test
        void findAllUsersWithPagination() throws Exception {
            UserRoleResponseDTO userRoleCreated1 = UserRoleResponseDTO.builder().id(UUID.fromString("a399bded-044d-4ffc-b2e7-70cefb21ed24")).role(Roles.CUSTOMER.name()).createdAt(LocalDateTime.now()).build();
            UserResponseDTO userSaved1 = UserResponseDTO.builder().id(UUID.fromString("d0dd04ab-a40a-48a1-877e-8833e66342fa")).name("user1").email("user1@gmail.com").roles(List.of(userRoleCreated1)).createdAt(LocalDateTime.now()).lastUpdated(LocalDateTime.now()).build();
            UserRoleResponseDTO userRoleCreated2 = UserRoleResponseDTO.builder().id(UUID.fromString("a399bded-044d-4ffc-b2e7-70cefb21ed25")).role(Roles.CUSTOMER.name()).createdAt(LocalDateTime.now()).build();
            UserResponseDTO userSaved2 = UserResponseDTO.builder().id(UUID.fromString("d0dd04ab-a40a-48a1-877e-8833e66342fb")).name("user2").email("user2@gmail.co").roles(List.of(userRoleCreated2)).createdAt(LocalDateTime.now()).lastUpdated(LocalDateTime.now()).build();

            given(userService.findAll(any())).willReturn(List.of(userSaved1, userSaved2));

            mockMvc.perform(MockMvcRequestBuilders
                            .get("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .queryParam("page", "2")
                            .queryParam("limit", "30")
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.is(userSaved1.getId().toString())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[1].id", Matchers.is(userSaved2.getId().toString())));

            verify(userService).findAll(argThat(filters -> Objects.equals(filters.getPage(), 2) && Objects.equals(filters.getLimit(), 30) && filters.getQ() == null));
        }

        @Test
        void findAllUsersWithPaginationAndTermToSearch() throws Exception {
            UserRoleResponseDTO userRoleCreated1 = UserRoleResponseDTO.builder().id(UUID.fromString("a399bded-044d-4ffc-b2e7-70cefb21ed24")).role(Roles.CUSTOMER.name()).createdAt(LocalDateTime.now()).build();
            UserResponseDTO userSaved1 = UserResponseDTO.builder().id(UUID.fromString("d0dd04ab-a40a-48a1-877e-8833e66342fa")).name("user1").email("user1@gmail.com").roles(List.of(userRoleCreated1)).createdAt(LocalDateTime.now()).lastUpdated(LocalDateTime.now()).build();
            UserRoleResponseDTO userRoleCreated2 = UserRoleResponseDTO.builder().id(UUID.fromString("a399bded-044d-4ffc-b2e7-70cefb21ed25")).role(Roles.CUSTOMER.name()).createdAt(LocalDateTime.now()).build();
            UserResponseDTO userSaved2 = UserResponseDTO.builder().id(UUID.fromString("d0dd04ab-a40a-48a1-877e-8833e66342fb")).name("user2").email("user2@gmail.co").roles(List.of(userRoleCreated2)).createdAt(LocalDateTime.now()).lastUpdated(LocalDateTime.now()).build();

            given(userService.findAll(any())).willReturn(List.of(userSaved1, userSaved2));

            mockMvc.perform(MockMvcRequestBuilders
                            .get("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .queryParam("page", "2")
                            .queryParam("limit", "30")
                            .queryParam("q", "user")
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].id", Matchers.is(userSaved1.getId().toString())))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[1].id", Matchers.is(userSaved2.getId().toString())));

            verify(userService).findAll(argThat(filters -> Objects.equals(filters.getPage(), 2) && Objects.equals(filters.getLimit(), 30) && Objects.equals(filters.getQ(), "user")));
        }

        @Test
        void errorOnPaginationWrongParams() throws Exception {

            mockMvc.perform(MockMvcRequestBuilders
                            .get("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .queryParam("page", "-1")
                            .queryParam("limit", "0")
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errors.size()").value(2));

            verify(userService, never()).findAll(any());
        }
    }

    @Nested
    class FindById {

        @Test
        void findById() throws Exception {
            UserRoleResponseDTO userRoleCreated = UserRoleResponseDTO.builder().id(UUID.fromString("a399bded-044d-4ffc-b2e7-70cefb21ed24")).role(Roles.CUSTOMER.name()).createdAt(LocalDateTime.now()).build();
            UserResponseDTO userSaved = UserResponseDTO.builder().id(UUID.fromString("d0dd04ab-a40a-48a1-877e-8833e66342fa")).name("user1").email("user1@gmail.com").roles(List.of(userRoleCreated)).createdAt(LocalDateTime.now()).lastUpdated(LocalDateTime.now()).build();

            given(userService.findById(any())).willReturn(userSaved);

            mockMvc.perform(MockMvcRequestBuilders
                            .get("/api/users/" + userSaved.getId().toString())
                            .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(userSaved.getId().toString())));

            verify(userService).findById(argThat(arg -> arg.equals(userSaved.getId().toString())));
        }

        @Test
        void errorWhenIdIsNotValidUUID() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders
                            .get("/api/users/abcd")
                            .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errors.size()", Matchers.greaterThanOrEqualTo(1)));

            verify(userService, never()).findById(any());
        }
    }

    @Nested
    class Update {

        @Test
        void updateUser() throws Exception {
            UpdateUserDTO updateUserDTO = new UpdateUserDTO(null, "nuevo_mail@gmail.com", null);
            UserRoleResponseDTO userRoleCreated = UserRoleResponseDTO.builder().id(UUID.fromString("a399bded-044d-4ffc-b2e7-70cefb21ed24")).role(Roles.CUSTOMER.name()).createdAt(LocalDateTime.now()).build();
            UserResponseDTO userUpdated = UserResponseDTO.builder().id(UUID.fromString("d0dd04ab-a40a-48a1-877e-8833e66342fa")).name("user1").email(updateUserDTO.getEmail()).roles(List.of(userRoleCreated)).createdAt(LocalDateTime.now()).lastUpdated(LocalDateTime.now()).build();

            given(userService.update(any(UpdateUserDTO.class), anyString())).willReturn(userUpdated);

            mockMvc.perform(MockMvcRequestBuilders
                            .patch("/api/users/" + userUpdated.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(updateUserDTO))
                            .header("X-UserId", userUpdated.getId().toString())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(userUpdated.getId().toString()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(updateUserDTO.getEmail()));

            verify(userService).update(argThat(dto -> dto.getEmail().equals(updateUserDTO.getEmail())), argThat(pathId -> pathId.equals(userUpdated.getId().toString())));
        }

        @Test
        void errorWhenIdIsNotValidUUID() throws Exception {
            UpdateUserDTO updateUserDTO = new UpdateUserDTO(null, "nuevo_mail@gmail.com", null);

            mockMvc.perform(MockMvcRequestBuilders
                            .patch("/api/users/abcd")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(updateUserDTO))
                            .header("X-UserId", UUID.randomUUID().toString())
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").doesNotExist())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errors.size()", Matchers.greaterThanOrEqualTo(1)));

            verify(userService, never()).update(any(), anyString());
        }

        @Test
        void errorWhenEmailHasNoValidFormat() throws Exception {
            UpdateUserDTO updateUserDTO = new UpdateUserDTO(null, "nuevo_mail", null);
            String id = UUID.randomUUID().toString();

            mockMvc.perform(MockMvcRequestBuilders
                            .patch("/api/users/" + id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(updateUserDTO))
                            .header("X-UserId", id)
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").doesNotExist())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errors.size()").value(1));

            verify(userService, never()).update(any(), anyString());
        }
    }

    @Nested
    class Delete {

        @Test
        void deleteUser() throws Exception {
            String idUserToDelete = "d0dd04ab-a40a-48a1-877e-8833e66342fa";

            given(userService.delete(anyString())).willReturn(new SimpleResponseDTO(true));

            mockMvc.perform(MockMvcRequestBuilders
                            .delete("/api/users/" + idUserToDelete)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("X-UserId", idUserToDelete)
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.ok").value(true));

            verify(userService).delete(argThat(arg -> arg.equals(idUserToDelete)));
        }

        @Test
        void errorWhenIdIsNotValidUUID() throws Exception {
            String idUserToDelete = "abcde";

            mockMvc.perform(MockMvcRequestBuilders
                            .delete("/api/users/" + idUserToDelete)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("X-UserId", idUserToDelete)
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.not(Matchers.isA(String.class))))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errors.size()", Matchers.greaterThanOrEqualTo(1)));

            verify(userService, never()).delete(anyString());
        }
    }
}