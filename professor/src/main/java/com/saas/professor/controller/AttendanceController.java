package com.saas.professor.controller;

import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.saas.professor.dto.request.SaveAttendanceListRequest;
import com.saas.professor.dto.response.AttendanceResponse;
import com.saas.professor.dto.response.StudentAttendanceReportResponse;
import com.saas.professor.security.TeacherUserDetails;
import com.saas.professor.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/attendances")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    

    @PostMapping
    public ResponseEntity<List<AttendanceResponse>> save(
            @Valid @RequestBody SaveAttendanceListRequest req,
            @AuthenticationPrincipal TeacherUserDetails userDetails) {
        return ResponseEntity.ok(
                attendanceService.saveAttendanceList(req, userDetails.getTeacher().getId()));
    }

    @PostMapping("/classroom/{classRoomId}/mark-all-present")
    public ResponseEntity<List<AttendanceResponse>> markAllPresent(
            @PathVariable Long classRoomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal TeacherUserDetails userDetails) {
        return ResponseEntity.ok(
                attendanceService.markAllPresent(classRoomId, date, userDetails.getTeacher().getId()));
    }
    
 // Lista todos os dias com chamada registrada
    @GetMapping("/classroom/{classRoomId}/days")
    public ResponseEntity<List<LocalDate>> findClassDays(
            @PathVariable Long classRoomId,
            @AuthenticationPrincipal TeacherUserDetails userDetails) {
        return ResponseEntity.ok(
            attendanceService.findClassDays(classRoomId, userDetails.getTeacher().getId()));
    }

    @GetMapping("/classroom/{classRoomId}")
    public ResponseEntity<List<AttendanceResponse>> findByClassRoomAndDate(
            @PathVariable Long classRoomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal TeacherUserDetails userDetails) {
        return ResponseEntity.ok(
                attendanceService.findByClassRoomAndDate(classRoomId, date, userDetails.getTeacher().getId()));
    }

    @GetMapping("/classroom/{classRoomId}/report")
    public ResponseEntity<List<StudentAttendanceReportResponse>> attendanceReport(
            @PathVariable Long classRoomId,
            @AuthenticationPrincipal TeacherUserDetails userDetails) {
        return ResponseEntity.ok(
                attendanceService.getAttendanceReport(classRoomId, userDetails.getTeacher().getId()));
    }
}