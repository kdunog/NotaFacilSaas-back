package com.saas.professor.dto.request;

import java.time.LocalDate;
import java.util.List;

import com.saas.professor.enums.EducationLevel;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveAttendanceListRequest {

	@NotNull(message = "classRoomId é obrigatório")
	private Long classRoomId;

	@NotNull(message = "Data é obrigatória")
	private LocalDate date;

	@NotNull(message = "Lista de chamada é obrigatória")
	private List<SaveAttendanceRequest> attendances;
	

	public Long getClassRoomId() {
		return classRoomId;
	}

	public void setClassRoomId(Long classRoomId) {
		this.classRoomId = classRoomId;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public List<SaveAttendanceRequest> getAttendances() {
		return attendances;
	}

	public void setAttendances(List<SaveAttendanceRequest> attendances) {
		this.attendances = attendances;
	}
	
	
}


