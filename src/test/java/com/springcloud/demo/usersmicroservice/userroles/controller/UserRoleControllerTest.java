package com.springcloud.demo.usersmicroservice.userroles.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springcloud.demo.usersmicroservice.dto.SimpleResponseDTO;
import com.springcloud.demo.usersmicroservice.monitoring.TracingExceptions;
import com.springcloud.demo.usersmicroservice.userroles.dto.AddRoleDTO;
import com.springcloud.demo.usersmicroservice.userroles.dto.UserRoleResponseDTO;
import com.springcloud.demo.usersmicroservice.userroles.model.Roles;
import com.springcloud.demo.usersmicroservice.userroles.service.UserRoleService;
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
import java.util.UUID;

@WebMvcTest(UserRoleController.class)
class UserRoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRoleService userRoleService;

    @MockBean
    private TracingExceptions tracingExceptions;

    @Nested
    class AddUserRole {

        @Test
        void addUserRole() throws Exception {
            AddRoleDTO addRoleDTO = new AddRoleDTO(Roles.OWNER.name());
            String userId = UUID.randomUUID().toString();
            UserRoleResponseDTO userRoleCreated = new UserRoleResponseDTO(Roles.OWNER.name(), UUID.randomUUID(), LocalDateTime.now());

            given(userRoleService.addUserRole(any(String.class), any())).willReturn(userRoleCreated);

            mockMvc.perform(MockMvcRequestBuilders
                            .post("/api/users/" + userId +"/roles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(addRoleDTO))
                            .header("X-UserId", userId)
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.CREATED.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.role").value(addRoleDTO.getRole()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").isString());
        }

        @Test
        void errorWhenUserNotExist() throws Exception {
            AddRoleDTO addRoleDTO = new AddRoleDTO(null);
            String userId = UUID.randomUUID().toString();

            mockMvc.perform(MockMvcRequestBuilders
                            .post("/api/users/" + userId +"/roles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(addRoleDTO))
                            .header("X-UserId", userId)
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errors.size()").value(1));

            verify(userRoleService,never()).addUserRole(any(String.class),any());
        }

        @Test
        void errorWhenIdUserIsNotValidUUID() throws Exception {
            AddRoleDTO addRoleDTO = new AddRoleDTO(Roles.OWNER.name());
            String userId = "abcdefg";

            mockMvc.perform(MockMvcRequestBuilders
                            .post("/api/users/" + userId +"/roles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(addRoleDTO))
                            .header("X-UserId", userId)
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errors.size()").value(1));

            verify(userRoleService,never()).addUserRole(any(String.class),any());
        }
    }

    @Nested
    class DeleteUserRole {

        @Test
        void deleteUserRole() throws Exception {

            given(userRoleService.deleteUserRole(anyString(), anyString())).willReturn(new SimpleResponseDTO(true));

            mockMvc.perform(MockMvcRequestBuilders
                    .delete("/api/users/roles/" + UUID.randomUUID())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-UserId", UUID.randomUUID().toString())
            )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.ok").value(true));
        }

        @Test
        void errorWhenUserRoleIdIsNotValidUUID() throws Exception {

            given(userRoleService.deleteUserRole(anyString(), anyString())).willReturn(new SimpleResponseDTO(true));

            mockMvc.perform(MockMvcRequestBuilders
                    .delete("/api/users/roles/abcde")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-UserId", UUID.randomUUID().toString())
            )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.errors.size()").value(1));
        }
    }
}
