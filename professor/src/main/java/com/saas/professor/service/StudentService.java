package com.saas.professor.service;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.saas.professor.dto.request.CreateStudentRequest;
import com.saas.professor.dto.response.StudentResponse;
import com.saas.professor.entity.Student;
import com.saas.professor.entity.StudentClassRoom;
import com.saas.professor.exceptions.BusinessException;
import com.saas.professor.repository.AttendanceRepository;
import com.saas.professor.repository.GradeRepository;
import com.saas.professor.repository.StudentAnswerRepository;
import com.saas.professor.repository.StudentClassRoomRepository;
import com.saas.professor.repository.StudentRepository;
import com.saas.professor.exceptions.ResourceNotFoundException;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final StudentClassRoomRepository studentClassRoomRepository;
    private final AttendanceRepository attendanceRepository;
    private final GradeRepository gradeRepository;
    private final StudentAnswerRepository studentAnswerRepository;

    public StudentService(StudentRepository studentRepository,
                          StudentClassRoomRepository studentClassRoomRepository,
                          AttendanceRepository attendanceRepository,
                          GradeRepository gradeRepository,
                          StudentAnswerRepository studentAnswerRepository) {
        this.studentRepository = studentRepository;
        this.studentClassRoomRepository = studentClassRoomRepository;
        this.attendanceRepository = attendanceRepository;
        this.gradeRepository = gradeRepository;
        this.studentAnswerRepository = studentAnswerRepository;
    }

    @Transactional
    public StudentResponse create(CreateStudentRequest request, Long teacherId) {
        Student student = new Student(teacherId, request.getName());
        return toResponse(studentRepository.save(student));
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> findAllByTeacher(Long teacherId) {
        return studentRepository.findByTeacherId(teacherId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> findActiveByTeacher(Long teacherId) {
        return studentRepository.findByTeacherIdAndActive(teacherId, true)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StudentResponse findById(Long id, Long teacherId) {
        return toResponse(getOrThrow(id, teacherId));
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> findWithoutActiveEnrollment(Long teacherId) {
        List<Long> enrolledIds = studentClassRoomRepository.findStudentIdsWithActiveEnrollment();
        return studentRepository.findByTeacherIdAndActive(teacherId, true)
                .stream()
                .filter(s -> !enrolledIds.contains(s.getId()))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deactivate(Long id, Long teacherId) {
        Student student = getOrThrow(id, teacherId);
        if (!student.getActive())
            throw new BusinessException("Aluno já está inativo");
        student.setActive(false);
        studentRepository.save(student);
    }

    @Transactional
    public StudentResponse reactivate(Long id, Long teacherId) {
        Student student = getOrThrow(id, teacherId);
        if (student.getActive())
            throw new BusinessException("Aluno já está ativo");
        student.setActive(true);
        return toResponse(studentRepository.save(student));
    }

    @Transactional
    public void delete(Long id, Long teacherId) {
        Student student = getOrThrow(id, teacherId);

        // 1. Deletar respostas do aluno
        studentAnswerRepository.deleteAll(
            studentAnswerRepository.findByStudentId(id)
        );

        // 2. Deletar grades do aluno
        gradeRepository.deleteAll(gradeRepository.findByStudentId(id));

        // 3. Deletar chamadas do aluno
        List<StudentClassRoom> enrollments = studentClassRoomRepository
            .findByStudent_IdOrderByEnteredAtDesc(id);
        for (StudentClassRoom sc : enrollments) {
            attendanceRepository.deleteAll(
                attendanceRepository.findByStudent_IdAndClassRoom_Id(id, sc.getClassRoom().getId())
            );
        }

        // 4. Deletar matrículas
        studentClassRoomRepository.deleteAll(enrollments);

        // 5. Deletar aluno
        studentRepository.delete(student);
    }

    public Student getEntityOrThrow(Long id, Long teacherId) {
        return getOrThrow(id, teacherId);
    }

    private Student getOrThrow(Long id, Long teacherId) {
        return studentRepository.findByIdAndTeacherId(id, teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Aluno não encontrado"));
    }

    private StudentResponse toResponse(Student s) {
        return new StudentResponse(
                s.getId(), s.getTeacherId(), s.getName(), s.getActive(), s.getCreatedAt()
        );
    }
}