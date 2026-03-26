package com.saas.professor.entity;
import java.time.LocalDateTime;
import com.saas.professor.enums.PlanType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "teachers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Teacher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String password;
    @Enumerated(EnumType.STRING)
    @Column
    private PlanType plan;
    @Column(name = "plan_expires_at")
    private LocalDateTime planExpiresAt;
    @Column(nullable = false)
    private Boolean active;
    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;
    @Column(name = "email_verification_token")
    private String emailVerificationToken;
    @Column(name = "trial_ends_at")
    private LocalDateTime trialEndsAt;
    @Column(name = "password_reset_token")
    private String passwordResetToken;
    @Column(name = "password_reset_expires_at")
    private LocalDateTime passwordResetExpiresAt;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "mercado_pago_payer_id")
    private String mercadoPagoPayerId;

    public Teacher(String name, String email, String password, PlanType plan) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.plan = plan;
        this.active = false;
        this.emailVerified = false;
    }
    public Teacher(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.active = false;
        this.emailVerified = false;
    }
    public boolean isTrialActive() {
        return trialEndsAt != null && LocalDateTime.now().isBefore(trialEndsAt);
    }
    public boolean hasActivePlan() {
        return plan != null && planExpiresAt != null && LocalDateTime.now().isBefore(planExpiresAt);
    }
 // Teacher.java ou TeacherService
    public boolean canAccess() {
        if (this.active != null && this.active) return true;
        if (this.planExpiresAt == null) return false;
        
        return LocalDateTime.now().isBefore(this.planExpiresAt);
    }
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.emailVerified == null) this.emailVerified = false;
    }
}