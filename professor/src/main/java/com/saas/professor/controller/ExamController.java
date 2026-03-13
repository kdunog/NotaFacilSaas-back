package com.saas.professor.controller;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.saas.professor.dto.request.CreateExamRequest;
import com.saas.professor.dto.request.UpdateExamRequest;
import com.saas.professor.dto.response.ExamResponse;
import com.saas.professor.security.TeacherUserDetails;
import com.saas.professor.service.ExamService;
import com.saas.professor.service.ExamPdfService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/exams")
@RequiredArgsConstructor
public class ExamController {
    private final ExamService examService;
    private final ExamPdfService examPdfService;

    @PostMapping
    public ResponseEntity<ExamResponse> create(
            @Valid @RequestBody CreateExamRequest req,
            @AuthenticationPrincipal TeacherUserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(examService.create(req, userDetails.getTeacher().getId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExamResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateExamRequest req,
            @AuthenticationPrincipal TeacherUserDetails userDetails) {
        return ResponseEntity.ok(examService.update(id, req, userDetails.getTeacher().getId()));
    }

    @PatchMapping("/{id}/apply")
    public ResponseEntity<ExamResponse> apply(
            @PathVariable Long id,
            @AuthenticationPrincipal TeacherUserDetails userDetails) {
        return ResponseEntity.ok(examService.apply(id, userDetails.getTeacher().getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal TeacherUserDetails userDetails) {
        examService.delete(id, userDetails.getTeacher().getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/classroom/{classRoomId}")
    public ResponseEntity<List<ExamResponse>> findByClassRoom(
            @PathVariable Long classRoomId,
            @AuthenticationPrincipal TeacherUserDetails userDetails) {
        return ResponseEntity.ok(
                examService.findByClassRoom(classRoomId, userDetails.getTeacher().getId()));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> generatePdf(
            @PathVariable Long id,
            @AuthenticationPrincipal TeacherUserDetails userDetails) {
        byte[] pdf = examPdfService.generateExamPdf(id, userDetails.getTeacher().getId());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=prova_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}