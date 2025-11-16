package com.innowise.paymentservice.service;

import com.innowise.paymentservice.model.PaymentStatus;
import com.innowise.paymentservice.model.dto.OrderEvent;
import com.innowise.paymentservice.model.dto.PaymentDto;
import com.innowise.paymentservice.model.dto.PaymentEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * @ClassName PaymentService
 * @Description Service interface for managing payment operations.
 * Provides methods for creating payments, retrieving them by various filters,
 * and calculating aggregated statistics.
 * @Author dshparko
 * @Date 11.11.2025 19:15
 * @Version 1.0
 */
public interface PaymentService {

    /**
     * Processes an incoming {@link OrderEvent} by determining the payment status based on parity
     * and creating a corresponding {@link PaymentDto} entry in the system.
     *
     * @param event  the incoming order event containing orderId and userId
     * @param isEven flag indicating whether the randomly generated number was even (success) or odd (failure)
     * @return the created and persisted payment DTO
     */
    PaymentDto processOrderEvent(OrderEvent event, boolean isEven);

    /**
     * Converts a {@link PaymentDto} into a {@link PaymentEvent} suitable for Kafka publishing.
     *
     * @param dto the payment DTO to convert
     * @return a Kafka-ready payment event containing orderId, userId, and payment status
     */
    PaymentEvent toPaymentEvent(PaymentDto dto);

    /**
     * Creates and persists a new payment.
     *
     * @param request the payment data to create
     * @return the saved payment as a DTO
     */
    PaymentDto create(PaymentDto request);

    /**
     * Retrieves all payments associated with a specific order ID.
     *
     * @param orderId the order ID to filter by
     * @return list of matching payments
     */
    List<PaymentDto> getByOrderId(Long orderId);

    /**
     * Retrieves all payments made by a specific user.
     *
     * @param userId the user ID to filter by
     * @return list of matching payments
     */
    List<PaymentDto> getByUserId(Long userId);

    /**
     * Retrieves all payments whose status matches any of the provided values.
     *
     * @param statuses set of status values to filter by
     * @return list of matching payments
     */
    List<PaymentDto> getByStatuses(Set<PaymentStatus> statuses);

    /**
     * Calculates the total sum of payments made within a specific time range.
     *
     * @param from start of the time range (inclusive)
     * @param to   end of the time range (inclusive)
     * @return total payment amount in the range
     */
    BigDecimal getTotalBetween(Instant from, Instant to);

    /**
     * Checks whether the specified order has already been processed.
     *
     *
     * @param orderId the unique identifier of the order to check; must not be {@code null}
     * @return {@code true} if the order has already been processed; {@code false} otherwise
     */
    boolean isAlreadyProcessed(Long orderId);

}
