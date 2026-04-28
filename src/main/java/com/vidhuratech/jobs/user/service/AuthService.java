package com.vidhuratech.jobs.user.service;

import com.vidhuratech.jobs.common.security.JwtUtil;
import com.vidhuratech.jobs.user.dto.AuthResponse;
import com.vidhuratech.jobs.user.dto.LoginRequest;
import com.vidhuratech.jobs.user.dto.RegisterRequest;
import com.vidhuratech.jobs.user.entity.User;
import com.vidhuratech.jobs.user.enums.UserRole;
import com.vidhuratech.jobs.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        if (userRepo.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.STUDENT);
        user.setActive(true);
        user.setFirstLogin(false);

        userRepo.save(user);

        return buildAuthResponse(user, jwtUtil.generateToken(user));
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        if (Boolean.FALSE.equals(user.getActive())) {
            throw new RuntimeException("Account is inactive");
        }

        return buildAuthResponse(user, jwtUtil.generateToken(user));
    }

    private AuthResponse buildAuthResponse(User user, String token) {
        return AuthResponse.builder()
                .token(token)
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .active(user.getActive())
                .firstLogin(Boolean.TRUE.equals(user.getFirstLogin()))
                .build();
    }
}
