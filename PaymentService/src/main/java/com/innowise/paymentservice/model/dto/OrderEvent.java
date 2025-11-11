package com.innowise.paymentservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class OrderEvent {

    private Long orderId;
    private Long paymentId;
    private Long userId;

}