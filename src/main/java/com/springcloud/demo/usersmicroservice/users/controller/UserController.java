package com.springcloud.demo.usersmicroservice.users.controller;

import com.springcloud.demo.usersmicroservice.dto.SimpleResponseDTO;
import com.springcloud.demo.usersmicroservice.exceptions.ForbiddenException;
import com.springcloud.demo.usersmicroservice.exceptions.dto.ErrorResponseDTO;
import com.springcloud.demo.usersmicroservice.users.dto.*;
import com.springcloud.demo.usersmicroservice.users.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor

public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(security = {})
    @ApiResponses({
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    UserResponseDTO create(@Valid @RequestBody CreateUserDTO createUserDTO) {
        return userService.create(createUserDTO);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses({
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    List<UserResponseDTO> findAll(@Valid @ModelAttribute UserFiltersDTO userFiltersDTO, HttpServletRequest request){
        String traceId = request.getHeader("X-Amzn-Trace-Id");
        System.out.println("X-Amzn-Trace-Id: (Controller)" + traceId);
        return userService.findAll(userFiltersDTO);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses({
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    UserResponseDTO findById(@PathVariable @UUID(message = "uuid not valid") String id){
        return userService.findById(id);
    }

    @GetMapping("/email/{email}")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses({
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    UserAuthResponseDTO findByEmailToAuth(@PathVariable @Email String email){
        return userService.findByEmailToAuth(email);
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses({
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
    })
    UserResponseDTO update(@Valid @RequestBody UpdateUserDTO updateUserDTO, @PathVariable @UUID String id, @RequestHeader("X-UserId") String idUserLogged){
        if(!idUserLogged.equals(id)){
            throw new ForbiddenException("Not have permission to update another user");
        }
        return userService.update(updateUserDTO, id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses({
           @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
    })
    SimpleResponseDTO delete(@PathVariable @UUID String id, @RequestHeader("X-UserId") String idUserLogged){
        if(!idUserLogged.equals(id)){
            throw new ForbiddenException("Not have permission to delete another user");
        }
        return userService.delete(id);
    }
}
