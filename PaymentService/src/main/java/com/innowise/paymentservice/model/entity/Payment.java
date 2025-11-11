package com.innowise.paymentservice.model.entity;

import com.innowise.paymentservice.model.PaymentStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * @ClassName Payment
 * @Description Represents a payment transaction stored in the MongoDB "payments" collection.
 * Contains metadata such as order reference, user, amount, status, and timestamp.
 * @Author dshparko
 * @Date 05.11.2025 11:32
 * @Version 1.0
 */
@Getter
@Setter
@Document(collection = "payments")
@NoArgsConstructor
public class Payment {

    @Id
    private ObjectId id;

    @Field("order_id")
    private Long orderId;

    @Indexed
    @Field("user_id")
    private Long userId;

    @Indexed
    private PaymentStatus status;

    @Indexed
    private Instant timestamp;

    @Field("payment_amount")
    private BigDecimal paymentAmount;

}
