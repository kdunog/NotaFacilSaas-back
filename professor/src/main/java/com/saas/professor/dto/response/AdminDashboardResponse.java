package com.saas.professor.dto.response;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter
public class AdminDashboardResponse {

	private long totalTeachers;
	private long activeTeachers;
	private long inactiveTeachers;
	private long proProfessorPlans;
	private long escolaPlans;
	private long expiredPlans;
	private double monthlyRevenue;
	private double annualRevenue;
	private List<TeacherSummaryResponse> recentRegistrations;
	private List<TeacherSummaryResponse> expiredPlanTeachers;
	private List<MonthlyRegistrationResponse> registrationsByMonth;

	public AdminDashboardResponse(long totalTeachers, long activeTeachers,
	        long inactiveTeachers, long proProfessorPlans, long escolaPlans,
	        long expiredPlans, double monthlyRevenue, double annualRevenue,
	        List<TeacherSummaryResponse> recentRegistrations,
	        List<TeacherSummaryResponse> expiredPlanTeachers,
	        List<MonthlyRegistrationResponse> registrationsByMonth) {
	    this.totalTeachers = totalTeachers;
	    this.activeTeachers = activeTeachers;
	    this.inactiveTeachers = inactiveTeachers;
	    this.proProfessorPlans = proProfessorPlans;
	    this.escolaPlans = escolaPlans;
	    this.expiredPlans = expiredPlans;
	    this.monthlyRevenue = monthlyRevenue;
	    this.annualRevenue = annualRevenue;
	    this.recentRegistrations = recentRegistrations;
	    this.expiredPlanTeachers = expiredPlanTeachers;
	    this.registrationsByMonth = registrationsByMonth;
	}
}
