package com.innowise.paymentservice.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {
    @NotNull
    @Min(1)
    private Long orderId;

    @NotNull
    @Min(1)
    private Long userId;

    @NotNull
    @Positive
    private BigDecimal amount;
}