package com.innowise.paymentservice.model.dto;

import com.innowise.paymentservice.model.PaymentStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * @ClassName PaymentDto
 * @Description Data Transfer Object representing a payment request or response.
 * Used for communication between services and for Kafka message exchange.
 * @Author dshparko
 * @Date 05.11.2025 17:47
 * @Version 1.0
 */
@Getter
@Builder
public class PaymentDto {
    @NotBlank
    private String paymentId;

    @NotNull
    private Long orderId;

    @NotNull
    private Long userId;

    @NotNull
    private Instant timestamp;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal paymentAmount;

    @NotNull
    private PaymentStatus status;
}
