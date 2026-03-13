package com.saas.professor.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.saas.professor.dto.request.RegisterGradeRequest;
import com.saas.professor.dto.request.UpdateGradeRequest;
import com.saas.professor.dto.response.GradeResponse;
import com.saas.professor.security.TeacherUserDetails;
import com.saas.professor.service.GradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/grades")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;

    @PostMapping
    public ResponseEntity<GradeResponse> register(
            @Valid @RequestBody RegisterGradeRequest req,
            @AuthenticationPrincipal TeacherUserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(gradeService.register(req, userDetails.getTeacher().getId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GradeResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateGradeRequest req,
            @AuthenticationPrincipal TeacherUserDetails userDetails) {
        return ResponseEntity.ok(gradeService.update(id, req, userDetails.getTeacher().getId()));
    }

    @GetMapping("/exam/{examId}")
    public ResponseEntity<List<GradeResponse>> findByExam(
            @PathVariable Long examId,
            @AuthenticationPrincipal TeacherUserDetails userDetails) {
        return ResponseEntity.ok(gradeService.findByExam(examId, userDetails.getTeacher().getId()));
    }
}