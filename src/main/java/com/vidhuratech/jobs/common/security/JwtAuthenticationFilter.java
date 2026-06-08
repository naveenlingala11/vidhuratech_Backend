package com.vidhuratech.jobs.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getServletPath();
        String authHeader = request.getHeader("Authorization");

        if ((authHeader == null || !authHeader.startsWith("Bearer ")) && isPublicPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.isValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        String email = jwtUtil.extractEmail(token);

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            List<GrantedAuthority> authorities = userDetails.getAuthorities()
                    .stream()
                    .map(authority -> {
                        String role = authority.getAuthority();

                        if (role == null || role.isBlank()) {
                            return null;
                        }

                        if (!role.startsWith("ROLE_")) {
                            role = "ROLE_" + role;
                        }

                        return new SimpleGrantedAuthority(role);
                    })
                    .filter(authority -> authority != null)
                    .map(authority -> (GrantedAuthority) authority)
                    .toList();

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            authorities
                    );

            authToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicPath(String path) {
        if (path == null || path.isBlank()) {
            return false;
        }

        return path.equals("/api/auth/register")
                || path.equals("/api/auth/login")
                || path.equals("/api/auth/set-password")
                || path.equals("/api/auth/send-otp")
                || path.equals("/api/auth/verify-otp")
                || path.equals("/api/auth/register/init")
                || path.equals("/api/auth/register/verify")
                || path.equals("/api/auth/resend-link")
                || path.equals("/api/auth/validate-token")
                || path.equals("/api/auth/oauth/google")
                || path.equals("/api/auth/oauth/github")
                || path.equals("/api/auth/phone/send-otp")
                || path.equals("/api/auth/phone/verify-otp")
                || path.startsWith("/api/public/")
                || path.startsWith("/public/");
    }
}