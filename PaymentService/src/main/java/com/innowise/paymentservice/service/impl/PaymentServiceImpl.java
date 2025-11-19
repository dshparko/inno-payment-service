package com.innowise.paymentservice.service.impl;

import com.innowise.paymentservice.mapper.PaymentMapper;
import com.innowise.paymentservice.model.PaymentStatus;
import com.innowise.paymentservice.model.dto.OrderEvent;
import com.innowise.paymentservice.model.dto.PaymentDto;
import com.innowise.paymentservice.model.dto.PaymentEvent;
import com.innowise.paymentservice.model.entity.Payment;
import com.innowise.paymentservice.repository.PaymentRepository;
import com.innowise.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * @ClassName PaymentService
 * @Description Service interface for managing payment operations.
 * @Author dshparko
 *
 * @Date 05.11.2025 17:41
 * @Version 1.0
 */
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    public static final String PAYMENT = "PAYMENT-";
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    @Override
    public boolean isAlreadyProcessed(Long orderId) {
        return paymentRepository.existsByOrderId(orderId);
    }

    @Override
    public PaymentDto create(PaymentDto request) {
        if (isAlreadyProcessed(request.getOrderId())) {
            throw new IllegalStateException("Order " + request.getOrderId() + " already processed");
        }
        Payment payment = paymentMapper.toEntity(request);

        Payment saved = paymentRepository.save(payment);
        return paymentMapper.toDto(saved);
    }

    @Override
    public List<PaymentDto> getByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .stream()
                .map(paymentMapper::toDto)
                .toList();
    }

    @Override
    public List<PaymentDto> getByUserId(Long userId) {
        return paymentRepository.findByUserId(userId)
                .stream()
                .map(paymentMapper::toDto)
                .toList();
    }

    @Override
    public List<PaymentDto> getByStatuses(Set<PaymentStatus> statuses) {
        List<PaymentStatus> statusNames = statuses.stream()
                .toList();

        return paymentRepository.findByStatusIn(statusNames)
                .stream()
                .map(paymentMapper::toDto)
                .toList();
    }

    @Override
    public BigDecimal getTotalBetween(Instant from, Instant to) {
        return paymentRepository.findAmountsByTimestampBetween(from, to)
                .stream()
                .map(Payment::getPaymentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public PaymentDto processOrderEvent(OrderEvent event, boolean isEven) {
        if (event == null || event.getOrderId() == null || event.getUserId() == null || event.getAmount() == null) {
            throw new IllegalArgumentException("Invalid OrderEvent: missing required fields");
        }

        PaymentStatus status = isEven ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
        String paymentId = generatePaymentId(event.getOrderId());

        PaymentDto dto = PaymentDto.builder()
                .paymentId(paymentId)
                .orderId(event.getOrderId())
                .userId(event.getUserId())
                .paymentAmount(event.getAmount())
                .status(status)
                .timestamp(Instant.now())
                .build();

        return create(dto);
    }


    public PaymentEvent toPaymentEvent(PaymentDto dto) {
        return new PaymentEvent(dto.getOrderId(), dto.getOrderId(), dto.getStatus());
    }

    private String generatePaymentId(Long orderId) {
        return PAYMENT + orderId;
    }


}