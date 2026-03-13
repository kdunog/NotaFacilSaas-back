package com.saas.professor.dto.response;

import java.time.LocalDateTime;

import com.saas.professor.dto.request.CreateClassRoomRequest;
import com.saas.professor.enums.EducationLevel;
import com.saas.professor.enums.PlanType;
import com.saas.professor.enums.SubscriptionStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponse {
	
	private Long id;
	private PlanType plan;
	private SubscriptionStatus status;
	private Double amountPaid;
	private LocalDateTime startsAt;
	private LocalDateTime expiresAt;


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public PlanType getPlan() {
		return plan;
	}

	public void setPlan(PlanType plan) {
		this.plan = plan;
	}

	public SubscriptionStatus getStatus() {
		return status;
	}

	public void setStatus(SubscriptionStatus status) {
		this.status = status;
	}

	public Double getAmountPaid() {
		return amountPaid;
	}

	public void setAmountPaid(Double amountPaid) {
		this.amountPaid = amountPaid;
	}

	public LocalDateTime getStartsAt() {
		return startsAt;
	}

	public void setStartsAt(LocalDateTime startsAt) {
		this.startsAt = startsAt;
	}

	public LocalDateTime getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(LocalDateTime expiresAt) {
		this.expiresAt = expiresAt;
	}
	
	

}
