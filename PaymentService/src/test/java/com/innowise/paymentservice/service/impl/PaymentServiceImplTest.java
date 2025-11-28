package com.innowise.paymentservice.service.impl;

import com.innowise.paymentservice.mapper.PaymentMapper;
import com.innowise.paymentservice.model.PaymentStatus;
import com.innowise.paymentservice.model.dto.OrderEvent;
import com.innowise.paymentservice.model.dto.PaymentDto;
import com.innowise.paymentservice.model.dto.PaymentEvent;
import com.innowise.paymentservice.model.entity.Payment;
import com.innowise.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentServiceImplTest {

    private PaymentRepository paymentRepository;
    private PaymentMapper paymentMapper;
    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setUp() {
        paymentRepository = mock(PaymentRepository.class);
        paymentMapper = mock(PaymentMapper.class);
        paymentService = new PaymentServiceImpl(paymentRepository, paymentMapper);
    }

    @Test
    void create_shouldSaveAndReturnDto() {
        PaymentDto dto = PaymentDto.builder()
                .orderId(1L)
                .userId(2L)
                .status(PaymentStatus.SUCCESS)
                .timestamp(Instant.now())
                .build();

        Payment entity = new Payment();
        Payment saved = new Payment();

        when(paymentMapper.toEntity(dto)).thenReturn(entity);
        when(paymentRepository.save(entity)).thenReturn(saved);
        when(paymentMapper.toDto(saved)).thenReturn(dto);

        PaymentDto result = paymentService.create(dto);

        assertThat(result).isEqualTo(dto);
        verify(paymentRepository).save(entity);
    }

    @Test
    void getByOrderId_shouldReturnMappedDtos() {
        Payment entity = new Payment();
        PaymentDto dto = PaymentDto.builder().orderId(1L).build();

        when(paymentRepository.findByOrderId(1L)).thenReturn(List.of(entity));
        when(paymentMapper.toDto(entity)).thenReturn(dto);

        List<PaymentDto> result = paymentService.getByOrderId(1L);

        assertThat(result).containsExactly(dto);
    }

    @Test
    void getByUserId_shouldReturnMappedDtos() {
        Payment entity = new Payment();
        PaymentDto dto = PaymentDto.builder().userId(2L).build();

        when(paymentRepository.findByUserId(2L)).thenReturn(List.of(entity));
        when(paymentMapper.toDto(entity)).thenReturn(dto);

        List<PaymentDto> result = paymentService.getByUserId(2L);

        assertThat(result).containsExactly(dto);
    }

    @Test
    void getByStatuses_shouldMapEnumsAndReturnDtos() {
        Payment entity = new Payment();
        PaymentDto dto = PaymentDto.builder().status(PaymentStatus.FAILED).build();

        when(paymentRepository.findByStatusIn(List.of(PaymentStatus.FAILED))).thenReturn(List.of(entity));
        when(paymentMapper.toDto(entity)).thenReturn(dto);

        List<PaymentDto> result = paymentService.getByStatuses(Set.of(PaymentStatus.FAILED));

        assertThat(result).containsExactly(dto);
    }

    @Test
    void getTotalBetween_shouldAggregateAmounts() {
        Payment p1 = new Payment();
        Payment p2 = new Payment();
        p1.setPaymentAmount(BigDecimal.valueOf(100));
        p2.setPaymentAmount(BigDecimal.valueOf(50));

        when(paymentRepository.findAmountsByTimestampBetween(any(), any())).thenReturn(List.of(p1, p2));

        BigDecimal result = paymentService.getTotalBetween(Instant.now().minusSeconds(3600), Instant.now());

        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(150));
    }

    @Test
    void processOrderEvent_shouldCreatePaymentWithCorrectStatus() {
        OrderEvent event = new OrderEvent(1L, 2L, BigDecimal.TWO);
        PaymentDto dto = PaymentDto.builder().orderId(1L).userId(2L).status(PaymentStatus.SUCCESS).timestamp(Instant.now()).build();

        when(paymentMapper.toEntity(any())).thenReturn(new Payment());
        when(paymentRepository.save(any())).thenReturn(new Payment());
        when(paymentMapper.toDto(any())).thenReturn(dto);

        PaymentDto result = paymentService.processOrderEvent(event, true);

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(result.getOrderId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo(2L);
    }

    @Test
    void toPaymentEvent_shouldMapDtoToEvent() {
        PaymentDto dto = PaymentDto.builder()
                .orderId(1L)
                .status(PaymentStatus.SUCCESS)
                .build();

        PaymentEvent event = paymentService.toPaymentEvent(dto);

        assertThat(event.getOrderId()).isEqualTo(1L);
        assertThat(event.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
    }
}
