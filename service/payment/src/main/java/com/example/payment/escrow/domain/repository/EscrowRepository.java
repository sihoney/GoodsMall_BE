package com.example.payment.escrow.domain.repository;

import com.example.payment.escrow.domain.entity.Escrow;
import com.example.payment.escrow.domain.enumtype.EscrowReferenceType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * escrow ???議고쉶 ?ы듃??
 * ?ㅼ쨷 seller 二쇰Ц 吏???댄썑?먮뒗 orderId ?④굔???꾨땲??orderId ?꾩껜 紐⑸줉 ?먮뒗 orderId + seller 湲곗? 議고쉶瑜??ъ슜?쒕떎.
 */
public interface EscrowRepository {

    Escrow save(Escrow escrow);

    /**
     * 媛숈? 二쇰Ц???랁븳 escrow ?щ윭 嫄댁쓣 ??踰덉뿉 ??ν븳??
     */
    List<Escrow> saveAll(List<Escrow> escrows);

    Optional<Escrow> findByEscrowId(UUID escrowId);

    /**
     * seller蹂?援щℓ?뺤젙/?댁젣 泥섎━瑜??꾪빐 二쇰Ц怨?seller 湲곗??쇰줈 escrow瑜?李얜뒗??
     */
    List<Escrow> findAllByOrderIdAndSellerMemberId(UUID orderId, UUID sellerMemberId);

    /**
     * 諛곗넚?꾨즺泥섎읆 二쇰Ц ?⑥쐞 ?대깽?멸? ?ㅼ뼱?????대떦 二쇰Ц??escrow ?꾩껜瑜?議고쉶?쒕떎.
     */
    List<Escrow> findAllByOrderId(UUID orderId);

    List<Escrow> lockAllByOrderId(UUID orderId);

    List<Escrow> findAllByReferenceTypeAndReferenceIdIn(EscrowReferenceType referenceType, List<UUID> referenceIds);

    Page<Escrow> findPendingBySellerMemberId(UUID sellerMemberId, Pageable pageable);
}
