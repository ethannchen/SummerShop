package com.summershop.order.repository;

import com.summershop.order.entity.Order;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends CassandraRepository<Order, UUID> {

    @Query("SELECT * FROM orders WHERE order_id = :orderId")
    Optional<Order> findByOrderId(@Param("orderId") UUID orderId);
}