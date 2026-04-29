package com.example.order.infrastructure.repository;

import com.example.order.domain.entity.Order;
import com.example.order.domain.enumtype.OrderStatus;
import com.example.order.domain.enumtype.OrderType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderJpaRepository extends JpaRepository<Order, UUID> {

    @Query("""
        select distinct o from Order o join o.items i
        where o.buyerId = :buyerId
          and (:orderType is null or o.orderType = :orderType)
          and (:keyword is null or i.productNameSnapshot like %:keyword%)
          and o.status <> com.example.order.domain.enumtype.OrderStatus.CREATED
        """)
    Page<Order> findByBuyerIdAndOrderType(@Param("buyerId") UUID buyerId, @Param("orderType") OrderType orderType, @Param("keyword") String keyword, Pageable pageable);

    @Query("""
    select o from Order o
    left join fetch o.items
    where o.orderId = :orderId
      and o.buyerId = :buyerId
""")
    Optional<Order> findByOrderIdAndBuyerId(UUID orderId, UUID buyerId);

    Optional<Order> findByOrderId(UUID orderId);

    @Query("""
    select o from Order o
    left join fetch o.items
    where o.status = :status
      and o.deliveredAt < :threshold
""")
    List<Order> findByStatusAndDeliveredAtBefore(OrderStatus status, LocalDateTime threshold);

    boolean existsByOrderNumber(String orderNumber);

    Optional<Order> findByAuctionId(UUID auctionId);
}
