package com.innowise.paymentservice.model.dto;

import com.innowise.paymentservice.model.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent {

    private Long orderId;
    private Long paymentId;
    private PaymentStatus status;

}

