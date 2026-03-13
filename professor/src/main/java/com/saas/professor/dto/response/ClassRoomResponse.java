package com.saas.professor.dto.response;
import java.time.LocalDateTime;
import com.saas.professor.enums.ClassRoomStatus;
import com.saas.professor.enums.EducationLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassRoomResponse {
    private Long id;
    private String subjectName;
    private EducationLevel educationLevel;
    private Integer gradeNumber;
    private String sectionLetter;
    private Integer schoolYear;
    private ClassRoomStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime archivedAt;
    private Double minimumGrade;
    private Boolean allowsBimestreRecovery;
    private String schoolName;
}