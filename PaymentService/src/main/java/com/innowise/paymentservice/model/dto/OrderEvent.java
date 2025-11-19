package com.innowise.paymentservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {

    private Long orderId;
    private Long userId;
    private BigDecimal amount;

}