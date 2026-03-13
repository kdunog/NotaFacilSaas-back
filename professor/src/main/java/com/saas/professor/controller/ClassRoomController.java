package com.saas.professor.controller;

import com.saas.professor.dto.request.CreateClassRoomRequest;
import com.saas.professor.dto.request.UpdateClassRoomRequest;
import com.saas.professor.dto.response.ClassRoomResponse;
import com.saas.professor.dto.response.StudentResponse;
import com.saas.professor.security.TeacherUserDetails;
import com.saas.professor.service.ClassRoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/classrooms")
public class ClassRoomController {

    private final ClassRoomService classRoomService;
    
	public ClassRoomController(ClassRoomService classRoomService) {
		this.classRoomService = classRoomService;
	}
	

    @PostMapping
    public ResponseEntity<ClassRoomResponse> create(
            @Valid @RequestBody CreateClassRoomRequest req,
            @AuthenticationPrincipal TeacherUserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(classRoomService.create(req, userDetails.getTeacher().getId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClassRoomResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateClassRoomRequest req,
            @AuthenticationPrincipal TeacherUserDetails userDetails) {
        return ResponseEntity.ok(classRoomService.update(id, req, userDetails.getTeacher().getId()));
    }
    
    @PostMapping("/{id}/duplicate")
    public ResponseEntity<ClassRoomResponse> duplicate(
            @PathVariable Long id,
            @AuthenticationPrincipal TeacherUserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(classRoomService.duplicate(id, userDetails.getTeacher().getId()));
    }

    @GetMapping
    public ResponseEntity<List<ClassRoomResponse>> findAll(
            @AuthenticationPrincipal TeacherUserDetails userDetails) {
        return ResponseEntity.ok(classRoomService.findAll(userDetails.getTeacher().getId()));
    }

    @GetMapping("/active")
    public ResponseEntity<List<ClassRoomResponse>> findActive(
            @AuthenticationPrincipal TeacherUserDetails userDetails) {
        return ResponseEntity.ok(classRoomService.findActive(userDetails.getTeacher().getId()));
    }

    @PatchMapping("/{id}/archive")
    public ResponseEntity<ClassRoomResponse> archive(
            @PathVariable Long id,
            @AuthenticationPrincipal TeacherUserDetails userDetails) {
        return ResponseEntity.ok(classRoomService.archive(id, userDetails.getTeacher().getId()));
    }

    @PatchMapping("/{id}/unarchive")
    public ResponseEntity<ClassRoomResponse> unarchive(
            @PathVariable Long id,
            @AuthenticationPrincipal TeacherUserDetails userDetails) {
        return ResponseEntity.ok(classRoomService.unarchive(id, userDetails.getTeacher().getId()));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal TeacherUserDetails userDetails) {
        classRoomService.delete(id, userDetails.getTeacher().getId());
        return ResponseEntity.noContent().build();
    }
    
    
    
}