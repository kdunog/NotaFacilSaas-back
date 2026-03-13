package com.saas.professor.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.saas.professor.dto.request.MoveStudentRequest;
import com.saas.professor.dto.response.StudentClassRoomResponse;
import com.saas.professor.security.TeacherUserDetails;
import com.saas.professor.service.StudentMovimentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/movements")
public class StudentMovimentController {

    private final StudentMovimentService movimentService;
    
    public StudentMovimentController(StudentMovimentService movimentService) {
    	        this.movimentService = movimentService;
    }

    @PostMapping("/enroll/{studentId}/classroom/{classRoomId}")
    public ResponseEntity<StudentClassRoomResponse> enroll(
            @PathVariable Long studentId,
            @PathVariable Long classRoomId,
            @AuthenticationPrincipal TeacherUserDetails userDetails) {
        Long teacherId = userDetails.getTeacher().getId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(movimentService.enroll(studentId, classRoomId, teacherId));
    }

    @PostMapping("/move")
    public ResponseEntity<List<StudentClassRoomResponse>> move(
            @Valid @RequestBody MoveStudentRequest req,
            @AuthenticationPrincipal TeacherUserDetails userDetails) {
        Long teacherId = userDetails.getTeacher().getId();
        return ResponseEntity.ok(movimentService.move(req, teacherId));
    }
    
    @GetMapping("/student/{studentId}/history")
    public ResponseEntity<List<StudentClassRoomResponse>> history(
            @PathVariable Long studentId) {
        return ResponseEntity.ok(movimentService.getHistory(studentId));
    }

    @GetMapping("/classroom/{classRoomId}/students")
    public ResponseEntity<List<StudentClassRoomResponse>> studentsInClass(
            @PathVariable Long classRoomId) {
        return ResponseEntity.ok(movimentService.getStudentsInClass(classRoomId));
    }
    
    @DeleteMapping("/classroom/{classRoomId}/student/{studentId}")
    public ResponseEntity<Void> unenroll(
            @PathVariable Long classRoomId,
            @PathVariable Long studentId,
            @AuthenticationPrincipal TeacherUserDetails userDetails) {
        movimentService.unenroll(studentId, classRoomId, userDetails.getTeacher().getId());
        return ResponseEntity.noContent().build();
    }
}