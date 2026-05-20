package com.example.member.application.port.out;

import com.example.member.domain.entity.Seller;
import java.util.Optional;
import java.util.UUID;

public interface SellerPersistencePort {

    Seller save(Seller seller);

    Optional<Seller> findByMemberId(UUID memberId);

    boolean existsByMemberId(UUID memberId);
}
