package com.saas.professor.service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.saas.professor.dto.request.CreateExamRequest;
import com.saas.professor.dto.request.UpdateExamRequest;
import com.saas.professor.dto.response.ExamResponse;
import com.saas.professor.entity.ClassRoom;
import com.saas.professor.entity.Exam;
import com.saas.professor.entity.Question;
import com.saas.professor.enums.ExamStatus;
import com.saas.professor.exceptions.BusinessException;
import com.saas.professor.exceptions.ResourceNotFoundException;
import com.saas.professor.repository.ExamRepository;
import com.saas.professor.repository.GradeRepository;
import com.saas.professor.repository.QuestionRepository;
import com.saas.professor.repository.StudentAnswerRepository;

@Service
public class ExamService {

    private final ExamRepository examRepository;
    private final ClassRoomService classRoomService;
    private final QuestionRepository questionRepository;
    private final GradeRepository gradeRepository;
    private final StudentAnswerRepository studentAnswerRepository;

    public ExamService(ExamRepository examRepository,
                       ClassRoomService classRoomService,
                       QuestionRepository questionRepository,
                       GradeRepository gradeRepository,
                       StudentAnswerRepository studentAnswerRepository) {
        this.examRepository = examRepository;
        this.classRoomService = classRoomService;
        this.questionRepository = questionRepository;
        this.gradeRepository = gradeRepository;
        this.studentAnswerRepository = studentAnswerRepository;
    }

    @Transactional
    public ExamResponse create(CreateExamRequest req, Long teacherId) {
        ClassRoom classRoom = classRoomService.findEntityById(req.getClassRoomId(), teacherId);
        Exam exam = new Exam(classRoom, req.getTitle(), req.getExamDate(),
                req.getTotalPoints(), req.getWeight());
        return toResponse(examRepository.save(exam));
    }

    @Transactional
    public ExamResponse update(Long examId, UpdateExamRequest req, Long teacherId) {
        Exam exam = getOrThrow(examId, teacherId);
        if (exam.getStatus() == ExamStatus.APPLIED)
            throw new BusinessException("Não é possível editar uma prova já aplicada");
        exam.setTitle(req.getTitle());
        exam.setExamDate(req.getExamDate());
        exam.setTotalPoints(req.getTotalPoints());
        exam.setWeight(req.getWeight());
        return toResponse(examRepository.save(exam));
    }

    @Transactional
    public ExamResponse apply(Long examId, Long teacherId) {
        Exam exam = getOrThrow(examId, teacherId);
        if (exam.getStatus() == ExamStatus.APPLIED)
            throw new BusinessException("Prova já foi aplicada");
        exam.setStatus(ExamStatus.APPLIED);
        exam.setAppliedAt(LocalDateTime.now());
        return toResponse(examRepository.save(exam));
    }

    @Transactional
    public void delete(Long examId, Long teacherId) {
        Exam exam = getOrThrow(examId, teacherId);

        // 1. Deletar respostas dos alunos
        studentAnswerRepository.deleteAll(
            studentAnswerRepository.findByQuestion_ExamId(examId)
        );

        // 2. Deletar grades
        gradeRepository.deleteAll(gradeRepository.findByExamId(examId));

        // 3. Deletar questões (options via cascade na entidade)
        questionRepository.deleteAll(
            questionRepository.findByExamIdOrderByOrderIndexAsc(examId)
        );

        // 4. Deletar prova
        examRepository.delete(exam);
    }

    @Transactional(readOnly = true)
    public List<ExamResponse> findByClassRoom(Long classRoomId, Long teacherId) {
        ClassRoom classRoom = classRoomService.findEntityById(classRoomId, teacherId);
        return examRepository.findByClassRoom(classRoom)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public Exam findEntityById(Long id, Long teacherId) {
        return getOrThrow(id, teacherId);
    }

    private Exam getOrThrow(Long id, Long teacherId) {
        return examRepository.findByIdAndClassRoom_TeacherId(id, teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Prova não encontrada"));
    }

    private ExamResponse toResponse(Exam e) {
        return new ExamResponse(e.getId(), e.getClassRoom().getId(), e.getTitle(),
                e.getExamDate(), e.getTotalPoints(), e.getWeight(),
                e.getStatus(), e.getAppliedAt(), e.getCreatedAt());
    }
}