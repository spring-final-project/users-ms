package com.springcloud.demo.usersmicroservice.userroles.service;

import com.springcloud.demo.usersmicroservice.dto.SimpleResponseDTO;
import com.springcloud.demo.usersmicroservice.exceptions.ForbiddenException;
import com.springcloud.demo.usersmicroservice.exceptions.NotFoundException;
import com.springcloud.demo.usersmicroservice.userroles.dto.AddRoleDTO;
import com.springcloud.demo.usersmicroservice.userroles.dto.UserRoleResponseDTO;
import com.springcloud.demo.usersmicroservice.userroles.model.Roles;
import com.springcloud.demo.usersmicroservice.userroles.model.UserRole;
import com.springcloud.demo.usersmicroservice.userroles.repository.UserRoleRepository;
import com.springcloud.demo.usersmicroservice.users.model.User;
import com.springcloud.demo.usersmicroservice.users.repository.UserRepository;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.BDDMockito.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class UserRoleServiceTest {

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserRoleService userRoleService;

    @Nested
    class AddUserRole {

        @Test
        void addUserRole(){
            AddRoleDTO addRoleDTO = new AddRoleDTO(Roles.OWNER.name());
            User userFound = new User(UUID.randomUUID(), "user1","user1@gmail.com","encrypted_password", LocalDateTime.now(), LocalDateTime.now(), List.of());
            UserRole userRoleCreated = new UserRole(UUID.randomUUID(), Roles.OWNER.name(), LocalDateTime.now(), userFound);

            given(userRepository.findById(any(UUID.class))).willReturn(Optional.of(userFound));
            given(userRoleRepository.findByUserAndRole(any(User.class),anyString())).willReturn(Optional.empty());
            given(userRoleRepository.save(any(UserRole.class))).willReturn(userRoleCreated);

            UserRoleResponseDTO response = userRoleService.addUserRole(userFound.getId().toString(), addRoleDTO);

            verify(userRoleRepository).save(argThat(args -> args.getUser().getId().equals(userFound.getId())));
            assertThat(response.getId()).isEqualTo(userRoleCreated.getId());
            assertThat(response.getRole()).isEqualTo(addRoleDTO.getRole());
        }

        @Test
        void errorWhenUserNotExist(){
            AddRoleDTO addRoleDTO = new AddRoleDTO(Roles.OWNER.name());

            given(userRepository.findById(any(UUID.class))).willReturn(Optional.empty());

            NotFoundException e = Assertions.assertThrows(NotFoundException.class, ()->{
                userRoleService.addUserRole(UUID.randomUUID().toString(), addRoleDTO);
            });

            verify(userRoleRepository, never()).save(any());
            assertThat(e.getMessage()).contains("Not found user");
        }

        @Test
        void errorWhenUserAlreadyHasSameRole(){
            AddRoleDTO addRoleDTO = new AddRoleDTO(Roles.OWNER.name());
            User userFound = new User(UUID.randomUUID(), "user1","user1@gmail.com","encrypted_password", LocalDateTime.now(), LocalDateTime.now(), List.of());
            UserRole userRoleFound = new UserRole(UUID.randomUUID(), Roles.OWNER.name(), LocalDateTime.now(), userFound);

            given(userRepository.findById(any(UUID.class))).willReturn(Optional.of(userFound));
            given(userRoleRepository.findByUserAndRole(any(User.class), anyString())).willReturn(Optional.of(userRoleFound));

            ForbiddenException e = Assertions.assertThrows(ForbiddenException.class, ()->{
                userRoleService.addUserRole(UUID.randomUUID().toString(), addRoleDTO);
            });

            verify(userRoleRepository, never()).save(any());
            verify(userRoleRepository).findByUserAndRole(argThat(user -> user.getId().equals(userFound.getId())), argThat(role -> role.equals(addRoleDTO.getRole())));
            assertThat(e.getMessage()).contains("User already has same role");
        }
    }

    @Nested
    class DeleteUserRole {

        @Test
        void deleteUserRole(){
            User userOfRole = User.builder().id(UUID.randomUUID()).name("user1").build();
            UserRole userRoleFound = new UserRole(UUID.randomUUID(), Roles.OWNER.name(), LocalDateTime.now(), userOfRole);

            given(userRoleRepository.findById(any(UUID.class))).willReturn(Optional.of(userRoleFound));
            willDoNothing().given(userRoleRepository).delete(any(UserRole.class));

            SimpleResponseDTO response =  userRoleService.deleteUserRole(userRoleFound.getId().toString(), userOfRole.getId().toString());

            verify(userRoleRepository).findById(argThat(uuid -> uuid.equals(userRoleFound.getId())));
            verify(userRoleRepository).delete(argThat(userRole -> userRole.getId().equals(userRoleFound.getId())));
            assertThat(response.isOk()).isTrue();
        }

        @Test
        void errorWhenNotFoundUserRoleById(){
            UUID idToDelete = UUID.randomUUID();

            given(userRoleRepository.findById(any(UUID.class))).willReturn(Optional.empty());

            NotFoundException e = Assertions.assertThrows(NotFoundException.class, ()->{
                userRoleService.deleteUserRole(idToDelete.toString(), UUID.randomUUID().toString());
            });

            verify(userRoleRepository).findById(argThat(uuid -> uuid.equals(idToDelete)));
            verify(userRoleRepository,never()).delete(any());
            assertThat(e.getMessage()).contains("Not found user_role with id");
        }
    }

}