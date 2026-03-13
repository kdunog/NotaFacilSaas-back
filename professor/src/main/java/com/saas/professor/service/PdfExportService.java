package com.saas.professor.service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import com.saas.professor.dto.response.ClassRoomReportResponse;
import com.saas.professor.dto.response.GradeResponse;
import com.saas.professor.dto.response.StudentAttendanceReportResponse;
import com.saas.professor.dto.response.StudentEvolutionResponse;
import com.saas.professor.dto.response.StudentReportResponse;
import com.saas.professor.entity.Teacher;
import com.saas.professor.exceptions.BusinessException;

@Service
public class PdfExportService {
	
	private final ReportService reportService;
	private final AttendanceService attendanceService;
	
	public PdfExportService(ReportService reportService, AttendanceService attendanceService) {
		this.reportService = reportService;
		this.attendanceService = attendanceService;
	}

	public byte[] exportStudentReport(Long studentId, Long teacherId) {
	    StudentReportResponse report = reportService.getStudentReport(studentId, teacherId);
	    List<StudentEvolutionResponse> evolution =
	            reportService.getStudentEvolution(studentId, teacherId);

	    try (PDDocument doc = new PDDocument()) {
	        PDPage page = new PDPage(PDRectangle.A4);
	        doc.addPage(page);

	        try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
	            float y = 780;
	            float margin = 50;
	            float width = page.getMediaBox().getWidth() - 2 * margin;

	            // Título
	            cs.beginText();
	            cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
	            cs.newLineAtOffset(margin, y);
	            cs.showText("Relatório do Aluno");
	            cs.endText();
	            y -= 25;

	            cs.beginText();
	            cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 13);
	            cs.newLineAtOffset(margin, y);
	            cs.showText("Aluno: " + report.getStudentName());
	            cs.endText();
	            y -= 18;

	            cs.beginText();
	            cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
	            cs.newLineAtOffset(margin, y);
	            cs.showText("Média Geral: " + report.getAverage() + "%");
	            cs.endText();
	            y -= 18;

	            cs.beginText();
	            cs.newLineAtOffset(margin, y);
	            cs.showText("Total de Provas: " + report.getTotalExams());
	            cs.endText();
	            y -= 18;

	            cs.beginText();
	            cs.newLineAtOffset(margin, y);
	            cs.showText("Data: " + LocalDate.now());
	            cs.endText();
	            y -= 30;

	            // Cabeçalho da tabela de notas
	            cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 11);
	            cs.beginText();
	            cs.newLineAtOffset(margin, y);
	            cs.showText("Prova");
	            cs.endText();
	            cs.beginText();
	            cs.newLineAtOffset(margin + 220, y);
	            cs.showText("Nota");
	            cs.endText();
	            cs.beginText();
	            cs.newLineAtOffset(margin + 290, y);
	            cs.showText("Total");
	            cs.endText();
	            cs.beginText();
	            cs.newLineAtOffset(margin + 360, y);
	            cs.showText("%");
	            cs.endText();
	            y -= 5;

	            // Linha separadora
	            cs.moveTo(margin, y);
	            cs.lineTo(margin + width, y);
	            cs.stroke();
	            y -= 15;

	            // Dados das notas
	            cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
	            for (GradeResponse g : report.getGrades()) {
	                if (y < 60) break;
	                String title = g.getExamTitle().length() > 35
	                        ? g.getExamTitle().substring(0, 35) + "..." : g.getExamTitle();
	                double pct = g.getTotalPoints() > 0
	                        ? Math.round((g.getScore() / g.getTotalPoints()) * 10000.0) / 100.0
	                        : 0.0;

	                cs.beginText(); cs.newLineAtOffset(margin, y);
	                cs.showText(title); cs.endText();
	                cs.beginText(); cs.newLineAtOffset(margin + 220, y);
	                cs.showText(String.valueOf(g.getScore())); cs.endText();
	                cs.beginText(); cs.newLineAtOffset(margin + 290, y);
	                cs.showText(String.valueOf(g.getTotalPoints())); cs.endText();
	                cs.beginText(); cs.newLineAtOffset(margin + 360, y);
	                cs.showText(pct + "%"); cs.endText();
	                y -= 15;
	            }

	            // Rodapé
	            cs.beginText();
	            cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE), 8);
	            cs.newLineAtOffset(margin, 30);
	            cs.showText("Gerado pelo ProfManager em " + LocalDate.now());
	            cs.endText();
	        }

	        ByteArrayOutputStream out = new ByteArrayOutputStream();
	        doc.save(out);
	        return out.toByteArray();

	    } catch (Exception e) {
	        throw new BusinessException("Erro ao gerar PDF: " + e.getMessage());
	    }
	}

	public byte[] exportClassRoomReport(Long classRoomId, Long teacherId) {
	    ClassRoomReportResponse report = reportService.getClassRoomReport(classRoomId, teacherId);
	    List<StudentAttendanceReportResponse> attendance =
	            attendanceService.getAttendanceReport(classRoomId, teacherId);

	    try (PDDocument doc = new PDDocument()) {
	        PDPage page = new PDPage(PDRectangle.A4);
	        doc.addPage(page);

	        try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
	            float y = 780;
	            float margin = 50;
	            float width = page.getMediaBox().getWidth() - 2 * margin;

	            cs.beginText();
	            cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
	            cs.newLineAtOffset(margin, y);
	            cs.showText("Relatório da Turma");
	            cs.endText();
	            y -= 25;

	            cs.beginText();
	            cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
	            cs.newLineAtOffset(margin, y);
	            cs.showText(report.getClassRoomDescription());
	            cs.endText();
	            y -= 18;

	            cs.beginText();
	            cs.newLineAtOffset(margin, y);
	            cs.showText("Média da Turma: " + report.getClassAverage() + "%"
	                    + "   |   Total de Alunos: " + report.getTotalStudents()
	                    + "   |   Alunos em Risco: " + report.getStudentsAtRisk());
	            cs.endText();
	            y -= 30;

	            // Cabeçalho tabela
	            cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
	            cs.beginText(); cs.newLineAtOffset(margin, y);
	            cs.showText("Aluno"); cs.endText();
	            cs.beginText(); cs.newLineAtOffset(margin + 180, y);
	            cs.showText("Média"); cs.endText();
	            cs.beginText(); cs.newLineAtOffset(margin + 240, y);
	            cs.showText("Provas"); cs.endText();
	            cs.beginText(); cs.newLineAtOffset(margin + 300, y);
	            cs.showText("Presença"); cs.endText();
	            cs.beginText(); cs.newLineAtOffset(margin + 380, y);
	            cs.showText("Risco"); cs.endText();
	            y -= 5;

	            cs.moveTo(margin, y);
	            cs.lineTo(margin + width, y);
	            cs.stroke();
	            y -= 15;

	            cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
	            for (StudentReportResponse s : report.getStudents()) {
	                if (y < 60) break;
	                String name = s.getStudentName().length() > 25
	                        ? s.getStudentName().substring(0, 25) + "..." : s.getStudentName();

	                var att = attendance.stream()
	                        .filter(a -> a.getStudentId().equals(s.getStudentId()))
	                        .findFirst();
	                String attStr = att.map(a -> a.getAttendancePercentage() + "%").orElse("N/A");
	                boolean atRisk = s.getAverage() < 50.0
	                        || att.map(a -> a.getAttendancePercentage() < 75.0).orElse(false);

	                cs.beginText(); cs.newLineAtOffset(margin, y); cs.showText(name); cs.endText();
	                cs.beginText(); cs.newLineAtOffset(margin + 180, y);
	                cs.showText(s.getAverage() + "%"); cs.endText();
	                cs.beginText(); cs.newLineAtOffset(margin + 240, y);
	                cs.showText(String.valueOf(s.getTotalExams())); cs.endText();
	                cs.beginText(); cs.newLineAtOffset(margin + 300, y);
	                cs.showText(attStr); cs.endText();
	                cs.beginText(); cs.newLineAtOffset(margin + 380, y);
	                cs.showText(atRisk ? "⚠ SIM" : "OK"); cs.endText();
	                y -= 15;
	            }

	            cs.beginText();
	            cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE), 8);
	            cs.newLineAtOffset(margin, 30);
	            cs.showText("Gerado pelo ProfManager em " + LocalDate.now());
	            cs.endText();
	        }

	        ByteArrayOutputStream out = new ByteArrayOutputStream();
	        doc.save(out);
	        return out.toByteArray();

	    } catch (Exception e) {
	        throw new BusinessException("Erro ao gerar PDF: " + e.getMessage());
	    }
	}

}
