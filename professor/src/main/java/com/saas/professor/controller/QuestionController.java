package com.saas.professor.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saas.professor.dto.request.CreateQuestionRequest;
import com.saas.professor.dto.response.QuestionResponse;
import com.saas.professor.security.TeacherUserDetails;
import com.saas.professor.service.QuestionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
public class QuestionController {
	
	private final QuestionService questionService;

	@PostMapping
	public ResponseEntity<QuestionResponse> create(
	        @Valid @RequestBody CreateQuestionRequest req,
	        @AuthenticationPrincipal TeacherUserDetails userDetails) {
	    return ResponseEntity.status(HttpStatus.CREATED)
	            .body(questionService.create(req, userDetails.getTeacher().getId()));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(
	        @PathVariable Long id,
	        @AuthenticationPrincipal TeacherUserDetails userDetails) {
	    questionService.delete(id, userDetails.getTeacher().getId());
	    return ResponseEntity.noContent().build();
	}

	@GetMapping("/exam/{examId}")
	public ResponseEntity<List<QuestionResponse>> findByExam(
	        @PathVariable Long examId,
	        @AuthenticationPrincipal TeacherUserDetails userDetails) {
	    return ResponseEntity.ok(
	            questionService.findByExam(examId, userDetails.getTeacher().getId()));
	}

}
