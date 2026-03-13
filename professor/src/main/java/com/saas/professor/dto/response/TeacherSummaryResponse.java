package com.saas.professor.dto.response;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter
public class TeacherSummaryResponse {
	
	private Long id;
	private String name;
	private String email;
	private String plan;
	private Boolean active;
	private LocalDateTime createdAt;
	private LocalDateTime planExpiresAt;

	public TeacherSummaryResponse(Long id, String name, String email,
	        String plan, Boolean active, LocalDateTime createdAt,
	        LocalDateTime planExpiresAt) {
	    this.id = id;
	    this.name = name;
	    this.email = email;
	    this.plan = plan;
	    this.active = active;
	    this.createdAt = createdAt;
	    this.planExpiresAt = planExpiresAt;
	}

}
