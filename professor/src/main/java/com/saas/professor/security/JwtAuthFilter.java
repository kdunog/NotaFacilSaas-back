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

        // Sem token — deixa o Spring Security decidir
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);
        final String requestURI = request.getRequestURI();

        // Token na blacklist (logout)
        try {
            if (Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + token))) {
                filterChain.doFilter(request, response);
                return;
            }
        } catch (Exception e) {
            System.err.println("Redis error checking blacklist: " + e.getMessage());
            // Se Redis falhar, continua sem checar blacklist
        }

        // Token inválido — deixa o Spring Security decidir (não rejeita aqui)
        if (!jwtService.isTokenValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        String email = jwtService.extractEmail(token);
        boolean isAdmin = jwtService.isAdminToken(token);

        try {
            UserDetails userDetails = isAdmin
                ? adminUserDetailsService.loadUserByUsername(email)
                : userDetailsService.loadUserByUsername(email);

            // Checar trial/plano apenas para professores (não admin)
            if (!isAdmin && userDetails instanceof TeacherUserDetails teacherDetails) {
                Teacher teacher = teacherDetails.getTeacher();
                String path = requestURI;
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

        } catch (Exception e) {
            System.err.println("Error loading user: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}