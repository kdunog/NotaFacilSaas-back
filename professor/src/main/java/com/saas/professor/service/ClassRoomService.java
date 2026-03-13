package com.saas.professor.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saas.professor.dto.request.CreateClassRoomRequest;
import com.saas.professor.dto.request.UpdateClassRoomRequest;
import com.saas.professor.dto.response.ClassRoomResponse;
import com.saas.professor.entity.ClassRoom;
import com.saas.professor.entity.Exam;
import com.saas.professor.entity.Question;
import com.saas.professor.entity.Subject;
import com.saas.professor.enums.ClassRoomStatus;
import com.saas.professor.enums.EducationLevel;
import com.saas.professor.exceptions.BusinessException;
import com.saas.professor.exceptions.ResourceNotFoundException;
import com.saas.professor.repository.AttendanceRepository;
import com.saas.professor.repository.ClassRoomRepository;
import com.saas.professor.repository.ExamRepository;
import com.saas.professor.repository.GradeRepository;
import com.saas.professor.repository.QuestionRepository;
import com.saas.professor.repository.StudentAnswerRepository;
import com.saas.professor.repository.BimestreRepository;
import com.saas.professor.repository.StudentClassRoomRepository;

@Service
public class ClassRoomService {

    private final ClassRoomRepository classRoomRepository;
    private final SubjectService subjectService;
    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final GradeRepository gradeRepository;
    private final AttendanceRepository attendanceRepository;
    private final StudentClassRoomRepository studentClassRoomRepository;
    private final StudentAnswerRepository studentAnswerRepository;
    private final BimestreRepository bimestreRepository;

    public ClassRoomService(ClassRoomRepository classRoomRepository,
                            SubjectService subjectService,
                            ExamRepository examRepository,
                            QuestionRepository questionRepository,
                            GradeRepository gradeRepository,
                            AttendanceRepository attendanceRepository,
                            StudentClassRoomRepository studentClassRoomRepository,
                            StudentAnswerRepository studentAnswerRepository,
                          BimestreRepository bimestreRepository) {
        this.classRoomRepository = classRoomRepository;
        this.subjectService = subjectService;
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
        this.gradeRepository = gradeRepository;
        this.attendanceRepository = attendanceRepository;
        this.studentClassRoomRepository = studentClassRoomRepository;
        this.studentAnswerRepository = studentAnswerRepository;
        this.bimestreRepository = bimestreRepository;
    }

    @Transactional
    public ClassRoomResponse create(CreateClassRoomRequest req, Long teacherId) {
        validateGradeForLevel(req.getEducationLevel(), req.getGradeNumber());
        Subject subject = subjectService.findEntityById(req.getSubjectId(), teacherId);

        if (classRoomRepository.existsByTeacherIdAndSubject_IdAndSchoolYearAndGradeNumberAndEducationLevelAndSectionLetterAndStatus(
                teacherId, req.getSubjectId(), req.getSchoolYear(), req.getGradeNumber(),
                req.getEducationLevel(), req.getSectionLetter().toUpperCase(), ClassRoomStatus.ACTIVE))
            throw new BusinessException("Já existe uma turma ATIVA com esses dados");

        ClassRoom classRoom = new ClassRoom(
            teacherId, subject, req.getEducationLevel(),
            req.getGradeNumber(), req.getSectionLetter().toUpperCase(),
            req.getSchoolYear(), req.getMinimumGrade(),
            req.getAllowsBimestreRecovery(), req.getSchoolName()
        );

        return toResponse(classRoomRepository.save(classRoom));
    }

    @Transactional
    public ClassRoomResponse update(Long id, UpdateClassRoomRequest req, Long teacherId) {
        ClassRoom cr = getOrThrow(id, teacherId);
        Subject subject = subjectService.findEntityById(req.getSubjectId(), teacherId);
        validateGradeForLevel(req.getEducationLevel(), req.getGradeNumber());

        cr.setSubject(subject);
        cr.setEducationLevel(req.getEducationLevel());
        cr.setGradeNumber(req.getGradeNumber());
        cr.setSectionLetter(req.getSectionLetter().toUpperCase());
        cr.setSchoolYear(req.getSchoolYear());

        return toResponse(classRoomRepository.save(cr));
    }

    @Transactional(readOnly = true)
    public List<ClassRoomResponse> findAll(Long teacherId) {
        return classRoomRepository.findByTeacherId(teacherId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ClassRoomResponse> findActive(Long teacherId) {
        return classRoomRepository.findByTeacherIdAndStatus(teacherId, ClassRoomStatus.ACTIVE)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public ClassRoomResponse archive(Long id, Long teacherId) {
        ClassRoom cr = getOrThrow(id, teacherId);
        if (cr.getStatus() == ClassRoomStatus.ARCHIVED)
            throw new BusinessException("Turma já está arquivada");
        cr.setStatus(ClassRoomStatus.ARCHIVED);
        cr.setArchivedAt(LocalDateTime.now());
        return toResponse(classRoomRepository.save(cr));
    }

    @Transactional
    public ClassRoomResponse unarchive(Long id, Long teacherId) {
        ClassRoom cr = getOrThrow(id, teacherId);
        if (cr.getStatus() == ClassRoomStatus.ACTIVE)
            throw new BusinessException("Turma já está ativa");
        cr.setStatus(ClassRoomStatus.ACTIVE);
        cr.setArchivedAt(null);
        return toResponse(classRoomRepository.save(cr));
    }

    @Transactional
    public void delete(Long id, Long teacherId) {
        ClassRoom cr = getOrThrow(id, teacherId);

        // 1. Deletar chamadas
        attendanceRepository.deleteAll(attendanceRepository.findByClassRoom_Id(id));

        // 2. Deletar bimestres (BimestreExam deletado via cascade)
        bimestreRepository.deleteAll(
            bimestreRepository.findByClassRoom_IdAndTeacherIdOrderByNumberAsc(id, teacherId)
        );

        // 3. Para cada prova: deletar respostas, grades e questões
        List<Exam> exams = examRepository.findByClassRoom(cr);
        for (Exam exam : exams) {
            // 2a. Deletar respostas dos alunos
            studentAnswerRepository.deleteAll(
                studentAnswerRepository.findByQuestion_ExamId(exam.getId())
            );
            // 2b. Deletar grades
            gradeRepository.deleteAll(gradeRepository.findByExamId(exam.getId()));
            // 2c. Deletar questões (options deletadas via cascade na entidade)
            questionRepository.deleteAll(
                questionRepository.findByExamIdOrderByOrderIndexAsc(exam.getId())
            );
        }

        // 4. Deletar provas
        examRepository.deleteAll(exams);

        // 5. Deletar matrículas (ativas e inativas)
        studentClassRoomRepository.deleteAll(
            studentClassRoomRepository.findByClassRoom_Id(id)
        );

        // 6. Deletar turma
        classRoomRepository.delete(cr);
    }

    @Transactional
    public ClassRoomResponse duplicate(Long id, Long teacherId) {
        ClassRoom original = getOrThrow(id, teacherId);
        int nextYear = original.getSchoolYear() + 1;

        if (classRoomRepository.existsByTeacherIdAndSubject_IdAndSchoolYearAndGradeNumberAndEducationLevelAndSectionLetterAndStatus(
                teacherId, original.getSubject().getId(), nextYear,
                original.getGradeNumber(), original.getEducationLevel(),
                original.getSectionLetter(), ClassRoomStatus.ACTIVE))
            throw new BusinessException("Já existe uma turma ATIVA igual para " + nextYear);

        ClassRoom nova = new ClassRoom(
            teacherId, original.getSubject(), original.getEducationLevel(),
            original.getGradeNumber(), original.getSectionLetter(), nextYear,
            original.getMinimumGrade(), original.getAllowsBimestreRecovery(),
            original.getSchoolName()
        );

        return toResponse(classRoomRepository.save(nova));
    }

    public ClassRoom findEntityById(Long id, Long teacherId) {
        return getOrThrow(id, teacherId);
    }

    private void validateGradeForLevel(EducationLevel level, Integer grade) {
        if (level == EducationLevel.FUNDAMENTAL && (grade < 1 || grade > 9))
            throw new BusinessException("Ensino Fundamental aceita séries de 1 a 9");
        if (level == EducationLevel.MEDIO && (grade < 1 || grade > 3))
            throw new BusinessException("Ensino Médio aceita séries de 1 a 3");
    }

    private ClassRoom getOrThrow(Long id, Long teacherId) {
        return classRoomRepository.findByIdAndTeacherId(id, teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Turma não encontrada"));
    }

    private ClassRoomResponse toResponse(ClassRoom c) {
        return new ClassRoomResponse(
            c.getId(), c.getSubject().getName(), c.getEducationLevel(),
            c.getGradeNumber(), c.getSectionLetter(), c.getSchoolYear(),
            c.getStatus(), c.getCreatedAt(), c.getArchivedAt(),
            c.getMinimumGrade(), c.getAllowsBimestreRecovery(), c.getSchoolName()
        );
    }
}