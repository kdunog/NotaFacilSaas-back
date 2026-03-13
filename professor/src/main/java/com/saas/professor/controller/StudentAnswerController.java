package com.saas.professor.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saas.professor.dto.request.RegisterAnswersRequest;
import com.saas.professor.dto.response.StudentAnswerResponse;
import com.saas.professor.security.TeacherUserDetails;
import com.saas.professor.service.StudentAnswerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/answers")
@RequiredArgsConstructor
public class StudentAnswerController {

	private final StudentAnswerService studentAnswerService;

	@PostMapping
	public ResponseEntity<StudentAnswerResponse> register(
	        @Valid @RequestBody RegisterAnswersRequest req,
	        @AuthenticationPrincipal TeacherUserDetails userDetails) {
	    return ResponseEntity.ok(
	            studentAnswerService.registerAnswers(req, userDetails.getTeacher().getId()));
	}
}
