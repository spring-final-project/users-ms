package com.springcloud.demo.usersmicroservice.users.service;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.springcloud.demo.usersmicroservice.dto.SimpleResponseDTO;
import com.springcloud.demo.usersmicroservice.userroles.mapper.UserRoleMapper;
import com.springcloud.demo.usersmicroservice.userroles.model.Roles;
import com.springcloud.demo.usersmicroservice.users.dto.*;
import com.springcloud.demo.usersmicroservice.exceptions.ForbiddenException;
import com.springcloud.demo.usersmicroservice.exceptions.NotFoundException;
import com.springcloud.demo.usersmicroservice.users.mapper.UserMapper;
import com.springcloud.demo.usersmicroservice.users.model.User;
import com.springcloud.demo.usersmicroservice.userroles.model.UserRole;
import com.springcloud.demo.usersmicroservice.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public UserResponseDTO create(CreateUserDTO createUserDTO) {

        Optional<User> userExist = userRepository.findByEmail(createUserDTO.getEmail());

        userExist.ifPresent(user -> {
            String roleSelected = Optional.ofNullable(createUserDTO.getRole()).orElse(Roles.CUSTOMER.name());
            boolean userAlreadyHasSameRole = user.getRoles().stream().anyMatch(role -> role.getRole().equals(roleSelected));
            if(userAlreadyHasSameRole){
                throw new ForbiddenException("Already exist user with same email.");
            } else {
                throw new ForbiddenException("Already exist user with same email. To add new role try update the user");
            }
        });

        User user = UserMapper.createUserDtoToUser(createUserDTO);

        user.setPassword(passwordEncoder.encode(createUserDTO.getPassword()));

        UserRole userRole = UserRoleMapper.stringToUserRole(createUserDTO.getRole());
        userRole.setUser(user);
        user.setRoles(List.of(userRole));

        user = userRepository.save(user);

        return UserMapper.userToUserResponseDto(user);
    }

    public List<UserResponseDTO> findAll(UserFiltersDTO userFiltersDTO) {
        int page = Optional.ofNullable(userFiltersDTO.getPage()).orElse(1) - 1;
        int limit = Optional.ofNullable(userFiltersDTO.getLimit()).orElse(20);
        Pageable pageable = PageRequest.of(page,limit);

        List<User> users;

        if(userFiltersDTO.getQ() != null){
            users = userRepository
                    .findBySearchTerm(userFiltersDTO.getQ(), pageable)
                    .getContent();
        } else {
            users = userRepository.findAll(pageable).getContent();
        }

        return users
                .stream()
                .map(UserMapper::userToUserResponseDto)
                .toList();
    }

    public UserResponseDTO findById(String id) {
        User user = userRepository.findById(UUID.fromString(id)).orElseThrow(()-> new NotFoundException("Not found user with id: " + id));

        return UserMapper.userToUserResponseDto(user);
    }

    public UserResponseDTO update(UpdateUserDTO updateUserDTO, String id) {
        User user = userRepository.findById(UUID.fromString(id)).orElseThrow(()-> new NotFoundException("Not found user with id: " + id));

        User updatedUser = UserMapper.userToUserUpdated(user, updateUserDTO);

        if(updateUserDTO.getPassword() != null){
            updatedUser.setPassword(passwordEncoder.encode(updateUserDTO.getPassword()));
        }

        updatedUser = userRepository.save(updatedUser);

        return UserMapper.userToUserResponseDto(updatedUser);
    }

    public SimpleResponseDTO delete(String id) {
        User user = userRepository.findById(UUID.fromString(id)).orElseThrow(()-> new NotFoundException("Not found user with id: " + id));

        userRepository.delete(user);

        return new SimpleResponseDTO(true);
    }

    public UserAuthResponseDTO findByEmailToAuth(String email) {
        User user = userRepository
                .findByEmail(email)
                .orElseThrow(()-> new NotFoundException("Not found user with email: " + email));

        return UserMapper.userToUserAuthResponseDto(user);
    }
}
