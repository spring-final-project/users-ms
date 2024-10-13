package com.springcloud.demo.usersmicroservice.users.service;

import com.springcloud.demo.usersmicroservice.dto.SimpleResponseDTO;
import com.springcloud.demo.usersmicroservice.exceptions.BadRequestException;
import com.springcloud.demo.usersmicroservice.exceptions.ForbiddenException;
import com.springcloud.demo.usersmicroservice.exceptions.NotFoundException;
import com.springcloud.demo.usersmicroservice.userroles.model.Roles;
import com.springcloud.demo.usersmicroservice.userroles.model.UserRole;
import com.springcloud.demo.usersmicroservice.users.dto.CreateUserDTO;
import com.springcloud.demo.usersmicroservice.users.dto.UpdateUserDTO;
import com.springcloud.demo.usersmicroservice.users.dto.UserFiltersDTO;
import com.springcloud.demo.usersmicroservice.users.dto.UserResponseDTO;
import com.springcloud.demo.usersmicroservice.users.model.User;
import com.springcloud.demo.usersmicroservice.users.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserService userService;

    String encryptedPassword = "password-encrypted";

    @Nested
    class Create {

        @Test
        void createUserWithDefaultRole() {
            CreateUserDTO createUserDTO = new CreateUserDTO("Gonza", "gonzalo@gmail.com", "abcd1234", null);
            UserRole userRoleCreated = UserRole.builder().id(UUID.fromString("a399bded-044d-4ffc-b2e7-70cefb21ed25")).role(Roles.CUSTOMER.name()).createdAt(LocalDateTime.now()).build();
            User userSaved = User.builder().id(UUID.fromString("d0dd04ab-a40a-48a1-877e-8833e66342fb")).name(createUserDTO.getName()).email(createUserDTO.getEmail()).password(encryptedPassword).roles(List.of(userRoleCreated)).createdAt(LocalDateTime.now()).lastUpdated(LocalDateTime.now()).build();

            given(userRepository.save(any(User.class))).willReturn(userSaved);
            given(passwordEncoder.encode(anyString())).willReturn(encryptedPassword);

            UserResponseDTO response = userService.create(createUserDTO);

            verify(userRepository).save(argThat(user -> user.getPassword().equals(encryptedPassword)));

            assertThat(response.getId()).isOfAnyClassIn(UUID.class);
            assertThat(response.getCreatedAt()).isInstanceOf(LocalDateTime.class);
            assertThat(response.getEmail()).isEqualTo(createUserDTO.getEmail());
            assertThat(response.getName()).isEqualTo(createUserDTO.getName());

            assertThat(response.getRoles()).hasSize(1);
            assertThat(response.getRoles().getFirst().getRole()).isEqualTo(Roles.CUSTOMER.name());
        }

        @Test
        void createUserWithDefinedRole() {
            CreateUserDTO createUserDTO = new CreateUserDTO("Gonza", "gonzalo@gmail.com", "abcd1234", "OWNER");
            UserRole userRoleCreated = UserRole.builder().id(UUID.fromString("a399bded-044d-4ffc-b2e7-70cefb21ed25")).role(Roles.OWNER.name()).createdAt(LocalDateTime.now()).build();
            User userSaved = User.builder().id(UUID.fromString("d0dd04ab-a40a-48a1-877e-8833e66342fb")).name(createUserDTO.getName()).email(createUserDTO.getEmail()).password(encryptedPassword).roles(List.of(userRoleCreated)).createdAt(LocalDateTime.now()).lastUpdated(LocalDateTime.now()).build();

            given(userRepository.save(any(User.class))).willReturn(userSaved);
            given(passwordEncoder.encode(anyString())).willReturn(encryptedPassword);

            UserResponseDTO response = userService.create(createUserDTO);

            verify(passwordEncoder, times(1)).encode(anyString());
            verify(userRepository).save(argThat(user -> user.getPassword().equals(encryptedPassword)));
            verify(userRepository).save(argThat(user -> user.getRoles().getFirst().getRole().equals(createUserDTO.getRole())));

            assertThat(response.getId()).isOfAnyClassIn(UUID.class);
            assertThat(response.getCreatedAt()).isInstanceOf(LocalDateTime.class);
            assertThat(response.getEmail()).isEqualTo(createUserDTO.getEmail());
            assertThat(response.getName()).isEqualTo(createUserDTO.getName());

            assertThat(response.getRoles()).hasSize(1);
            assertThat(response.getRoles().getFirst().getRole()).isEqualTo(Roles.OWNER.name());
        }

        @Test
        void errorWhenNotExistRole() {
            CreateUserDTO createUserDTO = new CreateUserDTO("Gonza", "gonzalo@gmail.com", "abcd1234", "OTHER_ROLE");
            given(passwordEncoder.encode(anyString())).willReturn(encryptedPassword);

            BadRequestException e = Assertions.assertThrows(BadRequestException.class, ()-> {
                userService.create(createUserDTO);
            });

            verify(passwordEncoder, times(1)).encode(anyString());
            verify(userRepository,never()).save(any(User.class));
            assertThat(e.getMessage()).contains("Role not valid");
        }

        @Test
        void errorWhenAlreadyExistUserWithSameEmailAndSameRole() {
            CreateUserDTO createUserDTO = new CreateUserDTO("Gonza", "gonzalo@gmail.com", "abcd1234", null);
            UserRole userRoleCreated = UserRole.builder().id(UUID.fromString("a399bded-044d-4ffc-b2e7-70cefb21ed25")).role(Roles.CUSTOMER.name()).createdAt(LocalDateTime.now()).build();
            User userSaved = User.builder().id(UUID.fromString("d0dd04ab-a40a-48a1-877e-8833e66342fb")).name(createUserDTO.getName()).email(createUserDTO.getEmail()).password(encryptedPassword).roles(List.of(userRoleCreated)).createdAt(LocalDateTime.now()).lastUpdated(LocalDateTime.now()).build();

            given(userRepository.findByEmail(any())).willReturn(Optional.ofNullable(userSaved));

            ForbiddenException e = Assertions.assertThrows(ForbiddenException.class, () -> {
                userService.create(createUserDTO);
            });

            assertThat(e.getMessage()).contains("Already exist user with same email.");
        }

        @Test
        void errorWhenAlreadyExistUserWithSameEmailButOtherRole() {
            CreateUserDTO createUserDTO = new CreateUserDTO("Gonza", "gonzalo@gmail.com", "abcd1234", "OWNER");
            UserRole userRoleCreated = UserRole.builder().id(UUID.fromString("a399bded-044d-4ffc-b2e7-70cefb21ed25")).role(Roles.CUSTOMER.name()).createdAt(LocalDateTime.now()).build();
            User userSaved = User.builder().id(UUID.fromString("d0dd04ab-a40a-48a1-877e-8833e66342fb")).name(createUserDTO.getName()).email(createUserDTO.getEmail()).password(encryptedPassword).roles(List.of(userRoleCreated)).createdAt(LocalDateTime.now()).lastUpdated(LocalDateTime.now()).build();

            given(userRepository.findByEmail(any())).willReturn(Optional.ofNullable(userSaved));

            ForbiddenException e = Assertions.assertThrows(ForbiddenException.class, () -> {
                userService.create(createUserDTO);
            });

            assertThat(e.getMessage()).contains("Already exist user with same email. To add new role try update the user");
        }

    }

    @Nested
    class FindAll {

        UserRole role;
        User user;

        @BeforeEach
        void setup() {
            role = UserRole.builder().id(UUID.fromString("a399bded-044d-4ffc-b2e7-70cefb21ed25")).role(Roles.CUSTOMER.name()).createdAt(LocalDateTime.now()).build();
            user = User.builder().id(UUID.fromString("d0dd04ab-a40a-48a1-877e-8833e66342fb")).name("User1").email("user1@gmail.com").password(encryptedPassword).roles(List.of(role)).createdAt(LocalDateTime.now()).lastUpdated(LocalDateTime.now()).build();
        }

        @Test
        void findAll() {
            given(userRepository.findAll(any(Pageable.class))).willReturn(new PageImpl<User>(List.of(user)));

            List<UserResponseDTO> response = userService.findAll(new UserFiltersDTO(null, null, null));

            verify(userRepository, times(1))
                    .findAll(argThat((Pageable pageable) -> pageable.getPageNumber() == 0 && pageable.getPageSize() == 20));
            verify(userRepository, never())
                    .findBySearchTerm(any(), any());
            assertThat(response.size()).isEqualTo(1);
        }

        @Test
        void findAllWithCustomPagination() {
            UserFiltersDTO userFiltersDTO = new UserFiltersDTO(2, 15, null);
            given(userRepository.findAll(any(Pageable.class))).willReturn(new PageImpl<User>(List.of(user)));

            List<UserResponseDTO> response = userService.findAll(userFiltersDTO);

            verify(userRepository, times(1))
                    .findAll(argThat((Pageable pageable) ->
                            pageable.getPageNumber() == userFiltersDTO.getPage() - 1
                                    && pageable.getPageSize() == userFiltersDTO.getLimit())
                    );
            verify(userRepository, never())
                    .findBySearchTerm(any(), any());
            assertThat(response.size()).isEqualTo(1);
        }

        @Test
        void findAllWithCustomPaginationAndTermToSearch() {
            UserFiltersDTO userFiltersDTO = new UserFiltersDTO(2, 15, "user");
            given(userRepository.findBySearchTerm(anyString(), any(Pageable.class))).willReturn(new PageImpl<User>(List.of(user)));

            List<UserResponseDTO> response = userService.findAll(userFiltersDTO);

            verify(userRepository, never()).findAll((Pageable) any());
            verify(userRepository, times(1))
                    .findBySearchTerm(argThat(term ->
                                    term.equals(userFiltersDTO.getQ())),
                            argThat((Pageable pageable) ->
                                    pageable.getPageNumber() == userFiltersDTO.getPage() - 1 &&
                                            pageable.getPageSize() == userFiltersDTO.getLimit())
                    );
            assertThat(response.size()).isEqualTo(1);
        }
    }

    @Nested
    class FindById {
        UserRole role;
        User user;

        @BeforeEach
        void setup() {
            role = UserRole.builder().id(UUID.fromString("a399bded-044d-4ffc-b2e7-70cefb21ed25")).role(Roles.CUSTOMER.name()).createdAt(LocalDateTime.now()).build();
            user = User.builder().id(UUID.fromString("d0dd04ab-a40a-48a1-877e-8833e66342fb")).name("User1").email("user1@gmail.com").password(encryptedPassword).roles(List.of(role)).createdAt(LocalDateTime.now()).lastUpdated(LocalDateTime.now()).build();
        }

        @Test
        void findById() {
            given(userRepository.findById(any(UUID.class))).willReturn(Optional.of(user));

            UserResponseDTO response = userService.findById("d0dd04ab-a40a-48a1-877e-8833e66342fb");

            verify(userRepository, times(1)).findById(argThat(uuid -> uuid.toString().equals("d0dd04ab-a40a-48a1-877e-8833e66342fb")));
            assertThat(response.getId()).isEqualTo(UUID.fromString("d0dd04ab-a40a-48a1-877e-8833e66342fb"));
        }

        @Test
        void errorWhenNotExistUserWithId() {
            given(userRepository.findById(any(UUID.class))).willReturn(Optional.empty());

            NotFoundException e = Assertions.assertThrows(NotFoundException.class, () -> {
                userService.findById("d0dd04ab-a40a-48a1-877e-8833e66342fa");
            });

            verify(userRepository, times(1)).findById(argThat(uuid -> uuid.toString().equals("d0dd04ab-a40a-48a1-877e-8833e66342fa")));
            assertThat(e.getMessage()).contains("Not found user with id");
        }
    }

    @Nested
    class Update {
        UserRole role;
        User user;

        @BeforeEach
        void setup() {
            role = UserRole.builder().id(UUID.fromString("a399bded-044d-4ffc-b2e7-70cefb21ed25")).role(Roles.CUSTOMER.name()).createdAt(LocalDateTime.now()).build();
            user = User.builder().id(UUID.fromString("d0dd04ab-a40a-48a1-877e-8833e66342fb")).name("User1").email("user1@gmail.com").password(encryptedPassword).roles(List.of(role)).createdAt(LocalDateTime.now()).lastUpdated(LocalDateTime.now()).build();
        }

        @Test
        void update() {
            String idUser = user.getId().toString();
            UpdateUserDTO updateUserDTO = new UpdateUserDTO(null, "nuevo_mail@gmail.com", null);
            User updatedUser = User.builder().id(UUID.fromString("d0dd04ab-a40a-48a1-877e-8833e66342fb")).name("User1").email("nuevo_mail@gmail.com").password(encryptedPassword).roles(List.of(role)).createdAt(LocalDateTime.now()).lastUpdated(LocalDateTime.now()).build();

            given(userRepository.findById(any(UUID.class))).willReturn(Optional.of(user));
            given(userRepository.save(any(User.class))).willReturn(updatedUser);

            UserResponseDTO response = userService.update(updateUserDTO, idUser);

            verify(userRepository).findById(argThat(uuid -> uuid.toString().equals(idUser)));
            verify(userRepository).save(argThat(u -> u.getEmail().equals("nuevo_mail@gmail.com")));
            assertThat(response.getEmail()).isEqualTo(updateUserDTO.getEmail());
            assertThat(response.getId()).isEqualTo(UUID.fromString(idUser));
            assertThat(response.getName()).isEqualTo("User1");
        }

        @Test
        void updatePassword() {
            String idUser = user.getId().toString();
            String newPasswordEncrypted = "password_encrypted2";
            UpdateUserDTO updateUserDTO = new UpdateUserDTO(null, null, "nueva_contraseña");
            User updatedUser = User.builder().id(UUID.fromString("d0dd04ab-a40a-48a1-877e-8833e66342fb")).name("User1").email("nuevo_mail@gmail.com").password(newPasswordEncrypted).roles(List.of(role)).createdAt(LocalDateTime.now()).lastUpdated(LocalDateTime.now()).build();

            given(passwordEncoder.encode(anyString())).willReturn(newPasswordEncrypted);
            given(userRepository.findById(any(UUID.class))).willReturn(Optional.of(user));
            given(userRepository.save(any(User.class))).willReturn(updatedUser);

            UserResponseDTO response = userService.update(updateUserDTO, idUser);

            verify(userRepository).findById(argThat(uuid -> uuid.toString().equals(idUser)));
            verify(userRepository).save(argThat(u -> u.getPassword().equals(newPasswordEncrypted)));
            assertThat(response.getId()).isEqualTo(UUID.fromString(idUser));
        }

        @Test
        void errorWhenNotExistUserWithId() {
            String idUser = "d0dd04ab-a40a-48a1-877e-8833e66342fa";
            UpdateUserDTO updateUserDTO = new UpdateUserDTO(null, "nuevo_mail@gmail.com", null);

            given(userRepository.findById(any(UUID.class))).willReturn(Optional.empty());

            NotFoundException e = Assertions.assertThrows(NotFoundException.class, () -> {
                UserResponseDTO response = userService.update(updateUserDTO, idUser);
            });

            assertThat(e.getMessage()).contains("Not found user with id");
            verify(userRepository).findById(argThat(uuid -> uuid.toString().equals(idUser)));
            verify(userRepository, never()).save(any(User.class));
        }
    }


    @Nested
    class Delete {
        UserRole role;
        User user;

        @BeforeEach
        void setup() {
            role = UserRole.builder().id(UUID.fromString("a399bded-044d-4ffc-b2e7-70cefb21ed25")).role(Roles.CUSTOMER.name()).createdAt(LocalDateTime.now()).build();
            user = User.builder().id(UUID.fromString("d0dd04ab-a40a-48a1-877e-8833e66342fb")).name("User1").email("user1@gmail.com").password(encryptedPassword).roles(List.of(role)).createdAt(LocalDateTime.now()).lastUpdated(LocalDateTime.now()).build();
        }

        @Test
        void delete() {
            String idUser = user.getId().toString();

            given(userRepository.findById(any(UUID.class))).willReturn(Optional.of(user));
            willDoNothing().given(userRepository).delete(any(User.class));

            SimpleResponseDTO response = userService.delete(idUser);

            verify(userRepository).findById(argThat(id -> id.equals(UUID.fromString(idUser))));
            verify(userRepository).delete(any(User.class));
            verify(userRepository).delete(argThat(user -> user.getId().equals(UUID.fromString(idUser))));
            assertThat(response.isOk()).isTrue();

        }

        @Test
        void errorWhenNotExistUserWithId() {
            String idUser = user.getId().toString();

            given(userRepository.findById(any(UUID.class))).willReturn(Optional.empty());

            NotFoundException e = Assertions.assertThrows(NotFoundException.class, ()-> {
                userService.delete(idUser);
            });

            verify(userRepository).findById(argThat(id -> id.equals(UUID.fromString(idUser))));
            verify(userRepository, never()).delete(any());
            assertThat(e.getMessage()).contains("Not found user with id");

        }
    }
}