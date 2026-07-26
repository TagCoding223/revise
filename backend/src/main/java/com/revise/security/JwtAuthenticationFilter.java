package com.revise.security;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.revise.dto.response.ApiResponse;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    // Inject ObjectMapper to convert Java object to JSON
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // 1. Extract the JWT from the request header
            String jwt = getJwtFromRequest(request);

            // 2. Validate the token
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {

                // 3. Get the user ID from the token
                String userId = tokenProvider.getUserIdFromToken(jwt);

                // 4. Load the user details
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(userId);

                // 5. Create an authentication object
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                // Attach the IP address and session ID to the auth object
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 6. Tell Spring Security that this user is officially authenticated
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            // If everything is fine, continue the chain
            // 7. Continue the request to the next filter or the controller
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException ex) {
            // Catch expired tokens specifically
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Session expired. Please log in again.");
        } catch (JwtException | IllegalArgumentException ex) {
            // Catch manipulated or invalid tokens
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Invalid authentication token.");
        } catch (Exception ex) {
            // Catch anything else that goes wrong in the filter
            log.error("Could not set user authentication in security context: " + ex.getMessage());
            // Catch anything else that goes wrong in the filter
            sendErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR,
                    "An internal error occurred during authentication.");
        }

    }

    // Helper method to extract the token from the "Authorization: Bearer <token>"
    // header
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // Helper method to manually construct and send the JSON response
    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");

        ApiResponse apiResponse = new ApiResponse(false, message);
        String jsonResponse = objectMapper.writeValueAsString(apiResponse);

        response.getWriter().write(jsonResponse);
    }
}
