package com.example.order.domain.repository;

import com.example.order.domain.entity.Order;

public interface OrderRepository {

    Order save(Order order);
}
