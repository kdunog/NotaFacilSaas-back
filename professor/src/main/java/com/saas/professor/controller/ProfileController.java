package com.saas.professor.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saas.professor.dto.request.UpdateNameRequest;
import com.saas.professor.dto.request.UpdatePasswordRequest;
import com.saas.professor.security.TeacherUserDetails;
import com.saas.professor.service.ProfileService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @PutMapping("/name")
    public ResponseEntity<Void> updateName(
            @Valid @RequestBody UpdateNameRequest req,
            @AuthenticationPrincipal TeacherUserDetails userDetails) {
        profileService.updateName(userDetails.getTeacher().getId(), req.getName());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/password")
    public ResponseEntity<Void> updatePassword(
            @Valid @RequestBody UpdatePasswordRequest req,
            @AuthenticationPrincipal TeacherUserDetails userDetails) {
        profileService.updatePassword(userDetails.getTeacher().getId(), req.getCurrentPassword(), req.getNewPassword());
        return ResponseEntity.ok().build();
    }
}