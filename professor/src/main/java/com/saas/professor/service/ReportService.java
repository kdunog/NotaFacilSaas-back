package com.saas.professor.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saas.professor.dto.response.AtRiskStudentResponse;
import com.saas.professor.dto.response.AttendanceVsGradeResponse;
import com.saas.professor.dto.response.ClassRoomReportResponse;
import com.saas.professor.dto.response.GradeResponse;
import com.saas.professor.dto.response.StudentEvolutionResponse;
import com.saas.professor.dto.response.StudentReportResponse;
import com.saas.professor.entity.Grade;
import com.saas.professor.entity.Student;
import com.saas.professor.entity.Teacher;
import com.saas.professor.enums.AttendanceStatus;
import com.saas.professor.repository.AttendanceRepository;
import com.saas.professor.repository.GradeRepository;
import com.saas.professor.repository.StudentClassRoomRepository;

@Service
public class ReportService {
	
	private final GradeRepository gradeRepository;
	private final AttendanceRepository attendanceRepository;
	private final StudentService studentService;
	private final ClassRoomService classRoomService;
	private final StudentClassRoomRepository studentClassRoomRepository;
	
	public ReportService(GradeRepository gradeRepository, AttendanceRepository attendanceRepository,
			StudentService studentService, ClassRoomService classRoomService,
			StudentClassRoomRepository studentClassRoomRepository) {
		this.gradeRepository = gradeRepository;
		this.attendanceRepository = attendanceRepository;
		this.studentService = studentService;
		this.classRoomService = classRoomService;
		this.studentClassRoomRepository = studentClassRoomRepository;
	}

	// Relatório completo do aluno: notas + evolução + frequência
	@Transactional(readOnly = true)
	public StudentReportResponse getStudentReport(Long studentId, Long teacherId) {
	    Student student = studentService.getEntityOrThrow(studentId, teacherId);
	    List<Grade> grades = gradeRepository.findByStudentId(studentId);

	    double weightedSum = 0.0;
	    double totalWeight = 0.0;
	    for (Grade g : grades) {
	        double weight = g.getExam().getWeight() != null ? g.getExam().getWeight() : 1.0;
	        weightedSum += (g.getScore() / g.getExam().getTotalPoints()) * 100 * weight;
	        totalWeight += weight;
	    }
	    double average = totalWeight > 0 ? Math.round((weightedSum / totalWeight) * 100.0) / 100.0 : 0.0;

	    List<GradeResponse> gradeResponses = grades.stream()
	            .map(g -> new GradeResponse(g.getId(), g.getStudent().getId(),
	                    g.getStudent().getName(), g.getExam().getId(),
	                    g.getExam().getTitle(), g.getScore(),
	                    g.getExam().getTotalPoints(), g.getObservation()))
	            .collect(Collectors.toList());

	    return new StudentReportResponse(student.getId(), student.getName(),
	            average, grades.size(), gradeResponses);
	}

	// Evolução do aluno ao longo do tempo (por prova)
	@Transactional(readOnly = true)
	public List<StudentEvolutionResponse> getStudentEvolution(Long studentId, Long teacherId) {
	    studentService.getEntityOrThrow(studentId, teacherId);
	    List<Grade> grades = gradeRepository.findByStudentIdOrderByExamDateAsc(studentId);

	    return grades.stream()
	            .map(g -> new StudentEvolutionResponse(
	                    g.getExam().getId(),
	                    g.getExam().getTitle(),
	                    g.getExam().getExamDate(),
	                    g.getScore(),
	                    g.getExam().getTotalPoints(),
	                    g.getExam().getWeight()))
	            .collect(Collectors.toList());
	}

	// Relatório da turma: média, frequência, alunos em risco
	@Transactional(readOnly = true)
	public ClassRoomReportResponse getClassRoomReport(Long classRoomId, Long teacherId) {
	    var classRoom = classRoomService.findEntityById(classRoomId, teacherId);

	    List<Student> students = studentClassRoomRepository
	            .findByClassRoom_IdAndLeftAtIsNull(classRoomId)
	            .stream().map(sc -> sc.getStudent()).collect(Collectors.toList());

	    long totalDays = attendanceRepository.countTotalClassDays(classRoomId);

	    List<StudentReportResponse> studentReports = new ArrayList<>();
	    double sumAverages = 0.0;
	    int atRiskCount = 0;

	    for (Student s : students) {
	        List<Grade> grades = gradeRepository.findByStudentId(s.getId());

	        double weightedSum = 0.0;
	        double totalWeight = 0.0;
	        for (Grade g : grades) {
	            double weight = g.getExam().getWeight() != null ? g.getExam().getWeight() : 1.0;
	            weightedSum += (g.getScore() / g.getExam().getTotalPoints()) * 100 * weight;
	            totalWeight += weight;
	        }
	        double avg = totalWeight > 0 ? Math.round((weightedSum / totalWeight) * 100.0) / 100.0 : 0.0;
	        sumAverages += avg;

	        long presences = attendanceRepository.countByClassRoomIdAndStudentIdAndStatus(
	                classRoomId, s.getId(), AttendanceStatus.PRESENT);
	        long lates = attendanceRepository.countByClassRoomIdAndStudentIdAndStatus(
	                classRoomId, s.getId(), AttendanceStatus.JUSTIFIED);
	        double attendance = totalDays > 0
	                ? Math.round(((double)(presences + lates) / totalDays) * 10000.0) / 100.0
	                : 0.0;

	        if (avg < 50.0 || attendance < 75.0) atRiskCount++;

	        List<GradeResponse> gradeResponses = grades.stream()
	                .map(g -> new GradeResponse(g.getId(), s.getId(), s.getName(),
	                        g.getExam().getId(), g.getExam().getTitle(),
	                        g.getScore(), g.getExam().getTotalPoints(), g.getObservation()))
	                .collect(Collectors.toList());

	        studentReports.add(new StudentReportResponse(s.getId(), s.getName(),
	                avg, grades.size(), gradeResponses));
	    }

	    double classAverage = students.size() > 0
	            ? Math.round((sumAverages / students.size()) * 100.0) / 100.0
	            : 0.0;

	    String desc = classRoom.getEducationLevel() + " "
	            + classRoom.getGradeNumber() + "°"
	            + classRoom.getSectionLetter() + " - "
	            + classRoom.getSubject().getName()
	            + " (" + classRoom.getSchoolYear() + ")";

	    return new ClassRoomReportResponse(classRoomId, desc, classAverage,
	            students.size(), atRiskCount, studentReports);
	}

	// Alunos em risco: nota < 50 OU frequência < 75%
	@Transactional(readOnly = true)
	public List<AtRiskStudentResponse> getAtRiskStudents(Long classRoomId, Long teacherId) {
	    classRoomService.findEntityById(classRoomId, teacherId);

	    List<Student> students = studentClassRoomRepository
	            .findByClassRoom_IdAndLeftAtIsNull(classRoomId)
	            .stream().map(sc -> sc.getStudent()).collect(Collectors.toList());

	    long totalDays = attendanceRepository.countTotalClassDays(classRoomId);
	    List<AtRiskStudentResponse> atRisk = new ArrayList<>();

	    for (Student s : students) {
	        List<Grade> grades = gradeRepository.findByStudentId(s.getId());

	        double weightedSum = 0.0;
	        double totalWeight = 0.0;
	        for (Grade g : grades) {
	            double weight = g.getExam().getWeight() != null ? g.getExam().getWeight() : 1.0;
	            weightedSum += (g.getScore() / g.getExam().getTotalPoints()) * 100 * weight;
	            totalWeight += weight;
	        }
	        double avg = totalWeight > 0 ? Math.round((weightedSum / totalWeight) * 100.0) / 100.0 : 0.0;

	        long presences = attendanceRepository.countByClassRoomIdAndStudentIdAndStatus(
	                classRoomId, s.getId(), AttendanceStatus.PRESENT);
	        long lates = attendanceRepository.countByClassRoomIdAndStudentIdAndStatus(
	                classRoomId, s.getId(), AttendanceStatus.JUSTIFIED);
	        double attendance = totalDays > 0
	                ? Math.round(((double)(presences + lates) / totalDays) * 10000.0) / 100.0
	                : 0.0;

	        List<String> reasons = new ArrayList<>();
	        if (avg < 50.0) reasons.add("Média abaixo de 50%");
	        if (attendance < 75.0) reasons.add("Frequência abaixo de 75%");

	        if (!reasons.isEmpty()) {
	            atRisk.add(new AtRiskStudentResponse(s.getId(), s.getName(),
	                    avg, attendance, String.join(" | ", reasons)));
	        }
	    }

	    return atRisk;
	}

	// Frequência x Nota (correlação por aluno)
	@Transactional(readOnly = true)
	public List<AttendanceVsGradeResponse> getAttendanceVsGrade(Long classRoomId, Long teacherId) {
	    classRoomService.findEntityById(classRoomId, teacherId);

	    List<Student> students = studentClassRoomRepository
	            .findByClassRoom_IdAndLeftAtIsNull(classRoomId)
	            .stream().map(sc -> sc.getStudent()).collect(Collectors.toList());

	    long totalDays = attendanceRepository.countTotalClassDays(classRoomId);

	    return students.stream().map(s -> {
	        List<Grade> grades = gradeRepository.findByStudentId(s.getId());
	        double weightedSum = 0.0;
	        double totalWeight = 0.0;
	        for (Grade g : grades) {
	            double weight = g.getExam().getWeight() != null ? g.getExam().getWeight() : 1.0;
	            weightedSum += (g.getScore() / g.getExam().getTotalPoints()) * 100 * weight;
	            totalWeight += weight;
	        }
	        double avg = totalWeight > 0 ? Math.round((weightedSum / totalWeight) * 100.0) / 100.0 : 0.0;

	        long presences = attendanceRepository.countByClassRoomIdAndStudentIdAndStatus(
	                classRoomId, s.getId(), AttendanceStatus.PRESENT);
	        long lates = attendanceRepository.countByClassRoomIdAndStudentIdAndStatus(
	                classRoomId, s.getId(), AttendanceStatus.JUSTIFIED);
	        double attendance = totalDays > 0
	                ? Math.round(((double)(presences + lates) / totalDays) * 10000.0) / 100.0
	                : 0.0;

	        return new AttendanceVsGradeResponse(s.getId(), s.getName(), attendance, avg);
	    }).collect(Collectors.toList());
	}

}
