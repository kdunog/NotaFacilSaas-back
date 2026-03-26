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
import com.saas.professor.entity.Teacher;
import com.saas.professor.enums.PlanType;
import com.saas.professor.security.TeacherUserDetails;
import com.saas.professor.service.SubscriptionService;
import com.saas.professor.repository.TeacherRepository;
import com.saas.professor.service.MercadoPagoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final TeacherRepository teacherRepository; // ← INJETE TeacherRepository!
    private final MercadoPagoService mercadoPagoService; // ← INJETE MercadoPagoService!
    private Logger log = LoggerFactory.getLogger(SubscriptionController.class);

    public SubscriptionController(SubscriptionService subscriptionService, TeacherRepository teacherRepository, 
    		MercadoPagoService mercadoPagoService, Logger logger) {
        this.subscriptionService = subscriptionService;
        this.teacherRepository = teacherRepository; // ← INJETE TeacherRepository!
        this.mercadoPagoService = mercadoPagoService; // ← INJETE MercadoPagoService!
        this.log = logger; // ← INJETE Logger!
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
            Map<String, Object> payment = mercadoPagoService.getPreapprovalPlan(paymentId);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> payer = (Map<String, Object>) payment.get("payer");
            
            if (payer != null) {
                String mpPayerId = String.valueOf(payer.get("id"));
                log.info("Payer encontrado: {} -> {}", paymentId, mpPayerId);
                return ResponseEntity.ok(Map.of("mpPayerId", mpPayerId));
            }
            
            log.warn("Payer nao encontrado: {}", paymentId);
            return ResponseEntity.ok(Map.of("mpPayerId", ""));
            
        } catch (Exception e) {
            log.error("Erro getPaymentPayer {}: {}", paymentId, e.getMessage(), e);
            return ResponseEntity.ok(Map.of("mpPayerId", ""));
        }
    }
    
    @PostMapping("/link-payer")
    public ResponseEntity<Void> linkPayer(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal TeacherUserDetails userDetails) {
        
        String mpPayerId = body.get("mpPayerId");
        
        if (mpPayerId != null && !mpPayerId.trim().isBlank()) {
            try {
                Teacher teacher = userDetails.getTeacher();
                teacher.setMercadoPagoPayerId(mpPayerId.trim());
                teacherRepository.save(teacher);
                
                log.info("Payer linked: {} -> {}", teacher.getEmail(), mpPayerId);
                return ResponseEntity.ok().build();
            } catch (Exception e) {
                log.error("Erro linkPayer: {}", e.getMessage(), e);
                return ResponseEntity.badRequest().build();
            }
        }
        
        log.warn("mpPayerId vazio");
        return ResponseEntity.badRequest().build();
    }
    
    
    @DeleteMapping("/cancel")
    public ResponseEntity<Void> cancel(
            @AuthenticationPrincipal TeacherUserDetails userDetails) {
        subscriptionService.cancelByTeacher(userDetails.getTeacher());
        return ResponseEntity.ok().build();
    }
}