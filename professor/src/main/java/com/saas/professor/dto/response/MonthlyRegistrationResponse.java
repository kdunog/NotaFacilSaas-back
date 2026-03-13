package com.saas.professor.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter
public class MonthlyRegistrationResponse {
	
	private int month;
	private long total;

	public MonthlyRegistrationResponse(int month, long total) {
	    this.month = month;
	    this.total = total;
	}

}
