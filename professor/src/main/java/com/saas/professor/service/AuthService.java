package com.saas.professor.service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saas.professor.dto.request.LoginRequest;
import com.saas.professor.dto.request.RefreshTokenRequest;
import com.saas.professor.dto.request.RegisterRequest;
import com.saas.professor.dto.response.AuthResponse;
import com.saas.professor.entity.RefreshToken;
import com.saas.professor.entity.Teacher;
import com.saas.professor.exceptions.BusinessException;
import com.saas.professor.repository.RefreshTokenRepository;
import com.saas.professor.repository.TeacherRepository;
import com.saas.professor.security.JwtService;

@Service
public class AuthService {

    private static final int TRIAL_DAYS = 5;

    private final TeacherRepository teacherRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RedisTemplate<String, String> redisTemplate;
    private final EmailService emailService;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    public AuthService(TeacherRepository teacherRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RedisTemplate<String, String> redisTemplate,
            EmailService emailService) {
        this.teacherRepository = teacherRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.redisTemplate = redisTemplate;
        this.emailService = emailService;
    }

    @Transactional
    public void register(RegisterRequest request) {
        if (teacherRepository.existsByEmail(request.getEmail()))
            throw new BusinessException("E-mail já cadastrado");

        String verificationToken = UUID.randomUUID().toString();

        Teacher teacher = new Teacher(
                request.getName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword())
        );
        teacher.setEmailVerificationToken(verificationToken);
        teacher.setEmailVerified(false);
        teacher.setActive(false);

        teacherRepository.save(teacher);
        emailService.sendVerificationEmail(teacher.getEmail(), teacher.getName(), verificationToken);
    }

    @Transactional
    public AuthResponse verifyEmail(String token) {
        Teacher teacher = teacherRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new BusinessException("Link de verificação inválido ou já utilizado"));

        teacher.setEmailVerified(true);
        teacher.setActive(true);
        teacher.setEmailVerificationToken(null);

        // Inicia trial de 5 dias no momento da confirmação do e-mail
        teacher.setTrialEndsAt(LocalDateTime.now().plusDays(TRIAL_DAYS));

        teacherRepository.save(teacher);
        return generateTokens(teacher);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Teacher teacher = teacherRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("E-mail ou senha inválidos"));

        if (!passwordEncoder.matches(request.getPassword(), teacher.getPassword()))
            throw new BusinessException("E-mail ou senha inválidos");

        if (!Boolean.TRUE.equals(teacher.getEmailVerified()))
            throw new BusinessException("Confirme seu e-mail antes de entrar. Verifique sua caixa de entrada.");

        if (!Boolean.TRUE.equals(teacher.getActive()))
            throw new BusinessException("Conta desativada");

        return generateTokens(teacher);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BusinessException("Refresh token inválido"));

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new BusinessException("Refresh token expirado. Faça login novamente.");
        }

        Teacher teacher = teacherRepository.findById(refreshToken.getTeacherId())
                .orElseThrow(() -> new BusinessException("Professor não encontrado"));

        refreshTokenRepository.delete(refreshToken);
        return generateTokens(teacher);
    }

    @Transactional
    public void logout(String accessToken, Long teacherId) {
        redisTemplate.opsForValue().set(
                "blacklist:" + accessToken,
                "true",
                accessTokenExpiration,
                TimeUnit.MILLISECONDS
        );
        refreshTokenRepository.deleteAllByTeacherId(teacherId);
    }

    @Transactional
    public void resendVerification(String email) {
        Teacher teacher = teacherRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("E-mail não encontrado"));

        if (Boolean.TRUE.equals(teacher.getEmailVerified()))
            throw new BusinessException("E-mail já verificado");

        String newToken = UUID.randomUUID().toString();
        teacher.setEmailVerificationToken(newToken);
        teacherRepository.save(teacher);

        emailService.sendVerificationEmail(teacher.getEmail(), teacher.getName(), newToken);
    }
    
 // Adicionar estes métodos no AuthService.java existente
 // (dentro da classe, junto dos outros métodos)

     @Transactional
     public void forgotPassword(String email) {
         // Não revela se e-mail existe ou não (segurança)
         teacherRepository.findByEmail(email).ifPresent(teacher -> {
             String token = UUID.randomUUID().toString();
             teacher.setPasswordResetToken(token);
             teacher.setPasswordResetExpiresAt(LocalDateTime.now().plusHours(1));
             teacherRepository.save(teacher);
             emailService.sendPasswordResetEmail(teacher.getEmail(), teacher.getName(), token);
         });
     }

     @Transactional
     public void resetPassword(String token, String newPassword) {
         Teacher teacher = teacherRepository.findByPasswordResetToken(token)
             .orElseThrow(() -> new BusinessException("Token inválido ou expirado"));

         if (teacher.getPasswordResetExpiresAt().isBefore(LocalDateTime.now()))
             throw new BusinessException("Token expirado. Solicite um novo link.");

         teacher.setPassword(passwordEncoder.encode(newPassword));
         teacher.setPasswordResetToken(null);
         teacher.setPasswordResetExpiresAt(null);
         teacherRepository.save(teacher);
     }

    private AuthResponse generateTokens(Teacher teacher) {
        String accessToken = jwtService.generateAccessToken(teacher.getId(), teacher.getEmail());

        String refreshTokenValue = UUID.randomUUID().toString();
        refreshTokenRepository.save(new RefreshToken(
                teacher.getId(),
                refreshTokenValue,
                LocalDateTime.now().plusDays(7)
        ));

        boolean trialExpired = !teacher.canAccess();

        AuthResponse response = new AuthResponse(accessToken, refreshTokenValue,
                teacher.getId(), teacher.getName(),
                teacher.getPlan() != null ? teacher.getPlan().name() : null,
                teacher.getTrialEndsAt(),
                trialExpired);

        return response;
    }
}