package com.saas.professor.dto.response;

import java.util.List;

import com.saas.professor.enums.QuestionType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionResponse {
	
	private Long id;
	private Long examId;
	private QuestionType type;
	private String statement;
	private Double points;
	private Integer orderIndex;
	private List<OptionResponse> options;

	public QuestionResponse(Long id, Long examId, QuestionType type, String statement,
	                         Double points, Integer orderIndex, List<OptionResponse> options) {
	    this.id = id;
	    this.examId = examId;
	    this.type = type;
	    this.statement = statement;
	    this.points = points;
	    this.orderIndex = orderIndex;
	    this.options = options;
	}

}
