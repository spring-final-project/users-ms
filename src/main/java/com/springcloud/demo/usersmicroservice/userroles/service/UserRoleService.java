package com.springcloud.demo.usersmicroservice.userroles.service;

import com.springcloud.demo.usersmicroservice.dto.SimpleResponseDTO;
import com.springcloud.demo.usersmicroservice.exceptions.ForbiddenException;
import com.springcloud.demo.usersmicroservice.exceptions.NotFoundException;
import com.springcloud.demo.usersmicroservice.userroles.dto.AddRoleDTO;
import com.springcloud.demo.usersmicroservice.userroles.dto.UserRoleResponseDTO;
import com.springcloud.demo.usersmicroservice.userroles.mapper.UserRoleMapper;
import com.springcloud.demo.usersmicroservice.userroles.model.UserRole;
import com.springcloud.demo.usersmicroservice.userroles.repository.UserRoleRepository;
import com.springcloud.demo.usersmicroservice.users.model.User;
import com.springcloud.demo.usersmicroservice.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserRoleService {

    private final UserRoleRepository userRoleRepository;
    private final UserRepository userRepository;

    public UserRoleResponseDTO addUserRole(String userId, AddRoleDTO addRoleDTO) {

        User user = userRepository.findById(UUID.fromString(userId)).orElseThrow(()-> new NotFoundException("Not found user with id: " + userId));

        UserRole userRole = UserRoleMapper.stringToUserRole(addRoleDTO.getRole());

        Optional<UserRole> existRole = userRoleRepository.findByUserAndRole(user, userRole.getRole());

        if(existRole.isPresent()){
            throw new ForbiddenException("User already has same role");
        }

        userRole.setUser(user);

        userRole = userRoleRepository.save(userRole);

        return UserRoleMapper.userRoleToUserRoleResponseDTO(userRole);
    }

    public SimpleResponseDTO deleteUserRole(String id, String idUserLogged) {
        UserRole userRoleToDelete = userRoleRepository
                .findById(UUID.fromString(id))
                .orElseThrow(()-> new NotFoundException("Not found user_role with id: " + id));

        if(!userRoleToDelete.getUser().getId().toString().equals(idUserLogged)){
            throw new ForbiddenException("Not have permission to delete another user");
        }

        userRoleRepository.delete(userRoleToDelete);

        return new SimpleResponseDTO(true);
    }
}
