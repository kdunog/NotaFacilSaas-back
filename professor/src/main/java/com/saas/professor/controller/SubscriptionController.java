package com.saas.professor.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saas.professor.dto.response.CheckoutResponse;
import com.saas.professor.dto.response.SubscriptionResponse;
import com.saas.professor.enums.PlanType;
import com.saas.professor.security.TeacherUserDetails;
import com.saas.professor.service.SubscriptionService;

@RestController
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/my")
    public ResponseEntity<SubscriptionResponse> findCurrent(
            @AuthenticationPrincipal TeacherUserDetails userDetails) {
        try {
            return ResponseEntity.ok(subscriptionService.findCurrent(userDetails.getTeacher()));
        } catch (Exception e) {
            return ResponseEntity.ok(null); // sem assinatura ativa — trial ou sem plano
        }
    }

    @PostMapping("/checkout/{plan}")
    public ResponseEntity<CheckoutResponse> checkout(
            @PathVariable PlanType plan,
            @AuthenticationPrincipal TeacherUserDetails userDetails) {
        return ResponseEntity.ok(
                subscriptionService.createCheckout(plan, userDetails.getTeacher()));
    }

    @DeleteMapping("/cancel")
    public ResponseEntity<Void> cancel(
            @AuthenticationPrincipal TeacherUserDetails userDetails) {
        subscriptionService.cancelByTeacher(userDetails.getTeacher());
        return ResponseEntity.ok().build();
    }
}