package com.saas.professor.dto.request;

import java.util.List;

import com.saas.professor.enums.EducationLevel;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoveStudentRequest {
	
	@NotNull
	private Long teacherId;

	@NotNull(message = "Lista de alunos é obrigatória")
	private List<Long> studentIds;

	@NotNull(message = "Turma destino é obrigatória")
	private Long targetClassRoomId;


	public Long getTeacherId() {
		return teacherId;
	}

	public void setTeacherId(Long teacherId) {
		this.teacherId = teacherId;
	}

	public List<Long> getStudentIds() {
		return studentIds;
	}

	public void setStudentIds(List<Long> studentIds) {
		this.studentIds = studentIds;
	}

	public Long getTargetClassRoomId() {
		return targetClassRoomId;
	}

	public void setTargetClassRoomId(Long targetClassRoomId) {
		this.targetClassRoomId = targetClassRoomId;
	}
	
	
	

}
