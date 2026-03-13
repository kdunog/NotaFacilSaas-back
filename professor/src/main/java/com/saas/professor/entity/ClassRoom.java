package com.saas.professor.entity;
import java.time.LocalDateTime;
import com.saas.professor.enums.ClassRoomStatus;
import com.saas.professor.enums.EducationLevel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "class_rooms",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_active_classroom",
        columnNames = {"teacher_id","subject_id","school_year",
            "grade_number","education_level","section_letter","status"}
    )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "teacher_id", nullable = false)
    private Long teacherId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Enumerated(EnumType.STRING)
    @Column(name = "education_level", nullable = false)
    private EducationLevel educationLevel;

    @Column(name = "grade_number", nullable = false)
    private Integer gradeNumber;

    @Column(name = "section_letter", nullable = false, length = 1)
    private String sectionLetter;

    @Column(name = "school_year", nullable = false)
    private Integer schoolYear;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClassRoomStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "archived_at")
    private LocalDateTime archivedAt;

    @Column(name = "minimum_grade", nullable = false)
    private Double minimumGrade = 5.0;

    @Column(name = "allows_bimestre_recovery", nullable = false)
    private Boolean allowsBimestreRecovery = false;

    @Column(name = "school_name", length = 200)
    private String schoolName;

    public ClassRoom(Long teacherId, Subject subject, EducationLevel educationLevel,
                     Integer gradeNumber, String sectionLetter, Integer schoolYear,
                     Double minimumGrade, Boolean allowsBimestreRecovery, String schoolName) {
        this.teacherId = teacherId;
        this.subject = subject;
        this.educationLevel = educationLevel;
        this.gradeNumber = gradeNumber;
        this.sectionLetter = sectionLetter;
        this.schoolYear = schoolYear;
        this.minimumGrade = minimumGrade != null ? minimumGrade : 5.0;
        this.allowsBimestreRecovery = allowsBimestreRecovery != null ? allowsBimestreRecovery : false;
        this.schoolName = schoolName;
    }

    // construtor legado sem schoolName para compatibilidade
    public ClassRoom(Long teacherId, Subject subject, EducationLevel educationLevel,
                     Integer gradeNumber, String sectionLetter, Integer schoolYear,
                     Double minimumGrade, Boolean allowsBimestreRecovery) {
        this(teacherId, subject, educationLevel, gradeNumber, sectionLetter, schoolYear,
             minimumGrade, allowsBimestreRecovery, null);
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.status = ClassRoomStatus.ACTIVE;
    }
}