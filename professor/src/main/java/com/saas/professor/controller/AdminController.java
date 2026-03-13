package com.saas.professor.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.saas.professor.dto.request.AdminLoginRequest;
import com.saas.professor.dto.response.AdminDashboardResponse;
import com.saas.professor.dto.response.AuthResponse;
import com.saas.professor.dto.response.TeacherSummaryResponse;
import com.saas.professor.service.AdminService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminController {
	
	private final AdminService adminService;

	// Login do admin — público
	@PostMapping("/admin/auth/login")
	public ResponseEntity<AuthResponse> login(
	        @Valid @RequestBody AdminLoginRequest req) {
	    return ResponseEntity.ok(adminService.login(req));
	}

	// Dashboard — protegido por ROLE_ADMIN
	@GetMapping("/admin/dashboard")
	public ResponseEntity<AdminDashboardResponse> dashboard() {
	    return ResponseEntity.ok(adminService.getDashboard());
	}

	// Lista todos os professores
	@GetMapping("/admin/teachers")
	public ResponseEntity<List<TeacherSummaryResponse>> allTeachers() {
	    return ResponseEntity.ok(adminService.getAllTeachers());
	}

	// Ativar/desativar professor
	@PatchMapping("/admin/teachers/{id}/toggle")
	public ResponseEntity<Void> toggleTeacher(@PathVariable Long id) {
	    adminService.toggleTeacherActive(id);
	    return ResponseEntity.noContent().build();
	}

}
