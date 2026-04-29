package com.example.order.application.service;

import com.example.order.domain.entity.Delivery;
import com.example.order.domain.entity.Order;
import com.example.order.domain.entity.OrderItem;
import com.example.order.domain.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryCreateService {

    private final DeliveryRepository deliveryRepository;

    @Transactional
    public void create(Order order) {
        List<Delivery> deliveries = new ArrayList<>();

        for (OrderItem orderItem : order.getItems()) {
            orderItem.prepare();
            deliveries.add(Delivery.create(
                    orderItem.getSellerId(),
                    order.getBuyerId(),
                    orderItem));
        }

        deliveryRepository.saveAll(deliveries);
    }
}
