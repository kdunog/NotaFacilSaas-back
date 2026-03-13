package com.saas.professor.service;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import com.saas.professor.entity.Exam;
import com.saas.professor.entity.Question;
import com.saas.professor.entity.QuestionOption;
import com.saas.professor.entity.Student;
import com.saas.professor.entity.StudentClassRoom;
import com.saas.professor.entity.Teacher;
import com.saas.professor.enums.QuestionType;
import com.saas.professor.exceptions.BusinessException;
import com.saas.professor.repository.QuestionRepository;
import com.saas.professor.repository.StudentClassRoomRepository;
import com.saas.professor.repository.TeacherRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExamPdfService {

    private final QuestionRepository questionRepository;
    private final ExamService examService;
    private final StudentClassRoomRepository studentClassRoomRepository;
    private final TeacherRepository teacherRepository;

    public byte[] generateExamPdf(Long examId, Long teacherId) {
        Exam exam = examService.findEntityById(examId, teacherId);
        List<Question> questions = questionRepository.findByExamIdOrderByOrderIndexAsc(examId);

        if (questions.isEmpty())
            throw new BusinessException("A prova não tem questões cadastradas");

        List<Student> students = studentClassRoomRepository
            .findByClassRoom_IdAndLeftAtIsNull(exam.getClassRoom().getId())
            .stream().map(StudentClassRoom::getStudent).toList();

        if (students.isEmpty())
            throw new BusinessException("Nenhum aluno matriculado na turma");

        Teacher teacher = teacherRepository.findById(teacherId)
            .orElseThrow(() -> new BusinessException("Professor não encontrado"));

        String schoolName = exam.getClassRoom().getSchoolName();
        String teacherName = teacher.getName();

        List<Question> versionA = new ArrayList<>(questions);
        List<Question> versionB = new ArrayList<>(questions);
        do { Collections.shuffle(versionB); } while (versionB.equals(versionA) && questions.size() > 1);

        try (PDDocument doc = new PDDocument()) {

            for (int i = 0; i < students.size(); i++) {
                Student student = students.get(i);
                boolean isA = (i % 2 == 0);
                addExamPage(doc, exam, student, isA ? versionA : versionB,
                    isA ? "A" : "B", schoolName, teacherName);
            }

            if (students.size() % 2 != 0) {
                addExamPage(doc, exam, null, versionB, "B", schoolName, teacherName);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new BusinessException("Erro ao gerar PDF da prova: " + e.getMessage());
        }
    }

    private void addExamPage(PDDocument doc, Exam exam, Student student,
                              List<Question> questions, String versionLabel,
                              String schoolName, String teacherName) throws Exception {
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);

        float margin = 50;
        float pageWidth = page.getMediaBox().getWidth();
        float lineWidth = pageWidth - margin * 2;
        float y = 780;

        PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDType1Font fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        PDType1Font fontItalic = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);

        try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {

            // ESCOLA
            String escola = (schoolName != null && !schoolName.isBlank())
                ? schoolName : "_______________________________________________";
            cs.beginText();
            cs.setFont(fontBold, 11);
            cs.newLineAtOffset(margin, y);
            cs.showText("Escola: " + escola);
            cs.endText();
            y -= 18;

            // DISCIPLINA + VERSÃO
            cs.beginText();
            cs.setFont(fontBold, 11);
            cs.newLineAtOffset(margin, y);
            cs.showText(exam.getClassRoom().getSubject().getName().toUpperCase()
                + "   —   Versão " + versionLabel);
            cs.endText();
            y -= 18;

            // PROFESSOR
            cs.beginText();
            cs.setFont(fontRegular, 11);
            cs.newLineAtOffset(margin, y);
            cs.showText("Professor(a): " + teacherName);
            cs.endText();
            y -= 18;

            // ESTUDANTE
            String studentName = (student != null)
                ? student.getName() : "_______________________________________________";
            cs.beginText();
            cs.setFont(fontRegular, 11);
            cs.newLineAtOffset(margin, y);
            cs.showText("Estudante: " + studentName);
            cs.endText();
            y -= 18;

            // DATA / TURMA
            String classDesc = exam.getClassRoom().getGradeNumber() + "º "
                + exam.getClassRoom().getSectionLetter()
                + " — " + exam.getClassRoom().getSchoolYear();
            String dateStr = exam.getExamDate() != null
                ? exam.getExamDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : "___/___/______";
            cs.beginText();
            cs.setFont(fontRegular, 11);
            cs.newLineAtOffset(margin, y);
            cs.showText("Data: " + dateStr
                + "     Turma: " + classDesc
                + "     Nota: _______"
                + "     Valor: " + exam.getTotalPoints() + " pts");
            cs.endText();
            y -= 8;

            // Linha separadora
            cs.moveTo(margin, y);
            cs.lineTo(pageWidth - margin, y);
            cs.stroke();
            y -= 18;

            // QUESTÕES
            for (int i = 0; i < questions.size(); i++) {
                Question q = questions.get(i);
                if (y < 80) break;

                cs.beginText();
                cs.setFont(fontBold, 10);
                cs.newLineAtOffset(margin, y);
                cs.showText("Questão " + (i + 1) + " (" + q.getPoints() + " pts) — " + getTypeLabel(q.getType()));
                cs.endText();
                y -= 14;

                for (String line : wrapText(q.getStatement(), 85)) {
                    cs.beginText();
                    cs.setFont(fontRegular, 10);
                    cs.newLineAtOffset(margin + 10, y);
                    cs.showText(line);
                    cs.endText();
                    y -= 13;
                }

                if (!q.getOptions().isEmpty()) {
                    List<QuestionOption> opts = new ArrayList<>(q.getOptions());
                    Collections.shuffle(opts);
                    String[] letters = {"A", "B", "C", "D", "E"};
                    for (int j = 0; j < opts.size() && j < letters.length; j++) {
                        cs.beginText();
                        cs.setFont(fontRegular, 10);
                        cs.newLineAtOffset(margin + 20, y);
                        cs.showText(letters[j] + ") " + opts.get(j).getText());
                        cs.endText();
                        y -= 13;
                    }
                }

                if (q.getType() == QuestionType.ESSAY) {
                    for (int l = 0; l < 4; l++) {
                        cs.moveTo(margin + 10, y);
                        cs.lineTo(pageWidth - margin, y);
                        cs.stroke();
                        y -= 14;
                    }
                }
                if (q.getType() == QuestionType.FILL_IN_THE_BLANK) {
                    cs.moveTo(margin + 10, y);
                    cs.lineTo(margin + 200, y);
                    cs.stroke();
                    y -= 14;
                }

                y -= 8;
            }

            // Rodapé
            cs.beginText();
            cs.setFont(fontItalic, 8);
            cs.newLineAtOffset(margin, 25);
            cs.showText("NotaFácil — " + exam.getClassRoom().getSubject().getName() + " — Versão " + versionLabel);
            cs.endText();
        }
    }

    private String getTypeLabel(QuestionType type) {
        return switch (type) {
            case MULTIPLE_CHOICE -> "Múltipla Escolha";
            case ESSAY -> "Dissertativa";
            case TRUE_OR_FALSE -> "Verdadeiro ou Falso";
            case FILL_IN_THE_BLANK -> "Preencher Lacunas";
        };
    }

    private List<String> wrapText(String text, int maxChars) {
        List<String> lines = new ArrayList<>();
        while (text.length() > maxChars) {
            int idx = text.lastIndexOf(' ', maxChars);
            if (idx == -1) idx = maxChars;
            lines.add(text.substring(0, idx));
            text = text.substring(idx).trim();
        }
        lines.add(text);
        return lines;
    }
}