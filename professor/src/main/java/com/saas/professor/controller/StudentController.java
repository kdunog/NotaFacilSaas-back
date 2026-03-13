package com.saas.professor.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.saas.professor.dto.request.CreateStudentRequest;
import com.saas.professor.dto.response.StudentResponse;
import com.saas.professor.security.TeacherUserDetails;
import com.saas.professor.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @PostMapping
    public ResponseEntity<StudentResponse> create(
            @Valid @RequestBody CreateStudentRequest req,
            @AuthenticationPrincipal TeacherUserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(studentService.create(req, userDetails.getTeacher().getId()));
    }

    @GetMapping
    public ResponseEntity<List<StudentResponse>> findAll(
            @AuthenticationPrincipal TeacherUserDetails userDetails) {
        return ResponseEntity.ok(studentService.findAllByTeacher(userDetails.getTeacher().getId()));
    }

    @GetMapping("/active")
    public ResponseEntity<List<StudentResponse>> findActive(
            @AuthenticationPrincipal TeacherUserDetails userDetails) {
        return ResponseEntity.ok(studentService.findActiveByTeacher(userDetails.getTeacher().getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentResponse> findById(
            @PathVariable Long id,
            @AuthenticationPrincipal TeacherUserDetails userDetails) {
        return ResponseEntity.ok(studentService.findById(id, userDetails.getTeacher().getId()));
    }
    
    @GetMapping("/without-enrollment")
    public ResponseEntity<List<StudentResponse>> findWithoutEnrollment(
            @AuthenticationPrincipal TeacherUserDetails userDetails) {
        return ResponseEntity.ok(
            studentService.findWithoutActiveEnrollment(userDetails.getTeacher().getId()));
    }
    
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal TeacherUserDetails userDetails) {
        studentService.delete(id, userDetails.getTeacher().getId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<StudentResponse> reactivate(
            @PathVariable Long id,
            @AuthenticationPrincipal TeacherUserDetails userDetails) {
        return ResponseEntity.ok(studentService.reactivate(id, userDetails.getTeacher().getId()));
    }
    
    
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(
            @PathVariable Long id,
            @AuthenticationPrincipal TeacherUserDetails userDetails) {
        studentService.deactivate(id, userDetails.getTeacher().getId());
        return ResponseEntity.noContent().build();
    }
}