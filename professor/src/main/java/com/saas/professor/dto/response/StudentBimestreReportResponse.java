package com.saas.professor.dto.response;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentBimestreReportResponse {
    private Long studentId;
    private String studentName;
    private double bimestreAverage;
    private long presences;
    private long absences;
    private long lates;
    private double attendancePercentage;
    private String situation;
    private List<GradeResponse> grades;

    public StudentBimestreReportResponse(Long studentId, String studentName,
                                          double bimestreAverage, long presences,
                                          long absences, long lates,
                                          double attendancePercentage,
                                          List<GradeResponse> grades,
                                          double minimumGrade,
                                          boolean allowsBimestreRecovery) {
        this.studentId = studentId;
        this.studentName = studentName;
        // Converte de 0-100 para 0-10
        this.bimestreAverage = Math.round((bimestreAverage / 10.0) * 100.0) / 100.0;
        this.presences = presences;
        this.absences = absences;
        this.lates = lates;
        this.attendancePercentage = attendancePercentage;
        this.grades = grades;
        this.situation = calcSituation(this.bimestreAverage, attendancePercentage,
                                       minimumGrade, allowsBimestreRecovery);
    }

    private String calcSituation(double avg, double freq,
                                  double minimumGrade, boolean allowsBimestreRecovery) {
        boolean aprovadoNota = avg >= minimumGrade;
        boolean aprovadoFreq  = freq >= 75.0;
        if (aprovadoNota && aprovadoFreq)   return "APROVADO";
        if (!aprovadoFreq && !aprovadoNota) return "REPROVADO_NOTA_FALTA";
        if (!aprovadoFreq)                  return "REPROVADO_FALTA";
        if (allowsBimestreRecovery)         return "RECUPERACAO_BIMESTRAL";
        return "ABAIXO_DA_MEDIA";
    }
}