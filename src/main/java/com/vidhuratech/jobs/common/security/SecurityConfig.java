package com.vidhuratech.jobs.common.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/set-password",
                                "/api/auth/send-otp",
                                "/api/auth/verify-otp",
                                "/api/auth/register/init",
                                "/api/auth/register/verify",
                                "/api/auth/resend-link",
                                "/api/auth/validate-token",
                                "/api/public/**",
                                "/uploads/**",
                                "/api/public/practice/**",
                                "/api/zoho/**",
                                "/public/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .requestMatchers(
                                "/actuator/health",
                                "/actuator/health/**",
                                "/course-thumbnails/**"
                        ).permitAll()
                        .requestMatchers("/", "/health").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/questions").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/me").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/auth/me").authenticated()

                        .requestMatchers(HttpMethod.GET, "/api/trainer/public-curriculum").permitAll()
                        .requestMatchers("/api/checkout/**").permitAll()
                        .requestMatchers("/api/access/**").permitAll()
                        .requestMatchers("/api/leads/save").permitAll()
                        .requestMatchers("/api/lms/batches/course/*/active").permitAll()
                        .requestMatchers("/course-thumbnails/**", "/api/lms/batches/course/*/upcoming").permitAll()
                        .requestMatchers("/api/notifications/**").authenticated()
                        .requestMatchers("/api/leads/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers("/api/super-admin/**").hasAnyRole("SUPER_ADMIN", "ADMIN")
                        .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers("/api/hr/**").hasRole("HR")
                        .requestMatchers("/api/manager/**").hasRole("MANAGER")
                        .requestMatchers("/api/trainer/**").hasRole("TRAINER")
                        .requestMatchers("/api/mentor/**").hasRole("MENTOR")
                        .requestMatchers("/api/student/**").hasAnyRole("STUDENT", "ADMIN", "SUPER_ADMIN")
                        .requestMatchers("/api/users/**").hasAnyRole("SUPER_ADMIN", "ADMIN", "MANAGER", "HR")

                        .requestMatchers("/api/lms/**").hasAnyRole(
                                "SUPER_ADMIN",
                                "ADMIN",
                                "TRAINER",
                                "MENTOR",
                                "STUDENT"
                        )
                        .requestMatchers("/api/lms/admin/**").hasAnyRole("ADMIN","SUPER_ADMIN","HR")
                        .requestMatchers(HttpMethod.GET, "/certificates/*/download").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                )
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }
}
