package com.vidhuratech.jobs.user.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vidhuratech.jobs.common.security.JwtUtil;
import com.vidhuratech.jobs.user.dto.AuthResponse;
import com.vidhuratech.jobs.user.dto.LoginRequest;
import com.vidhuratech.jobs.user.dto.RegisterRequest;
import com.vidhuratech.jobs.user.entity.User;
import com.vidhuratech.jobs.user.enums.UserRole;
import com.vidhuratech.jobs.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final OtpService otpService;

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${app.oauth.google-client-id:}")
    private String googleClientId;

    @Value("${app.oauth.github-client-id:}")
    private String githubClientId;

    @Value("${app.oauth.github-client-secret:}")
    private String githubClientSecret;

    public AuthResponse register(RegisterRequest request) {
        if (userRepo.existsByEmail(request.getEmail())) {
            throw new RuntimeException("EMAIL_ALREADY_EXISTS");
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
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("INVALID_CREDENTIALS");
        }

        if (Boolean.FALSE.equals(user.getActive())) {
            throw new RuntimeException("ACCOUNT_INACTIVE");
        }

        return buildAuthResponse(user, jwtUtil.generateToken(user));
    }

    public AuthResponse loginWithGoogle(String idToken) {
        try {
            if (googleClientId == null || googleClientId.isBlank()) {
                throw new RuntimeException("GOOGLE_CLIENT_ID_NOT_CONFIGURED");
            }

            String url = "https://oauth2.googleapis.com/tokeninfo?id_token="
                    + URLEncoder.encode(idToken, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                throw new RuntimeException("GOOGLE_TOKEN_INVALID");
            }

            JsonNode node = mapper.readTree(response.body());

            String audience = text(node, "aud");
            if (!googleClientId.equals(audience)) {
                throw new RuntimeException("GOOGLE_AUDIENCE_INVALID");
            }

            String email = text(node, "email");
            String name = text(node, "name");

            if (email.isBlank()) {
                throw new RuntimeException("GOOGLE_EMAIL_REQUIRED");
            }

            return loginOrCreateSocialUser(email, name, "");
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("GOOGLE_LOGIN_FAILED");
        }
    }

    public AuthResponse loginWithGithub(String code, String redirectUri) {
        try {
            if (githubClientId == null || githubClientId.isBlank() || githubClientSecret == null || githubClientSecret.isBlank()) {
                throw new RuntimeException("GITHUB_OAUTH_NOT_CONFIGURED");
            }

            String form = "client_id=" + encode(githubClientId)
                    + "&client_secret=" + encode(githubClientSecret)
                    + "&code=" + encode(code)
                    + "&redirect_uri=" + encode(redirectUri);

            HttpRequest tokenRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://github.com/login/oauth/access_token"))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(form))
                    .build();

            HttpResponse<String> tokenResponse = httpClient.send(tokenRequest, HttpResponse.BodyHandlers.ofString());
            JsonNode tokenJson = mapper.readTree(tokenResponse.body());

            String accessToken = text(tokenJson, "access_token");
            if (accessToken.isBlank()) {
                throw new RuntimeException("GITHUB_TOKEN_FAILED");
            }

            JsonNode profile = githubGet("https://api.github.com/user", accessToken);
            JsonNode emails = githubGet("https://api.github.com/user/emails", accessToken);

            String email = "";
            if (emails.isArray()) {
                for (JsonNode item : emails) {
                    if (item.path("primary").asBoolean(false) && item.path("verified").asBoolean(false)) {
                        email = text(item, "email");
                        break;
                    }
                }
            }

            if (email.isBlank()) {
                String id = text(profile, "id");
                email = "github-" + id + "@github.vidhuratech.local";
            }

            String name = text(profile, "name");
            if (name.isBlank()) {
                name = text(profile, "login");
            }

            return loginOrCreateSocialUser(email, name, "");
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("GITHUB_LOGIN_FAILED");
        }
    }

    public AuthResponse loginWithPhoneOtp(String phone, String otp) {
        String normalizedPhone = normalizePhone(phone);

        otpService.verifyPhoneOtpOrThrow(normalizedPhone, otp);

        User user = userRepo.findByPhone(normalizedPhone)
                .orElseGet(() -> createPhoneUser(normalizedPhone));

        if (Boolean.FALSE.equals(user.getActive())) {
            throw new RuntimeException("ACCOUNT_INACTIVE");
        }

        return buildAuthResponse(user, jwtUtil.generateToken(user));
    }

    private AuthResponse loginOrCreateSocialUser(String email, String name, String phone) {
        String normalizedEmail = email.trim().toLowerCase();

        User user = userRepo.findByEmail(normalizedEmail)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setName(name == null || name.isBlank() ? "Student" : name.trim());
                    newUser.setEmail(normalizedEmail);
                    newUser.setPhone(phone == null ? "" : phone.trim());
                    newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                    newUser.setRole(UserRole.STUDENT);
                    newUser.setActive(true);
                    newUser.setFirstLogin(false);
                    newUser.setCreatedAt(LocalDateTime.now());
                    return userRepo.save(newUser);
                });

        if (Boolean.FALSE.equals(user.getActive())) {
            throw new RuntimeException("ACCOUNT_INACTIVE");
        }

        return buildAuthResponse(user, jwtUtil.generateToken(user));
    }

    private User createPhoneUser(String phone) {
        User user = new User();
        user.setName("Student " + phone.substring(phone.length() - 4));
        user.setEmail("phone-" + phone + "@phone.vidhuratech.local");
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setRole(UserRole.STUDENT);
        user.setActive(true);
        user.setFirstLogin(false);
        user.setCreatedAt(LocalDateTime.now());
        return userRepo.save(user);
    }

    private JsonNode githubGet(String url, String token) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/vnd.github+json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return mapper.readTree(response.body());
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

    private String text(JsonNode node, String field) {
        return node != null && node.hasNonNull(field) ? node.get(field).asText("").trim() : "";
    }

    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private String normalizePhone(String phone) {
        if (phone == null) {
            return "";
        }

        String digits = phone.replaceAll("\\D", "");

        if (digits.length() == 12 && digits.startsWith("91")) {
            digits = digits.substring(2);
        }

        if (digits.length() != 10) {
            throw new RuntimeException("INVALID_PHONE");
        }

        return digits;
    }
}