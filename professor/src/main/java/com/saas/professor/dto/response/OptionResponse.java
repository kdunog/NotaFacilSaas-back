package com.saas.professor.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OptionResponse {
	
	private Long id;
	private String text;
	private Boolean correct;
	private Integer orderIndex;

	public OptionResponse(Long id, String text, Boolean correct, Integer orderIndex) {
	    this.id = id;
	    this.text = text;
	    this.correct = correct;
	    this.orderIndex = orderIndex;
	}

}
