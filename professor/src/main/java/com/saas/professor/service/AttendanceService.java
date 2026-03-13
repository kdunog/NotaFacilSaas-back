package com.saas.professor.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saas.professor.dto.request.SaveAttendanceListRequest;
import com.saas.professor.dto.response.AttendanceResponse;
import com.saas.professor.dto.response.StudentAttendanceReportResponse;
import com.saas.professor.entity.Attendance;
import com.saas.professor.entity.ClassRoom;
import com.saas.professor.entity.Student;
import com.saas.professor.entity.Teacher;
import com.saas.professor.enums.AttendanceStatus;
import com.saas.professor.repository.AttendanceRepository;
import com.saas.professor.repository.StudentClassRoomRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AttendanceService {

	private final AttendanceRepository attendanceRepository;
	private final ClassRoomService classRoomService;
	private final StudentService studentService;
	private final StudentClassRoomRepository studentClassRoomRepository;
	

	// Salva chamada de um dia inteiro (cria ou atualiza cada registro)
	@Transactional
	public List<AttendanceResponse> saveAttendanceList(SaveAttendanceListRequest req,
	                                                    Long teacherId) {
	    ClassRoom classRoom = classRoomService.findEntityById(req.getClassRoomId(), teacherId);
	    List<AttendanceResponse> results = new ArrayList<>();

	    for (var item : req.getAttendances()) {
	        Student student = studentService.getEntityOrThrow(item.getStudentId(), teacherId);

	        Attendance attendance = attendanceRepository
	            .findByStudent_IdAndClassRoom_IdAndAttendanceDate(
	                student.getId(), classRoom.getId(), req.getDate())
	            .orElse(new Attendance(student, classRoom, item.getStatus(), req.getDate()));

	        attendance.setStatus(item.getStatus());
	        results.add(toResponse(attendanceRepository.save(attendance)));
	    }

	    return results;
	}

	// Marca todos como presentes de uma vez
	@Transactional
	public List<AttendanceResponse> markAllPresent(Long classRoomId, LocalDate date,
	                                                Long teacherId) {
	    ClassRoom classRoom = classRoomService.findEntityById(classRoomId, teacherId);

	    List<Student> students = studentClassRoomRepository
	        .findByClassRoom_IdAndLeftAtIsNull(classRoomId)
	        .stream()
	        .map(sc -> sc.getStudent())
	        .collect(Collectors.toList());

	    List<AttendanceResponse> results = new ArrayList<>();

	    for (Student student : students) {
	        Attendance attendance = attendanceRepository
	            .findByStudent_IdAndClassRoom_IdAndAttendanceDate(
	                student.getId(), classRoomId, date)
	            .orElse(new Attendance(student, classRoom, AttendanceStatus.PRESENT, date));

	        attendance.setStatus(AttendanceStatus.PRESENT);
	        results.add(toResponse(attendanceRepository.save(attendance)));
	    }

	    return results;
	}

	// Busca chamada de uma turma em uma data
	@Transactional(readOnly = true)
	public List<AttendanceResponse> findByClassRoomAndDate(Long classRoomId, LocalDate date,
	                                                        Long teacherId) {
	    classRoomService.findEntityById(classRoomId, teacherId); // Verifica se a turma existe e pertence ao professor);
	    return attendanceRepository.findByClassRoom_IdAndAttendanceDate(classRoomId, date)
	            .stream().map(this::toResponse).collect(Collectors.toList());
	}

	// Relatório de frequência por aluno na turma
	@Transactional(readOnly = true)
	public List<StudentAttendanceReportResponse> getAttendanceReport(Long classRoomId,
	                                                                  Long teacherId) {
	    classRoomService.findEntityById(classRoomId, teacherId);

	    long totalDays = attendanceRepository.countTotalClassDays(classRoomId);

	    List<Student> students = studentClassRoomRepository
	        .findByClassRoom_IdAndLeftAtIsNull(classRoomId)
	        .stream()
	        .map(sc -> sc.getStudent())
	        .collect(Collectors.toList());

	    return students.stream().map(s -> {
	        long presences = attendanceRepository.countByClassRoomIdAndStudentIdAndStatus(
	            classRoomId, s.getId(), AttendanceStatus.PRESENT);
	        long absences = attendanceRepository.countByClassRoomIdAndStudentIdAndStatus(
	            classRoomId, s.getId(), AttendanceStatus.ABSENT);
	        long lates = attendanceRepository.countByClassRoomIdAndStudentIdAndStatus(
	            classRoomId, s.getId(), AttendanceStatus.JUSTIFIED);

	        return new StudentAttendanceReportResponse(
	            s.getId(), s.getName(), totalDays, presences, absences, lates);
	    }).collect(Collectors.toList());
	}
	
	@Transactional(readOnly = true)
	public List<LocalDate> findClassDays(Long classRoomId, Long teacherId) {
	    classRoomService.findEntityById(classRoomId, teacherId);
	    return attendanceRepository.findDistinctDatesByClassRoomId(classRoomId);
	}

	private AttendanceResponse toResponse(Attendance a) {
	    return new AttendanceResponse(a.getId(), a.getStudent().getId(),
	            a.getStudent().getName(), a.getClassRoom().getId(),
	            a.getStatus(), a.getAttendanceDate());
	}
}
