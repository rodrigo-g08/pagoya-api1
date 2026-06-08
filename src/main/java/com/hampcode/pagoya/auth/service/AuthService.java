package com.hampcode.pagoya.auth.service;

import com.hampcode.pagoya.auth.dto.AuthResponse;
import com.hampcode.pagoya.auth.dto.LoginRequest;
import com.hampcode.pagoya.auth.model.RefreshToken;
import com.hampcode.pagoya.auth.model.User;
import com.hampcode.pagoya.auth.repository.UserRepository;
import com.hampcode.pagoya.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Value("${pagoya.security.jwt.expiration-ms}")
    private long accessExpirationMs;

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
        User user = userRepository.findByEmail(request.email()).orElseThrow();
        String role = user.getRole().getName();

        String accessToken = jwtService.generateToken(userDetails, role);
        RefreshToken refresh = refreshTokenService.create(user);

        return new AuthResponse(
            accessToken, refresh.getToken(),
            user.getEmail(), role, accessExpirationMs);
    }

    @Override
    public AuthResponse refresh(String refreshToken) {
        RefreshToken current = refreshTokenService.validate(refreshToken);
        RefreshToken rotated = refreshTokenService.rotate(current);

        User user = rotated.getUser();
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String role = user.getRole().getName();

        String newAccessToken = jwtService.generateToken(userDetails, role);
        return new AuthResponse(
            newAccessToken, rotated.getToken(),
            user.getEmail(), role, accessExpirationMs);
    }

    @Override
    public void logout(String refreshToken) {
        refreshTokenService.revoke(refreshToken);
    }

    @Override
    public void logoutAll(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("usuario no encontrado"));
        refreshTokenService.revokeAllByUserId(user.getId());
    }
}
