package com.saas.professor.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.saas.professor.dto.response.CheckoutResponse;
import com.saas.professor.dto.response.SubscriptionResponse;
import com.saas.professor.enums.PlanType;
import com.saas.professor.security.TeacherUserDetails;
import com.saas.professor.service.SubscriptionService;
import java.util.Map;

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
        
        // ✅ Bloqueia ANTES de chamar service
        if (!userDetails.canAccess()) {
            return ResponseEntity.status(403).build();
        }
        
        return ResponseEntity.ok(
            subscriptionService.findCurrent(userDetails.getTeacher())
        );
    }

    @PostMapping("/checkout/{plan}")
    public ResponseEntity<CheckoutResponse> checkout(
            @PathVariable PlanType plan,
            @AuthenticationPrincipal TeacherUserDetails userDetails) {
        return ResponseEntity.ok(
                subscriptionService.createCheckout(plan, userDetails.getTeacher()));
    }

    @GetMapping("/payment-payer/{paymentId}")
    public ResponseEntity<Map<String, String>> getPaymentPayer(
            @PathVariable String paymentId,
            @AuthenticationPrincipal TeacherUserDetails userDetails) {
        try {
            Map<String, Object> payment = subscriptionService.getPaymentInfo(paymentId);
            Object payerObj = payment.get("payer");
            if (payerObj instanceof Map<?,?> payer) {
                String mpPayerId = String.valueOf(payer.get("id"));
                return ResponseEntity.ok(Map.of("mpPayerId", mpPayerId));
            }
        } catch (Exception e) {
            System.err.println("Erro ao buscar payer: " + e.getMessage());
        }
        return ResponseEntity.ok(Map.of());
    }

    @PostMapping("/link-payer")
    public ResponseEntity<Void> linkPayer(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal TeacherUserDetails userDetails) {
        String mpPayerId = body.get("mpPayerId");
        if (mpPayerId != null && !mpPayerId.isBlank()) {
            subscriptionService.linkPayerId(userDetails.getTeacher().getId(), mpPayerId);
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/cancel")
    public ResponseEntity<Void> cancel(
            @AuthenticationPrincipal TeacherUserDetails userDetails) {
        subscriptionService.cancelByTeacher(userDetails.getTeacher());
        return ResponseEntity.ok().build();
    }
}