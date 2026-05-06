package com.asset.smartgrampanchayatapi.web.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.asset.smartgrampanchayatapi.district.service.user.UserService;
import com.asset.smartgrampanchayatapi.web.dto.UserDto;
import com.asset.smartgrampanchayatapi.web.dto.UserLoginRequest;
import com.asset.smartgrampanchayatapi.web.dto.UserLoginResponse;
import com.asset.smartgrampanchayatapi.web.filter.TenantCodeHeaderFilter;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "District shard user APIs (requires " + TenantCodeHeaderFilter.HEADER_TENANT_CODE + ")")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    @Operation(summary = "Login using mobile/email and password")
    @Parameter(
            name = TenantCodeHeaderFilter.HEADER_TENANT_CODE,
            in = ParameterIn.HEADER,
            required = true,
            example = "GP001"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Login successful",
            content = @Content(schema = @Schema(implementation = UserLoginResponse.class))
    )
    @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content)
    @ApiResponse(responseCode = "403", description = "User inactive", content = @Content)
    @ApiResponse(responseCode = "503", description = "District database unavailable", content = @Content)
    public ResponseEntity<UserLoginResponse> login(@Valid @RequestBody UserLoginRequest body) {
        return userService
                .login(body.identifier(), body.password())
                .map(user -> ResponseEntity.ok(new UserLoginResponse(UserDto.fromEntity(user), "Login successful")))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials."));
    }

    @GetMapping
    @Operation(summary = "List all users for this tenant")
    @Parameter(
            name = TenantCodeHeaderFilter.HEADER_TENANT_CODE,
            in = ParameterIn.HEADER,
            required = true,
            example = "GP001"
    )
    @ApiResponse(
            responseCode = "200",
            description = "List users",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserDto.class)))
    )
    @ApiResponse(responseCode = "503", description = "District database unavailable", content = @Content)
    public ResponseEntity<List<UserDto>> listUsers() {
        List<UserDto> users = userService.listUsers().stream().map(UserDto::fromEntity).toList();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/lookup")
    @Operation(summary = "Get one user by mobile or email")
    @Parameter(
            name = TenantCodeHeaderFilter.HEADER_TENANT_CODE,
            in = ParameterIn.HEADER,
            required = true,
            example = "GP001"
    )
    @ApiResponse(
            responseCode = "200",
            description = "User found",
            content = @Content(schema = @Schema(implementation = UserDto.class))
    )
    @ApiResponse(responseCode = "400", description = "Need exactly one of mobile,email", content = @Content)
    @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    @ApiResponse(responseCode = "503", description = "District database unavailable", content = @Content)
    public ResponseEntity<UserDto> getByMobileOrEmail(
            @RequestParam(value = "mobile", required = false) String mobile,
            @RequestParam(value = "email", required = false) String email
    ) {
        boolean hasMobile = mobile != null && !mobile.isBlank();
        boolean hasEmail = email != null && !email.isBlank();
        if (hasMobile == hasEmail) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Provide exactly one of query parameters 'mobile' or 'email'."
            );
        }
        return userService.findByMobileOrEmail(mobile, email)
                .map(UserDto::fromEntity)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));
    }
}
