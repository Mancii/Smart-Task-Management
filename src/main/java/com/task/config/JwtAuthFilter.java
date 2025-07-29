package com.task.config;

import com.task.entity.User;
import com.task.service.JwtUserDetailsService;
import com.task.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);
    private final JwtService jwtService;
    private final JwtUserDetailsService userDetailService;

    @Autowired
    public JwtAuthFilter(JwtService jwtService, JwtUserDetailsService userDetailService) {
        this.jwtService = jwtService;
        this.userDetailService = userDetailService;
    }


    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String jwtToken = extractJwtFromRequest(request);
            if (jwtToken != null) {
                String email = jwtService.extractUserEmail(jwtToken);
                processAuthentication(request, email, jwtToken);
            }
        } catch (ExpiredJwtException e) {
            handleSecurityException(response, HttpServletResponse.SC_UNAUTHORIZED, "Token has expired");
            return;
        } catch (JwtException e) {
            handleSecurityException(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
            return;
        } catch (Exception e) {
            handleSecurityException(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Security processing error");
            return;
        }

        filterChain.doFilter(request, response);

    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private void processAuthentication(HttpServletRequest request, String email, String jwtToken) {
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            User userDetails = userDetailService.loadUserByEmail(email);
            boolean tokenValid = jwtService.isTokenValid(jwtToken, userDetails);
            if (tokenValid) {
                UsernamePasswordAuthenticationToken authentication = createAuthenticationToken(userDetails, request);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
    }

    private UsernamePasswordAuthenticationToken createAuthenticationToken(UserDetails userDetails, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return authentication;
    }

    private void handleSecurityException(HttpServletResponse response, int status, String message) throws IOException {
        logger.error(message);
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write(String.format("{\"error\": \"%s\"}", message));
    }

}
