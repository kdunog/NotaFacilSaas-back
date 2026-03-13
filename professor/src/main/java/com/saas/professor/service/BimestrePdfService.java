package com.saas.professor.service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import com.saas.professor.dto.response.AnnualReportResponse;
import com.saas.professor.dto.response.BimestreAverageResponse;
import com.saas.professor.dto.response.BimestreReportResponse;
import com.saas.professor.dto.response.StudentAnnualReportResponse;
import com.saas.professor.dto.response.StudentBimestreReportResponse;
import com.saas.professor.exceptions.BusinessException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BimestrePdfService {

    private final BimestreService bimestreService;

    private static final DateTimeFormatter BR_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] exportBimestreReport(Long bimestreId, Long teacherId) {
        BimestreReportResponse report = bimestreService.getBimestreReport(bimestreId, teacherId);

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                float y = 780, margin = 45;
                float width = page.getMediaBox().getWidth() - 2 * margin;

                // Título
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 15);
                cs.newLineAtOffset(margin, y);
                cs.showText("Relatorio de Fechamento - " + report.getBimestreName());
                cs.endText();
                y -= 18;

                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
                cs.newLineAtOffset(margin, y);
                cs.showText(report.getClassRoomDescription()
                    + " | Ano: " + report.getSchoolYear()
                    + " | Emitido: " + LocalDate.now().format(BR_DATE));
                cs.endText();
                y -= 14;

                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
                cs.newLineAtOffset(margin, y);
                cs.showText("Media da turma: " + report.getClassAverage()
                    + "   |   Total de alunos: " + report.getTotalStudents());
                cs.endText();
                y -= 8;

                cs.moveTo(margin, y); cs.lineTo(margin + width, y); cs.stroke();
                y -= 16;

                // Cabeçalho tabela
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 9);
                float[] cols = {margin, margin+160, margin+220, margin+270, margin+320, margin+375};
                String[] headers = {"Aluno", "Media", "Presencas", "Faltas", "Freq%", "Situacao"};
                for (int i = 0; i < headers.length; i++) {
                    cs.beginText();
                    cs.newLineAtOffset(cols[i], y);
                    cs.showText(headers[i]);
                    cs.endText();
                }
                y -= 5;
                cs.moveTo(margin, y); cs.lineTo(margin + width, y); cs.stroke();
                y -= 13;

                // Dados
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);
                for (StudentBimestreReportResponse s : report.getStudents()) {
                    if (y < 60) break;
                    String name = s.getStudentName().length() > 22
                        ? s.getStudentName().substring(0, 22) + "." : s.getStudentName();

                    String[] row = {
                        name,
                        String.valueOf(s.getBimestreAverage()),
                        String.valueOf(s.getPresences()),
                        String.valueOf(s.getAbsences()),
                        s.getAttendancePercentage() + "%",
                        s.getSituation()
                    };
                    for (int i = 0; i < row.length; i++) {
                        cs.beginText();
                        cs.newLineAtOffset(cols[i], y);
                        cs.showText(row[i]);
                        cs.endText();
                    }
                    y -= 13;
                }

                // Rodapé
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE), 8);
                cs.newLineAtOffset(margin, 25);
                cs.showText("NotaFacil - " + report.getBimestreName()
                    + " - " + report.getClassRoomDescription());
                cs.endText();
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new BusinessException("Erro ao gerar PDF: " + e.getMessage());
        }
    }

    public byte[] exportAnnualReport(Long classRoomId, Integer schoolYear, Long teacherId) {
        AnnualReportResponse report = bimestreService.getAnnualReport(
            classRoomId, schoolYear, teacherId);

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                float y = 780, margin = 45;
                float width = page.getMediaBox().getWidth() - 2 * margin;

                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 15);
                cs.newLineAtOffset(margin, y);
                cs.showText("Relatorio Anual - " + report.getSchoolYear());
                cs.endText();
                y -= 18;

                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
                cs.newLineAtOffset(margin, y);
                cs.showText(report.getClassRoomDescription()
                    + " | Media anual da turma: " + report.getClassAnnualAverage()
                    + " | Emitido: " + LocalDate.now().format(BR_DATE));
                cs.endText();
                y -= 8;

                cs.moveTo(margin, y); cs.lineTo(margin + width, y); cs.stroke();
                y -= 16;

                // Cabeçalho
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 9);
                cs.beginText(); cs.newLineAtOffset(margin, y);
                cs.showText("Aluno"); cs.endText();
                cs.beginText(); cs.newLineAtOffset(margin + 150, y);
                cs.showText("1 Bim"); cs.endText();
                cs.beginText(); cs.newLineAtOffset(margin + 210, y);
                cs.showText("2 Bim"); cs.endText();
                cs.beginText(); cs.newLineAtOffset(margin + 270, y);
                cs.showText("3 Bim"); cs.endText();
                cs.beginText(); cs.newLineAtOffset(margin + 330, y);
                cs.showText("4 Bim"); cs.endText();
                cs.beginText(); cs.newLineAtOffset(margin + 390, y);
                cs.showText("Media"); cs.endText();
                cs.beginText(); cs.newLineAtOffset(margin + 440, y);
                cs.showText("Freq%"); cs.endText();
                cs.beginText(); cs.newLineAtOffset(margin + 485, y);
                cs.showText("Situacao"); cs.endText();
                y -= 5;
                cs.moveTo(margin, y); cs.lineTo(margin + width, y); cs.stroke();
                y -= 13;

                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);
                for (StudentAnnualReportResponse s : report.getStudents()) {
                    if (y < 60) break;
                    String name = s.getStudentName().length() > 20
                        ? s.getStudentName().substring(0, 20) + "." : s.getStudentName();

                    cs.beginText(); cs.newLineAtOffset(margin, y);
                    cs.showText(name); cs.endText();

                    List<BimestreAverageResponse> avgs = s.getBimestreAverages();
                    float[] bCols = {margin+150, margin+210, margin+270, margin+330};
                    for (int i = 0; i < 4; i++) {
                        String val = i < avgs.size() ? String.valueOf(avgs.get(i).getAverage()) : "-";
                        cs.beginText(); cs.newLineAtOffset(bCols[i], y);
                        cs.showText(val); cs.endText();
                    }

                    cs.beginText(); cs.newLineAtOffset(margin + 390, y);
                    cs.showText(String.valueOf(s.getAnnualAverage())); cs.endText();
                    cs.beginText(); cs.newLineAtOffset(margin + 440, y);
                    cs.showText(s.getAnnualAttendancePercentage() + "%"); cs.endText();
                    cs.beginText(); cs.newLineAtOffset(margin + 485, y);
                    cs.showText(s.getFinalSituation()); cs.endText();
                    y -= 13;
                }

                // Rodapé
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE), 8);
                cs.newLineAtOffset(margin, 25);
                cs.showText("NotaFacil - Relatorio Anual " + report.getSchoolYear()
                    + " - " + report.getClassRoomDescription());
                cs.endText();
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new BusinessException("Erro ao gerar PDF anual: " + e.getMessage());
        }
    }
}