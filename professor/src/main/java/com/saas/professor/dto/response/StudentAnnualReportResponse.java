package com.saas.professor.dto.response;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentAnnualReportResponse {
    private Long studentId;
    private String studentName;
    private double annualAverage;
    private long totalPresences;
    private long totalAbsences;
    private long totalLates;
    private double annualAttendancePercentage;
    private String finalSituation;
    private List<BimestreAverageResponse> bimestreAverages;

    public StudentAnnualReportResponse(Long studentId, String studentName,
                                        double annualAverage, long totalPresences,
                                        long totalAbsences, long totalLates,
                                        double annualAttendancePercentage,
                                        List<BimestreAverageResponse> bimestreAverages,
                                        double minimumGrade) {
        this.studentId = studentId;
        this.studentName = studentName;
        // Converte de 0-100 para 0-10
        this.annualAverage = Math.round((annualAverage / 10.0) * 100.0) / 100.0;
        this.totalPresences = totalPresences;
        this.totalAbsences = totalAbsences;
        this.totalLates = totalLates;
        this.annualAttendancePercentage = annualAttendancePercentage;
        this.bimestreAverages = bimestreAverages;
        this.finalSituation = calcSituation(this.annualAverage, annualAttendancePercentage, minimumGrade);
    }

    private String calcSituation(double avg, double freq, double minimumGrade) {
        boolean aprovadoNota = avg >= minimumGrade;
        boolean aprovadoFreq  = freq >= 75.0;
        if (aprovadoNota && aprovadoFreq)   return "APROVADO";
        if (!aprovadoFreq && !aprovadoNota) return "REPROVADO_NOTA_FALTA";
        if (!aprovadoFreq)                  return "REPROVADO_FALTA";
        return "RECUPERACAO_FINAL";
    }
}