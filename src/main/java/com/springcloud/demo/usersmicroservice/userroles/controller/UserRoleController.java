package com.springcloud.demo.usersmicroservice.userroles.controller;

import com.springcloud.demo.usersmicroservice.dto.SimpleResponseDTO;
import com.springcloud.demo.usersmicroservice.exceptions.ForbiddenException;
import com.springcloud.demo.usersmicroservice.exceptions.dto.ErrorResponseDTO;
import com.springcloud.demo.usersmicroservice.userroles.dto.AddRoleDTO;
import com.springcloud.demo.usersmicroservice.userroles.dto.UserRoleResponseDTO;
import com.springcloud.demo.usersmicroservice.userroles.service.UserRoleService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserRoleController {

    private final UserRoleService userRoleService;

    @PostMapping("/{userId}/roles")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses({
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
    })
    UserRoleResponseDTO addUserRole(@Valid @RequestBody AddRoleDTO addRoleDTO, @PathVariable @UUID String userId, @RequestHeader("X-UserId") String idUserLogged) {
        if(!idUserLogged.equals(userId)){
            throw new ForbiddenException("Not have permission to add role to another user");
        }
        return userRoleService.addUserRole(userId, addRoleDTO);
    }

    @DeleteMapping("/roles/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses({
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
    })
    public SimpleResponseDTO deleteUserRole(@PathVariable @UUID String id, @RequestHeader("X-UserId") String idUserLogged){
        return userRoleService.deleteUserRole(id, idUserLogged);
    }
}
