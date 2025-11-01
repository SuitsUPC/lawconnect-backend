package com.qu3dena.lawconnect.backend.iam.interfaces.rest;

import com.qu3dena.lawconnect.backend.iam.domain.services.UserCommandService;
import com.qu3dena.lawconnect.backend.iam.interfaces.rest.resources.AuthenticatedUserResource;
import com.qu3dena.lawconnect.backend.iam.interfaces.rest.resources.SignInResource;
import com.qu3dena.lawconnect.backend.iam.interfaces.rest.resources.SignUpResource;
import com.qu3dena.lawconnect.backend.iam.interfaces.rest.resources.UserResource;
import com.qu3dena.lawconnect.backend.iam.interfaces.rest.transform.AuthenticatedUserResourceFromEntityAssembler;
import com.qu3dena.lawconnect.backend.iam.interfaces.rest.transform.SignInCommandFromResourceAssembler;
import com.qu3dena.lawconnect.backend.iam.interfaces.rest.transform.SignUpCommandFromResourceAssembler;
import com.qu3dena.lawconnect.backend.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for handling authentication-related endpoints such as user sign-up and sign-in.
 * <p>
 * Provides endpoints for registering new users and authenticating existing users.
 * </p>
 *
 * @author LawConnect Team
 * @since 1.0.0
 */
@RestController
@RequestMapping(value = "/api/v1/authentication", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Authentication", description = "Available Authentication Endpoints")
public class AuthenticationController {

    private final UserCommandService userCommandService;

    /**
     * Constructs an {@code AuthenticationController} with the specified {@link UserCommandService}.
     *
     * @param userCommandService the service handling user commands
     */
    public AuthenticationController(UserCommandService userCommandService) {
        this.userCommandService = userCommandService;
    }

    /**
     * Registers a new user with the provided sign-up information.
     *
     * @param resource the sign-up resource containing username, password, and role
     * @return a {@link ResponseEntity} containing the created {@link UserResource} and HTTP status
     */
    @PostMapping("/sign-up")
    @Operation(summary = "Sign up a new user", description = "Sign up a new user with the provided username, password, and roles.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully."),
            @ApiResponse(responseCode = "400", description = "Bad request.")
    })
    public ResponseEntity<UserResource> signUp(@RequestBody SignUpResource resource) {
        var signUpCommand = SignUpCommandFromResourceAssembler.toCommandFromResource(resource);
        var user = userCommandService.handle(signUpCommand);

        if (user.isEmpty())
            return ResponseEntity.badRequest().build();

        var userEntity = user.get();
        var userResource = UserResourceFromEntityAssembler.toResourceFromEntity(userEntity);

        return new ResponseEntity<>(userResource, HttpStatus.CREATED);
    }

    /**
     * Authenticates a user with the provided sign-in credentials.
     *
     * @param resource the sign-in resource containing username and password
     * @return a {@link ResponseEntity} containing the authenticated user resource and HTTP status
     */
    @PostMapping("/sign-in")
    @Operation(summary = "Sign in a user", description = "Sign in a user with the provided username and password.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User signed in successfully."),
            @ApiResponse(responseCode = "404", description = "User not found.")
    })
    public ResponseEntity<AuthenticatedUserResource> signIn(@RequestBody SignInResource resource) {
        var signInCommand = SignInCommandFromResourceAssembler.toCommandFromResource(resource);
        var authenticatedUserResult = userCommandService.handle(signInCommand);

        if (authenticatedUserResult.isEmpty())
            return ResponseEntity.notFound().build();

        var authenticatedUser = authenticatedUserResult.get();
        var authenticatedUserResource = AuthenticatedUserResourceFromEntityAssembler
                .toResourceFromEntity(authenticatedUser.left, authenticatedUser.right);

        return ResponseEntity.ok(authenticatedUserResource);
    }
}
