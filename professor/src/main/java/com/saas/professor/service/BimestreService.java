package com.saas.professor.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saas.professor.dto.request.CreateBimestreRequest;
import com.saas.professor.dto.response.AnnualReportResponse;
import com.saas.professor.dto.response.BimestreAverageResponse;
import com.saas.professor.dto.response.BimestreReportResponse;
import com.saas.professor.dto.response.BimestreResponse;
import com.saas.professor.dto.response.ExamResponse;
import com.saas.professor.dto.response.GradeResponse;
import com.saas.professor.dto.response.StudentAnnualReportResponse;
import com.saas.professor.dto.response.StudentBimestreReportResponse;
import com.saas.professor.entity.Bimestre;
import com.saas.professor.entity.BimestreExam;
import com.saas.professor.entity.ClassRoom;
import com.saas.professor.entity.Exam;
import com.saas.professor.entity.Grade;
import com.saas.professor.entity.Student;
import com.saas.professor.entity.StudentClassRoom;
import com.saas.professor.enums.AttendanceStatus;
import com.saas.professor.exceptions.BusinessException;
import com.saas.professor.exceptions.ResourceNotFoundException;
import com.saas.professor.repository.AttendanceRepository;
import com.saas.professor.repository.BimestreRepository;
import com.saas.professor.repository.GradeRepository;
import com.saas.professor.repository.StudentClassRoomRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BimestreService {

    private final BimestreRepository bimestreRepository;
    private final ClassRoomService classRoomService;
    private final ExamService examService;
    private final GradeRepository gradeRepository;
    private final AttendanceRepository attendanceRepository;
    private final StudentClassRoomRepository studentClassRoomRepository;

    // Converte média de 0-100 para 0-10 com 2 casas decimais
    private double toScale10(double avg) {
        return Math.round((avg / 10.0) * 100.0) / 100.0;
    }

    @Transactional
    public BimestreResponse create(CreateBimestreRequest req, Long teacherId) {
        ClassRoom classRoom = classRoomService.findEntityById(req.getClassRoomId(), teacherId);

        if (bimestreRepository.existsByClassRoom_IdAndTeacherIdAndNumber(
                req.getClassRoomId(), teacherId, req.getNumber()))
            throw new BusinessException("Já existe o " + req.getNumber()
                + "º Bimestre para esta turma neste ano");

        Bimestre bimestre = new Bimestre(teacherId, classRoom, req.getName(),
                req.getNumber(), req.getSchoolYear(),
                req.getStartDate(), req.getEndDate());

        if (req.getExamIds() != null) {
            for (Long examId : req.getExamIds()) {
                Exam exam = examService.findEntityById(examId, teacherId);
                bimestre.getBimestreExams().add(new BimestreExam(bimestre, exam));
            }
        }

        return toResponse(bimestreRepository.save(bimestre));
    }

    @Transactional
    public BimestreResponse addExam(Long bimestreId, Long examId, Long teacherId) {
        Bimestre bimestre = getOrThrow(bimestreId, teacherId);
        if (bimestre.getClosed())
            throw new BusinessException("Bimestre já está fechado");
        Exam exam = examService.findEntityById(examId, teacherId);
        bimestre.getBimestreExams().add(new BimestreExam(bimestre, exam));
        return toResponse(bimestreRepository.save(bimestre));
    }

    @Transactional
    public BimestreResponse close(Long bimestreId, Long teacherId) {
        Bimestre bimestre = getOrThrow(bimestreId, teacherId);
        if (bimestre.getClosed())
            throw new BusinessException("Bimestre já está fechado");
        bimestre.setClosed(true);
        return toResponse(bimestreRepository.save(bimestre));
    }

    @Transactional(readOnly = true)
    public List<BimestreResponse> findByClassRoom(Long classRoomId, Long teacherId) {
        return bimestreRepository
            .findByClassRoom_IdAndTeacherIdOrderByNumberAsc(classRoomId, teacherId)
            .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BimestreReportResponse getBimestreReport(Long bimestreId, Long teacherId) {
        Bimestre bimestre = getOrThrow(bimestreId, teacherId);
        ClassRoom classRoom = bimestre.getClassRoom();

        List<Student> students = studentClassRoomRepository
            .findByClassRoom_IdAndLeftAtIsNull(classRoom.getId())
            .stream().map(StudentClassRoom::getStudent).collect(Collectors.toList());

        List<Exam> exams = bimestre.getBimestreExams().stream()
            .map(BimestreExam::getExam).collect(Collectors.toList());

        long totalDays = attendanceRepository.countTotalClassDays(classRoom.getId());
        List<StudentBimestreReportResponse> studentReports = new ArrayList<>();
        double sumAverages = 0.0;

        for (Student s : students) {
            List<GradeResponse> grades = new ArrayList<>();
            double weightedSum = 0.0;
            double totalWeight = 0.0;

            for (Exam exam : exams) {
                Optional<Grade> gradeOpt = gradeRepository
                    .findByStudentIdAndExamId(s.getId(), exam.getId());
                if (gradeOpt.isPresent()) {
                    Grade g = gradeOpt.get();
                    grades.add(new GradeResponse(g.getId(), s.getId(), s.getName(),
                        exam.getId(), exam.getTitle(), g.getScore(),
                        exam.getTotalPoints(), g.getObservation()));
                    double w = exam.getWeight() != null ? exam.getWeight() : 1.0;
                    weightedSum += (g.getScore() / exam.getTotalPoints()) * 100 * w;
                    totalWeight += w;
                }
            }

            // Média em 0-100 internamente, converte para 0-10 ao passar para o DTO
            double avg100 = totalWeight > 0
                ? Math.round((weightedSum / totalWeight) * 100.0) / 100.0 : 0.0;
            sumAverages += avg100;

            long presences = attendanceRepository.countByClassRoomIdAndStudentIdAndStatus(
                classRoom.getId(), s.getId(), AttendanceStatus.PRESENT);
            long absences = attendanceRepository.countByClassRoomIdAndStudentIdAndStatus(
                classRoom.getId(), s.getId(), AttendanceStatus.ABSENT);
            long lates = attendanceRepository.countByClassRoomIdAndStudentIdAndStatus(
                classRoom.getId(), s.getId(), AttendanceStatus.JUSTIFIED);
            double attPct = totalDays > 0
                ? Math.round(((double)(presences + lates) / totalDays) * 10000.0) / 100.0 : 0.0;

            // StudentBimestreReportResponse converte avg100 → 0-10 internamente
            studentReports.add(new StudentBimestreReportResponse(
                s.getId(), s.getName(), avg100, presences, absences, lates, attPct,
                grades, classRoom.getMinimumGrade(),
                Boolean.TRUE.equals(classRoom.getAllowsBimestreRecovery())));
        }

        // Média da turma também em 0-10
        double classAverage = students.size() > 0
            ? toScale10(Math.round((sumAverages / students.size()) * 100.0) / 100.0) : 0.0;

        String desc = classRoom.getEducationLevel() + " "
            + classRoom.getGradeNumber() + "º"
            + classRoom.getSectionLetter() + " - "
            + classRoom.getSubject().getName();

        return new BimestreReportResponse(bimestre.getId(), bimestre.getName(),
            bimestre.getNumber(), bimestre.getSchoolYear(), desc,
            classAverage, students.size(), studentReports);
    }

    @Transactional(readOnly = true)
    public AnnualReportResponse getAnnualReport(Long classRoomId,
                                                  Integer schoolYear, Long teacherId) {
        ClassRoom classRoom = classRoomService.findEntityById(classRoomId, teacherId);

        List<Bimestre> bimestres = bimestreRepository
            .findByClassRoom_IdAndTeacherIdAndSchoolYearOrderByNumberAsc(
                classRoomId, teacherId, schoolYear);

        if (bimestres.isEmpty())
            throw new BusinessException("Nenhum bimestre encontrado para este ano");

        List<Student> students = studentClassRoomRepository
            .findByClassRoom_IdAndLeftAtIsNull(classRoomId)
            .stream().map(StudentClassRoom::getStudent).collect(Collectors.toList());

        long totalDays = attendanceRepository.countTotalClassDays(classRoomId);
        List<StudentAnnualReportResponse> studentReports = new ArrayList<>();
        double sumAnnualAverages = 0.0;

        for (Student s : students) {
            List<BimestreAverageResponse> bimestreAvgs = new ArrayList<>();
            double sumBimestreAvg = 0.0;

            for (Bimestre b : bimestres) {
                List<Exam> exams = b.getBimestreExams().stream()
                    .map(BimestreExam::getExam).collect(Collectors.toList());

                double ws = 0.0, tw = 0.0;
                for (Exam exam : exams) {
                    Optional<Grade> g = gradeRepository
                        .findByStudentIdAndExamId(s.getId(), exam.getId());
                    if (g.isPresent()) {
                        double w = exam.getWeight() != null ? exam.getWeight() : 1.0;
                        ws += (g.get().getScore() / exam.getTotalPoints()) * 100 * w;
                        tw += w;
                    }
                }
                // bAvg em 0-100, converte para 0-10 para exibição
                double bAvg100 = tw > 0 ? Math.round((ws / tw) * 100.0) / 100.0 : 0.0;
                double bAvg10 = toScale10(bAvg100);
                sumBimestreAvg += bAvg100; // soma em 0-100 para média anual correta

                long pres = attendanceRepository.countByClassRoomIdAndStudentIdAndStatus(
                    classRoomId, s.getId(), AttendanceStatus.PRESENT);
                long late = attendanceRepository.countByClassRoomIdAndStudentIdAndStatus(
                    classRoomId, s.getId(), AttendanceStatus.JUSTIFIED);
                double attPct = totalDays > 0
                    ? Math.round(((double)(pres + late) / totalDays) * 10000.0) / 100.0 : 0.0;

                // Exibe 0-10 no BimestreAverageResponse
                bimestreAvgs.add(new BimestreAverageResponse(
                    b.getName(), b.getNumber(), bAvg10, attPct));
            }

            // Média anual em 0-100, passa para DTO que converte para 0-10
            double annualAvg100 = bimestres.size() > 0
                ? Math.round((sumBimestreAvg / bimestres.size()) * 100.0) / 100.0 : 0.0;
            sumAnnualAverages += annualAvg100;

            long presences = attendanceRepository.countByClassRoomIdAndStudentIdAndStatus(
                classRoomId, s.getId(), AttendanceStatus.PRESENT);
            long absences = attendanceRepository.countByClassRoomIdAndStudentIdAndStatus(
                classRoomId, s.getId(), AttendanceStatus.ABSENT);
            long lates = attendanceRepository.countByClassRoomIdAndStudentIdAndStatus(
                classRoomId, s.getId(), AttendanceStatus.JUSTIFIED);
            double annualAtt = totalDays > 0
                ? Math.round(((double)(presences + lates) / totalDays) * 10000.0) / 100.0 : 0.0;

            // StudentAnnualReportResponse converte annualAvg100 → 0-10 internamente
            studentReports.add(new StudentAnnualReportResponse(
                s.getId(), s.getName(), annualAvg100,
                presences, absences, lates, annualAtt, bimestreAvgs,
                classRoom.getMinimumGrade()));
        }

        // Média anual da turma em 0-10
        double classAnnualAvg = students.size() > 0
            ? toScale10(Math.round((sumAnnualAverages / students.size()) * 100.0) / 100.0) : 0.0;

        String desc = classRoom.getEducationLevel() + " "
            + classRoom.getGradeNumber() + "º"
            + classRoom.getSectionLetter() + " - "
            + classRoom.getSubject().getName();

        return new AnnualReportResponse(classRoomId, desc, schoolYear,
            classAnnualAvg, students.size(), studentReports);
    }

    private Bimestre getOrThrow(Long id, Long teacherId) {
        return bimestreRepository.findByIdAndTeacherId(id, teacherId)
            .orElseThrow(() -> new ResourceNotFoundException("Bimestre não encontrado"));
    }

    private BimestreResponse toResponse(Bimestre b) {
        List<ExamResponse> exams = b.getBimestreExams().stream()
            .map(be -> new ExamResponse(be.getExam().getId(), be.getExam().getClassRoom().getId(),
                be.getExam().getTitle(), be.getExam().getExamDate(),
                be.getExam().getTotalPoints(), be.getExam().getWeight(),
                be.getExam().getStatus(), be.getExam().getAppliedAt(),
                be.getExam().getCreatedAt()))
            .collect(Collectors.toList());
        return new BimestreResponse(b.getId(), b.getClassRoom().getId(), b.getName(),
            b.getNumber(), b.getSchoolYear(), b.getStartDate(), b.getEndDate(),
            b.getClosed(), exams);
    }
}