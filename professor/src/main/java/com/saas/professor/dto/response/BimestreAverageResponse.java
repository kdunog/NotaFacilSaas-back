package com.saas.professor.dto.response;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class BimestreAverageResponse {
    private String bimestreName;
    private int bimestreNumber;
    private double average;
    private double attendancePercentage;
}