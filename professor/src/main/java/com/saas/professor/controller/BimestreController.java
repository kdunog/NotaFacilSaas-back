package com.saas.professor.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saas.professor.dto.request.CreateBimestreRequest;
import com.saas.professor.dto.response.AnnualReportResponse;
import com.saas.professor.dto.response.BimestreReportResponse;
import com.saas.professor.dto.response.BimestreResponse;
import com.saas.professor.security.TeacherUserDetails;
import com.saas.professor.service.BimestrePdfService;
import com.saas.professor.service.BimestreService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/bimestres")
@RequiredArgsConstructor
public class BimestreController {
	
	private final BimestreService bimestreService;
	private final BimestrePdfService bimestrePdfService;

	@PostMapping
	public ResponseEntity<BimestreResponse> create(
	        @Valid @RequestBody CreateBimestreRequest req,
	        @AuthenticationPrincipal TeacherUserDetails userDetails) {
	    return ResponseEntity.status(HttpStatus.CREATED)
	            .body(bimestreService.create(req, userDetails.getTeacher().getId()));
	}

	@PostMapping("/{id}/exams/{examId}")
	public ResponseEntity<BimestreResponse> addExam(
	        @PathVariable Long id,
	        @PathVariable Long examId,
	        @AuthenticationPrincipal TeacherUserDetails userDetails) {
	    return ResponseEntity.ok(
	            bimestreService.addExam(id, examId, userDetails.getTeacher().getId()));
	}

	@PatchMapping("/{id}/close")
	public ResponseEntity<BimestreResponse> close(
	        @PathVariable Long id,
	        @AuthenticationPrincipal TeacherUserDetails userDetails) {
	    return ResponseEntity.ok(
	            bimestreService.close(id, userDetails.getTeacher().getId()));
	}

	@GetMapping("/classroom/{classRoomId}")
	public ResponseEntity<List<BimestreResponse>> findByClassRoom(
	        @PathVariable Long classRoomId,
	        @AuthenticationPrincipal TeacherUserDetails userDetails) {
	    return ResponseEntity.ok(
	            bimestreService.findByClassRoom(classRoomId, userDetails.getTeacher().getId()));
	}

	// Relatório bimestral — dados na tela
	@GetMapping("/{id}/report")
	public ResponseEntity<BimestreReportResponse> bimestreReport(
	        @PathVariable Long id,
	        @AuthenticationPrincipal TeacherUserDetails userDetails) {
	    return ResponseEntity.ok(
	            bimestreService.getBimestreReport(id, userDetails.getTeacher().getId()));
	}

	// Relatório bimestral — PDF
	@GetMapping("/{id}/report/pdf")
	public ResponseEntity<byte[]> bimestreReportPdf(
	        @PathVariable Long id,
	        @AuthenticationPrincipal TeacherUserDetails userDetails) {
	    byte[] pdf = bimestrePdfService.exportBimestreReport(
	            id, userDetails.getTeacher().getId());
	    return ResponseEntity.ok()
	            .header(HttpHeaders.CONTENT_DISPOSITION,
	                    "attachment; filename=fechamento_bimestre_" + id + ".pdf")
	            .contentType(MediaType.APPLICATION_PDF)
	            .body(pdf);
	}

	// Relatório anual — dados na tela
	@GetMapping("/classroom/{classRoomId}/annual/{schoolYear}")
	public ResponseEntity<AnnualReportResponse> annualReport(
	        @PathVariable Long classRoomId,
	        @PathVariable Integer schoolYear,
	        @AuthenticationPrincipal TeacherUserDetails userDetails) {
	    return ResponseEntity.ok(
	            bimestreService.getAnnualReport(
	                classRoomId, schoolYear, userDetails.getTeacher().getId()));
	}

	// Relatório anual — PDF
	@GetMapping("/classroom/{classRoomId}/annual/{schoolYear}/pdf")
	public ResponseEntity<byte[]> annualReportPdf(
	        @PathVariable Long classRoomId,
	        @PathVariable Integer schoolYear,
	        @AuthenticationPrincipal TeacherUserDetails userDetails) {
	    byte[] pdf = bimestrePdfService.exportAnnualReport(
	            classRoomId, schoolYear, userDetails.getTeacher().getId());
	    return ResponseEntity.ok()
	            .header(HttpHeaders.CONTENT_DISPOSITION,
	                    "attachment; filename=relatorio_anual_" + schoolYear + ".pdf")
	            .contentType(MediaType.APPLICATION_PDF)
	            .body(pdf);
	}

}
