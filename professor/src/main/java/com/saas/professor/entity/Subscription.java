package com.saas.professor.entity;

import java.time.LocalDateTime;

import com.saas.professor.enums.PlanType;
import com.saas.professor.enums.SubscriptionStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "subscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "teacher_id", nullable = false)
	private Teacher teacher;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PlanType plan;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SubscriptionStatus status;

	@Column(name = "mercadopago_payment_id")
	private String mercadoPagoPaymentId;

	@Column(name = "amount_paid")
	private Double amountPaid;

	@Column(name = "starts_at")
	private LocalDateTime startsAt;

	@Column(name = "expires_at")
	private LocalDateTime expiresAt;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	public Subscription(Teacher teacher, PlanType plan,
	                    String mercadoPagoPaymentId, Double amountPaid) {
	    this.teacher = teacher;
	    this.plan = plan;
	    this.mercadoPagoPaymentId = mercadoPagoPaymentId;
	    this.amountPaid = amountPaid;
	    this.status = SubscriptionStatus.ACTIVE;
	    this.startsAt = LocalDateTime.now();
	    this.expiresAt = LocalDateTime.now().plusMonths(1);
	}

	@PrePersist
	public void prePersist() {
	    this.createdAt = LocalDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Teacher getTeacher() {
		return teacher;
	}

	public void setTeacher(Teacher teacher) {
		this.teacher = teacher;
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

	public String getMercadoPagoPaymentId() {
		return mercadoPagoPaymentId;
	}

	public void setMercadoPagoPaymentId(String mercadoPagoPaymentId) {
		this.mercadoPagoPaymentId = mercadoPagoPaymentId;
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

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
	
	

}
