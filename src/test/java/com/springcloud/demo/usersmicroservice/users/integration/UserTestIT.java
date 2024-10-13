package com.springcloud.demo.usersmicroservice.users.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springcloud.demo.usersmicroservice.userroles.model.Roles;
import com.springcloud.demo.usersmicroservice.userroles.model.UserRole;
import com.springcloud.demo.usersmicroservice.users.dto.CreateUserDTO;
import com.springcloud.demo.usersmicroservice.users.dto.UpdateUserDTO;
import com.springcloud.demo.usersmicroservice.users.model.User;
import com.springcloud.demo.usersmicroservice.users.repository.UserRepository;

import static org.assertj.core.api.Assertions.*;

import org.hamcrest.Matchers;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;
import java.util.Optional;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class UserTestIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void setup() {
        userRepository.deleteAll();
    }

    @Nested
    class CreateUser {

        @Test
        void createUser() throws Exception {
            CreateUserDTO createUserDTO = new CreateUserDTO("Gonza", "gonzalo@gmail.com", "Abcd1234.", null);

            mockMvc.perform(MockMvcRequestBuilders
                            .post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(createUserDTO))
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.CREATED.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").isString())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.password").doesNotExist())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(createUserDTO.getEmail()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(createUserDTO.getName()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.roles", Matchers.hasSize(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.roles[0].role").value(Roles.CUSTOMER.name()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.roles[0].id").isString());

            User userSaved = userRepository.findByEmail(createUserDTO.getEmail()).orElseThrow(() -> new AssertionError("user not found in database"));
            assertThat(userSaved.getPassword()).isNotEqualTo(createUserDTO.getPassword());
        }

        @Test
        void createUserWithCustomRole() throws Exception {
            CreateUserDTO createUserDTO = new CreateUserDTO("Gonza", "gonzalo@gmail.com", "Abcd1234.", "OWNER");

            mockMvc.perform(MockMvcRequestBuilders
                            .post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(createUserDTO))
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.CREATED.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").isString())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.password").doesNotExist())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(createUserDTO.getEmail()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(createUserDTO.getName()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.roles", Matchers.hasSize(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.roles[0].role").value(Roles.OWNER.name()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.roles[0].id").isString());

            User userSaved = userRepository.findByEmail(createUserDTO.getEmail()).orElseThrow(() -> new AssertionError("user not found in database"));
            assertThat(userSaved.getPassword()).isNotEqualTo(createUserDTO.getPassword());
        }

        @Test
        void errorOnCreateUserWithRoleThatNotExist() throws Exception {
            CreateUserDTO createUserDTO = new CreateUserDTO("Gonza", "gonzalo@gmail.com", "Abcd1234.", "OTHER_ROLE");

            mockMvc.perform(MockMvcRequestBuilders
                            .post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(createUserDTO))
                    )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").doesNotExist())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message", Matchers.containsString("Role not valid")));

            Optional<User> userSaved = userRepository.findByEmail(createUserDTO.getEmail());
            assertThat(userSaved).isEmpty();
        }
    }

    @Nested
    class FindAll {

        User user1;
        User user2;

        @BeforeEach
        void setup(){
            UserRole userRole1 = new UserRole(null, Roles.CUSTOMER.name(), null, null);
            user1 =new User(null,"User1","user1@gmail.com","encrypted_password",null,null, List.of(userRole1));
            userRole1.setUser(user1);
            UserRole userRole2 = new UserRole(null, Roles.OWNER.name(), null, null);
            user2 =new User(null,"User2","user2@gmail.com","encrypted_password",null,null, List.of(userRole2));
            userRole2.setUser(user2);

            userRepository.saveAll(List.of(user1,user2));
        }

        @Test
        void findAllUsers() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders
                    .get("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
            )
                    .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(2));
        }

        @Test
        void findAllUsersWithPagination() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders
                    .get("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                            .queryParam("page","1")
                            .queryParam("limit","1")
            )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(1))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].email").value(user1.getEmail()));
        }

        @Test
        void findAllUsersWithTermToSearch() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders
                    .get("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                            .queryParam("q","user2")
            )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(1))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].email").value(user2.getEmail()));
        }
    }

    @Nested
    class FindById {

        User user1;
        User user2;

        @BeforeEach
        void setup(){
            UserRole userRole1 = new UserRole(null, Roles.CUSTOMER.name(), null, null);
            user1 =new User(null,"User1","user1@gmail.com","encrypted_password",null,null, List.of(userRole1));
            userRole1.setUser(user1);
            UserRole userRole2 = new UserRole(null, Roles.OWNER.name(), null, null);
            user2 =new User(null,"User2","user2@gmail.com","encrypted_password",null,null, List.of(userRole2));
            userRole2.setUser(user2);

            userRepository.saveAll(List.of(user1,user2));
        }

        @Test
        void findById() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders
                    .get("/api/users/"+ user1.getId())
                    .contentType(MediaType.APPLICATION_JSON)
            )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(user1.getId().toString()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(user1.getEmail()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.roles.size()").value(1));
        }

        @Test
        void errorWhenNotExistUserById() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders
                    .get("/api/users/697af010-4e42-454c-b4fc-f258166a49aa")
                    .contentType(MediaType.APPLICATION_JSON)
            )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.NOT_FOUND.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message", Matchers.containsString("Not found user with id")));
        }
    }

    @Nested
    class UpdateUser {

        User user1;
        User user2;

        @BeforeEach
        void setup(){
            UserRole userRole1 = new UserRole(null, Roles.CUSTOMER.name(), null, null);
            user1 =new User(null,"User1","user1@gmail.com","encrypted_password",null,null, List.of(userRole1));
            userRole1.setUser(user1);
            UserRole userRole2 = new UserRole(null, Roles.OWNER.name(), null, null);
            user2 =new User(null,"User2","user2@gmail.com","encrypted_password",null,null, List.of(userRole2));
            userRole2.setUser(user2);

            userRepository.saveAll(List.of(user1,user2));
        }

        @Test
        void updateUserEmail() throws Exception {
            UpdateUserDTO updateUserDTO = new UpdateUserDTO(null, "nuevo_Email@gmail.com", null);

            mockMvc.perform(MockMvcRequestBuilders
                    .patch("/api/users/" + user1.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(updateUserDTO))
                    .header("X-UserId", user1.getId().toString())
            )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(user1.getId().toString()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(updateUserDTO.getEmail()));

            Optional<User> userUpdated = userRepository.findById(user1.getId());
            assertThat(userUpdated.orElseThrow().getEmail()).isEqualToIgnoringCase(updateUserDTO.getEmail());
        }

        @Test
        void updateUserPassword() throws Exception {
            UpdateUserDTO updateUserDTO = new UpdateUserDTO(null, null, "Abcde12345!");

            mockMvc.perform(MockMvcRequestBuilders
                    .patch("/api/users/" + user2.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(updateUserDTO))
                    .header("X-UserId", user2.getId().toString())
            )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(user2.getId().toString()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(user2.getEmail()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.password").doesNotExist());

            Optional<User> userUpdated = userRepository.findById(user2.getId());
            assertThat(userUpdated.orElseThrow().getPassword()).isNotEqualTo(updateUserDTO.getPassword());
        }
    }

    @Nested
    class Delete {

        User user1;
        User user2;

        @BeforeEach
        void setup(){
            UserRole userRole1 = new UserRole(null, Roles.CUSTOMER.name(), null, null);
            user1 =new User(null,"User1","user1@gmail.com","encrypted_password",null,null, List.of(userRole1));
            userRole1.setUser(user1);
            UserRole userRole2 = new UserRole(null, Roles.OWNER.name(), null, null);
            user2 =new User(null,"User2","user2@gmail.com","encrypted_password",null,null, List.of(userRole2));
            userRole2.setUser(user2);

            userRepository.saveAll(List.of(user1,user2));
        }

        @Test
        void deleteUser() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders
                    .delete("/api/users/" + user1.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-UserId", user1.getId().toString())
            )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.ok").value(true));

            Optional<User> userDeleted = userRepository.findById(user1.getId());
            assertThat(userDeleted).isEmpty();
        }

        @Test
        void errorWhenNotExistUserWithId() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders
                    .delete("/api/users/24e1aa5a-952a-4cb3-ba55-ea177081c756")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-UserId", "24e1aa5a-952a-4cb3-ba55-ea177081c756")
            )
                    .andExpect(MockMvcResultMatchers.status().is(HttpStatus.NOT_FOUND.value()))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message", Matchers.containsString("Not found user with id")));

            Optional<User> userNotDeleted = userRepository.findById(user1.getId());
            assertThat(userNotDeleted).isPresent();
        }
    }
}
