package com.saas.professor.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private Long teacherId;
    private String name;
    private String plan;
    private LocalDateTime trialEndsAt;
    private Boolean trialExpired;

    // Construtor legado para admin
    public AuthResponse(String accessToken, String refreshToken, Long teacherId, String name, String plan) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.teacherId = teacherId;
        this.name = name;
        this.plan = plan;
        this.trialExpired = false;
    }
}