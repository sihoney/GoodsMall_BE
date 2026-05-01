package com.example.order.infrastructure.repository;

import com.example.order.domain.entity.Delivery;
import com.example.order.domain.enumtype.DeliveryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryJpaRepository extends JpaRepository<Delivery, UUID> {

    Optional<Delivery> findByDeliveryIdAndBuyerId(UUID deliveryId, UUID buyerId);

    Optional<Delivery> findByOrderItemOrderItemId(UUID orderItemId);

    @Query(value = """
        select d from Delivery d
        join fetch d.orderItem oi
        join fetch oi.order o
        where d.sellerId = :sellerId
          and d.status = :status
          and (:courierCode is null or d.courierCode = :courierCode)
          and (:orderNumber is null or o.orderNumber like :orderNumber)
          and (:receiver is null or o.receiver like :receiver)
          and (:productName is null or oi.productNameSnapshot like :productName)
          and d.createdAt >= :dateFrom
          and d.createdAt <= :dateTo
        """,
        countQuery = """
        select count(d) from Delivery d
        join d.orderItem oi
        join oi.order o
        where d.sellerId = :sellerId
          and d.status = :status
          and (:courierCode is null or d.courierCode = :courierCode)
          and (:orderNumber is null or o.orderNumber like :orderNumber)
          and (:receiver is null or o.receiver like :receiver)
          and (:productName is null or oi.productNameSnapshot like :productName)
          and d.createdAt >= :dateFrom
          and d.createdAt <= :dateTo
        """)
    Page<Delivery> findBySellerIdAndStatusWithFilters(
            @Param("sellerId") UUID sellerId,
            @Param("status") DeliveryStatus status,
            @Param("courierCode") String courierCode,
            @Param("orderNumber") String orderNumber,
            @Param("receiver") String receiver,
            @Param("productName") String productName,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            Pageable pageable
    );

    @Query(value = """
        select d from Delivery d
        join fetch d.orderItem oi
        join fetch oi.order o
        where d.sellerId = :sellerId
          and d.status <> com.example.order.domain.enumtype.DeliveryStatus.CANCELED
          and (:courierCode is null or d.courierCode = :courierCode)
          and (:orderNumber is null or o.orderNumber like :orderNumber)
          and (:receiver is null or o.receiver like :receiver)
          and (:productName is null or oi.productNameSnapshot like :productName)
          and d.createdAt >= :dateFrom
          and d.createdAt <= :dateTo
        """,
        countQuery = """
        select count(d) from Delivery d
        join d.orderItem oi
        join oi.order o
        where d.sellerId = :sellerId
          and d.status <> com.example.order.domain.enumtype.DeliveryStatus.CANCELED
          and (:courierCode is null or d.courierCode = :courierCode)
          and (:orderNumber is null or o.orderNumber like :orderNumber)
          and (:receiver is null or o.receiver like :receiver)
          and (:productName is null or oi.productNameSnapshot like :productName)
          and d.createdAt >= :dateFrom
          and d.createdAt <= :dateTo
        """)
    Page<Delivery> findBySellerIdWithFilters(
            @Param("sellerId") UUID sellerId,
            @Param("courierCode") String courierCode,
            @Param("orderNumber") String orderNumber,
            @Param("receiver") String receiver,
            @Param("productName") String productName,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            Pageable pageable
    );

    @Query("""
        select d.status, count(d) from Delivery d
        where d.sellerId = :sellerId
        group by d.status
        """)
    List<Object[]> countBySellerIdGroupByStatus(@Param("sellerId") UUID sellerId);
}
