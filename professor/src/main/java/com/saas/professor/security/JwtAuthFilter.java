package com.saas.professor.security;
import java.io.IOException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.saas.professor.entity.Teacher;
import com.saas.professor.service.AdminUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;
    private final AdminUserDetailsService adminUserDetailsService;
    private final RedisTemplate<String, String> redisTemplate;

    public JwtAuthFilter(JwtService jwtService,
            UserDetailsServiceImpl userDetailsService,
            AdminUserDetailsService adminUserDetailsService,
            RedisTemplate<String, String> redisTemplate) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.adminUserDetailsService = adminUserDetailsService;
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);

        // Token na blacklist (logout)
        if (Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + token))) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Token invalidado\"}");
            return;
        }

        if (!jwtService.isTokenValid(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Token expirado ou invalido\"}");
            return;
        }

        String email = jwtService.extractEmail(token);
        boolean isAdmin = jwtService.isAdminToken(token);

        UserDetails userDetails = isAdmin
            ? adminUserDetailsService.loadUserByUsername(email)
            : userDetailsService.loadUserByUsername(email);

        if (!isAdmin && userDetails instanceof TeacherUserDetails teacherDetails) {
            Teacher teacher = teacherDetails.getTeacher();
            String path = request.getRequestURI();
            boolean isCheckout = path.startsWith("/api/v1/subscriptions/checkout");
            if (!isCheckout && !teacher.canAccess()) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"TRIAL_EXPIRED\",\"message\":\"Seu periodo de teste expirou. Assine um plano para continuar.\"}");
                return;
            }
        }

        UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }
}