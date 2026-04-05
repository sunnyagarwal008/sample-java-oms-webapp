package com.example.ordermanagement.repository;

import com.example.ordermanagement.model.Order;
import com.example.ordermanagement.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByCustomerEmail(String customerEmail);
}
