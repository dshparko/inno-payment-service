package com.innowise.paymentservice.mapper;

import com.innowise.paymentservice.model.dto.PaymentDto;
import com.innowise.paymentservice.model.entity.Payment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    PaymentDto toDto(Payment entity);

    Payment toEntity(PaymentDto dto);
    
}


