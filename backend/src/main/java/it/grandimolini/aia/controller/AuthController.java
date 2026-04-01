package it.grandimolini.aia.controller;

import it.grandimolini.aia.dto.*;
import it.grandimolini.aia.exception.ResourceNotFoundException;
import it.grandimolini.aia.security.JwtTokenProvider;
import it.grandimolini.aia.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider tokenProvider;

    /** Endpoint di liveness usato dall'healthcheck Docker/Kubernetes — nessuna auth richiesta. */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/register")
    public ResponseEntity<UserVM> register(@Valid @RequestBody CreateUserRequest request) {
        UserVM vm = userService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(vm);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = tokenProvider.generateAccessToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);

        UserVM vm = userService.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", request.getUsername()));

        AuthResponse response = new AuthResponse(
                accessToken,
                refreshToken,
                vm.getId(),
                vm.getUsername(),
                vm.getRuolo().name()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new ResourceNotFoundException("Invalid or expired refresh token");
        }

        String username = tokenProvider.getUsernameFromToken(refreshToken);
        UserVM vm = userService.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String newAccessToken = tokenProvider.generateAccessToken(authentication);

        AuthResponse response = new AuthResponse(
                newAccessToken,
                refreshToken,
                vm.getId(),
                vm.getUsername(),
                vm.getRuolo().name()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<CurrentUserDTO> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        CurrentUserDTO currentUser = userService.findCurrentUserDTO(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        return ResponseEntity.ok(currentUser);
    }

    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        UserVM vm = userService.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        userService.changePassword(vm.getId(), request.getOldPassword(), request.getNewPassword());

        return ResponseEntity.ok("Password changed successfully");
    }
}
