package com.janpeterdhalle.transfer.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.janpeterdhalle.transfer.services.AuthService;
import com.janpeterdhalle.transfer.services.UserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {
    private final UserDetailsService userDetailsService;
    private final AuthService authService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        System.out.println("JWTFilter.doFilterInternal");
        String authHeader = request.getHeader("Authorization");
        System.out.println("AUTH HEADER: " + authHeader);
        if (authHeader != null && !authHeader.isBlank() && authHeader.startsWith("Bearer ")) {
            System.out.println("FILTER doing jwt check?");
            String jwt = authHeader.substring(7);
            if (jwt.isBlank()) {
                System.out.println("JWTFilter.doFilterInternal invalid!!: JWT is missing or empty");
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "JWT is missing or empty");
            } else {
                try {
                    String email = authService.validateTokenAndRetrieveSubject(jwt);
                    System.out.println("JWTFilter.doFilterInternal validated" + email);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            email,
                            userDetails.getPassword(),
                            userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request)); // enhance token with data from request
                    if (SecurityContextHolder.getContext().getAuthentication() == null) {
                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    }
                } catch (JWTVerificationException e) {
                    System.out.println("JWTFilter.doFilterInternal invalid token!!");
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT");
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
