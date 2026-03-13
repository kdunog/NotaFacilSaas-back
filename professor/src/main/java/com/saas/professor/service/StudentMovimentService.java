package com.saas.professor.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saas.professor.dto.request.MoveStudentRequest;
import com.saas.professor.dto.response.StudentClassRoomResponse;
import com.saas.professor.entity.ClassRoom;
import com.saas.professor.entity.Student;
import com.saas.professor.entity.StudentClassRoom;
import com.saas.professor.entity.Teacher;
import com.saas.professor.enums.ClassRoomStatus;
import com.saas.professor.exceptions.BusinessException;
import com.saas.professor.repository.StudentClassRoomRepository;

import lombok.RequiredArgsConstructor;

@Service
public class StudentMovimentService {

    private final StudentClassRoomRepository studentClassRoomRepository;
    private final StudentService studentService;
    private final ClassRoomService classRoomService;
    
	public StudentMovimentService(StudentClassRoomRepository studentClassRoomRepository, StudentService studentService,
			ClassRoomService classRoomService) {
		this.studentClassRoomRepository = studentClassRoomRepository;
		this.studentService = studentService;
		this.classRoomService = classRoomService;
	}

	@Transactional
	public StudentClassRoomResponse enroll(Long studentId, Long classRoomId, Long teacherId) {
	    Student student = studentService.getEntityOrThrow(studentId, teacherId);
	    ClassRoom classRoom = classRoomService.findEntityById(classRoomId, teacherId);
	    assertActive(classRoom);

	    // Verifica só se já está NESSA turma específica, não em qualquer turma
	    boolean jaMatriculadoNessaTurma = studentClassRoomRepository
	            .findByClassRoom_IdAndLeftAtIsNull(classRoomId)
	            .stream()
	            .anyMatch(sc -> sc.getStudent().getId().equals(studentId));

	    if (jaMatriculadoNessaTurma) {
	        throw new BusinessException("Aluno já matriculado nesta turma.");
	    }

	    StudentClassRoom enrollment = new StudentClassRoom(student, classRoom);
	    return toResponse(studentClassRoomRepository.save(enrollment));
	}

    @Transactional
    public List<StudentClassRoomResponse> move(MoveStudentRequest req, Long teacherId) {
        ClassRoom target = classRoomService.findEntityById(req.getTargetClassRoomId(), teacherId);
        assertActive(target);

        List<StudentClassRoomResponse> results = new ArrayList<>();
        for (Long studentId : req.getStudentIds()) {
            results.add(moveOne(studentId, target, teacherId));
        }
        return results;
    }

    @Transactional(readOnly = true)
    public List<StudentClassRoomResponse> getHistory(Long studentId) {
        return studentClassRoomRepository.findByStudent_IdOrderByEnteredAtDesc(studentId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StudentClassRoomResponse> getStudentsInClass(Long classRoomId) {
        return studentClassRoomRepository.findByClassRoom_IdAndLeftAtIsNull(classRoomId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void unenroll(Long studentId, Long classRoomId, Long teacherId) {
        classRoomService.findEntityById(classRoomId, teacherId); // valida que a turma é do professor
        StudentClassRoom sc = studentClassRoomRepository
            .findByClassRoom_IdAndLeftAtIsNull(classRoomId)
            .stream()
            .filter(e -> e.getStudent().getId().equals(studentId))
            .findFirst()
            .orElseThrow(() -> new BusinessException("Aluno não matriculado nesta turma"));
        sc.setLeftAt(LocalDateTime.now());
        studentClassRoomRepository.save(sc);
    }

    private StudentClassRoomResponse moveOne(Long studentId, ClassRoom target, Long teacherId) {
        studentClassRoomRepository
                .findActiveByStudentIdAndTeacherId(studentId, teacherId)
                .ifPresent(current -> {
                    current.setLeftAt(LocalDateTime.now());
                    studentClassRoomRepository.save(current);
                });

        Student student = studentService.getEntityOrThrow(studentId, teacherId);
        StudentClassRoom newLink = new StudentClassRoom(student, target);
        return toResponse(studentClassRoomRepository.save(newLink));
    }

    private void assertActive(ClassRoom cr) {
        if (cr.getStatus() != ClassRoomStatus.ACTIVE)
            throw new BusinessException("Turma destino não está ativa");
    }

    private String describe(ClassRoom c) {
        return c.getEducationLevel() + " " + c.getGradeNumber() + "°"
                + c.getSectionLetter() + " - " + c.getSubject().getName()
                + " (" + c.getSchoolYear() + ")";
    }

    private StudentClassRoomResponse toResponse(StudentClassRoom sc) {
        return new StudentClassRoomResponse(
                sc.getId(),
                sc.getStudent().getId(),
                sc.getStudent().getName(),
                sc.getClassRoom().getId(),
                describe(sc.getClassRoom()),
                sc.getEnteredAt(),
                sc.getLeftAt()
        );
    }
}