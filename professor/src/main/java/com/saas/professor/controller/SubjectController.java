package com.saas.professor.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saas.professor.dto.request.CreateSubjectRequest;
import com.saas.professor.dto.response.SubjectResponse;
import com.saas.professor.security.TeacherUserDetails;
import com.saas.professor.service.SubjectService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/subjects")
public class SubjectController {
	
	private final SubjectService subjectService;
	
	public SubjectController(SubjectService subjectService) {
		this.subjectService = subjectService;
	}

	@PostMapping
	public ResponseEntity<SubjectResponse> create(
	        @Valid @RequestBody CreateSubjectRequest req,
	        @AuthenticationPrincipal TeacherUserDetails userDetails) {
	    return ResponseEntity.status(HttpStatus.CREATED)
	            .body(subjectService.create(req, userDetails.getTeacher().getId()));
	}

	@GetMapping("/teacher/{teacherId}")
	public ResponseEntity<List<SubjectResponse>> findAll(@PathVariable Long teacherId) {
	    return ResponseEntity.ok(subjectService.findAllByTeacher(teacherId));
	}
}
