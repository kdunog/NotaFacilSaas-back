package com.saas.professor.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saas.professor.dto.response.AtRiskStudentResponse;
import com.saas.professor.dto.response.AttendanceVsGradeResponse;
import com.saas.professor.dto.response.ClassRoomReportResponse;
import com.saas.professor.dto.response.StudentEvolutionResponse;
import com.saas.professor.dto.response.StudentReportResponse;
import com.saas.professor.security.TeacherUserDetails;
import com.saas.professor.service.PdfExportService;
import com.saas.professor.service.ReportService;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {
	
	private final ReportService reportService;
	private final PdfExportService pdfExportService;
	
	public ReportController(ReportService reportService, PdfExportService pdfExportService) {
		this.reportService = reportService;
		this.pdfExportService = pdfExportService;
	}

	@GetMapping("/student/{studentId}")
	public ResponseEntity<StudentReportResponse> studentReport(
	        @PathVariable Long studentId,
	        @AuthenticationPrincipal TeacherUserDetails userDetails) {
	    return ResponseEntity.ok(
	            reportService.getStudentReport(studentId, userDetails.getTeacher().getId()));
	}

	@GetMapping("/student/{studentId}/evolution")
	public ResponseEntity<List<StudentEvolutionResponse>> studentEvolution(
	        @PathVariable Long studentId,
	        @AuthenticationPrincipal TeacherUserDetails userDetails) {
	    return ResponseEntity.ok(
	            reportService.getStudentEvolution(studentId, userDetails.getTeacher().getId()));
	}

	@GetMapping("/classroom/{classRoomId}")
	public ResponseEntity<ClassRoomReportResponse> classRoomReport(
	        @PathVariable Long classRoomId,
	        @AuthenticationPrincipal TeacherUserDetails userDetails) {
	    return ResponseEntity.ok(
	            reportService.getClassRoomReport(classRoomId, userDetails.getTeacher().getId()));
	}

	@GetMapping("/classroom/{classRoomId}/at-risk")
	public ResponseEntity<List<AtRiskStudentResponse>> atRiskStudents(
	        @PathVariable Long classRoomId,
	        @AuthenticationPrincipal TeacherUserDetails userDetails) {
	    return ResponseEntity.ok(
	            reportService.getAtRiskStudents(classRoomId, userDetails.getTeacher().getId()));
	}

	@GetMapping("/classroom/{classRoomId}/attendance-vs-grade")
	public ResponseEntity<List<AttendanceVsGradeResponse>> attendanceVsGrade(
	        @PathVariable Long classRoomId,
	        @AuthenticationPrincipal TeacherUserDetails userDetails) {
	    return ResponseEntity.ok(
	            reportService.getAttendanceVsGrade(classRoomId, userDetails.getTeacher().getId()));
	}

	// PDF do relatório do aluno
	@GetMapping("/student/{studentId}/pdf")
	public ResponseEntity<byte[]> studentReportPdf(
	        @PathVariable Long studentId,
	        @AuthenticationPrincipal TeacherUserDetails userDetails) {
	    byte[] pdf = pdfExportService.exportStudentReport(studentId, userDetails.getTeacher().getId());
	    return ResponseEntity.ok()
	            .header(HttpHeaders.CONTENT_DISPOSITION,
	                    "attachment; filename=relatorio_aluno_" + studentId + ".pdf")
	            .contentType(MediaType.APPLICATION_PDF)
	            .body(pdf);
	}

	// PDF do relatório da turma
	@GetMapping("/classroom/{classRoomId}/pdf")
	public ResponseEntity<byte[]> classRoomReportPdf(
	        @PathVariable Long classRoomId,
	        @AuthenticationPrincipal TeacherUserDetails userDetails) {
	    byte[] pdf = pdfExportService.exportClassRoomReport(classRoomId, userDetails.getTeacher().getId());
	    return ResponseEntity.ok()
	            .header(HttpHeaders.CONTENT_DISPOSITION,
	                    "attachment; filename=relatorio_turma_" + classRoomId + ".pdf")
	            .contentType(MediaType.APPLICATION_PDF)
	            .body(pdf);
	}

}
