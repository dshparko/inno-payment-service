package com.innowise.paymentservice.model.dto;

import com.innowise.paymentservice.model.PaymentStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent {

    @NotNull
    @Min(1)
    private Long paymentId;

    @NotNull
    @Min(1)
    private Long orderId;

    @NotNull
    private PaymentStatus status;
}

