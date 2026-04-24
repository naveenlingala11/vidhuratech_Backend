package com.vidhuratech.jobs.common.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
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

                .cors(cors -> {})

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/api/auth/**",
                                "/public/**"
                        ).permitAll()
                        .requestMatchers(
                                "/api/checkout/**"
                        ).permitAll()
                        .requestMatchers("/api/access/**").permitAll()
                        .requestMatchers("/api/leads/save").permitAll()
                        .requestMatchers("/api/lms/batches/course/*/active")
                        .permitAll()
                        .requestMatchers("/api/leads/**").hasRole("ADMIN")
                        .requestMatchers(
                                "/jobs/**",
                                "/certificates/**"
                        ).permitAll()
                        .requestMatchers("/api/super-admin/**")
                        .hasAnyRole("SUPER_ADMIN","ADMIN")

                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")

                        .requestMatchers("/api/hr/**")
                        .hasRole("HR")

                        .requestMatchers("/api/manager/**")
                        .hasRole("MANAGER")

                        .requestMatchers("/api/trainer/**")
                        .hasRole("TRAINER")

                        .requestMatchers("/api/mentor/**")
                        .hasRole("MENTOR")

                        .requestMatchers("/api/student/**")
                        .hasAnyRole("STUDENT", "ADMIN")

                        .requestMatchers("/api/users/**")
                        .hasAnyRole("SUPER_ADMIN", "ADMIN", "MANAGER", "HR")

                        // ✅ LMS MODULE ACCESS
                        .requestMatchers("/api/lms/**")
                        .hasAnyRole(
                                "SUPER_ADMIN",
                                "ADMIN",
                                "TRAINER",
                                "MENTOR",
                                "STUDENT"
                        )

                        .anyRequest()
                        .authenticated()
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