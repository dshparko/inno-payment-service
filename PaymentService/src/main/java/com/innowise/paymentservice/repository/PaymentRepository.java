package com.innowise.paymentservice.repository;

import com.innowise.paymentservice.model.entity.Payment;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.List;

/**
 * Repository interface for accessing and managing Payment documents in MongoDB.
 * Provides custom query methods for filtering payments by various attributes.
 *
 * @Author dshparko
 * @Date 05.11.2025
 * @Version 1.0
 */
public interface PaymentRepository extends MongoRepository<Payment, ObjectId> {

    /**
     * Saves a Payment entity to the database.
     *
     * @param entity the Payment to save
     * @return the saved Payment
     */
    Payment save(Payment entity);

    /**
     * Retrieves all payments associated with a specific order ID.
     *
     * @param orderId the order ID to filter by
     * @return list of matching payments
     */
    List<Payment> findByOrderId(String orderId);

    /**
     * Retrieves all payments made by a specific user.
     *
     * @param userId the user ID to filter by
     * @return list of matching payments
     */
    List<Payment> findByUserId(String userId);

    /**
     * Retrieves all payments whose status matches any of the provided values.
     *
     * @param statuses list of status values to filter by
     * @return list of matching payments
     */
    List<Payment> findByStatusIn(List<String> statuses);

    /**
     * Retrieves all payments made within a specific time range.
     *
     * @param from start of the time range (inclusive)
     * @param to   end of the time range (inclusive)
     * @return list of matching payments
     */
    @Query("{ 'timestamp': { $gte: ?0, $lte: ?1 } }")
    List<Payment> findByTimestampBetween(Instant from, Instant to);

    /**
     * Retrieves only the payment amounts for payments made within a specific time range.
     *
     * @param from start of the time range (inclusive)
     * @param to   end of the time range (inclusive)
     * @return list of payments with only the paymentAmount field populated
     */
    @Query(value = "{ 'timestamp': { $gte: ?0, $lte: ?1 } }", fields = "{ paymentAmount: 1 }")
    List<Payment> findAmountsByTimestampBetween(Instant from, Instant to);

}