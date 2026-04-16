package com.example.order.infrastructure.repository;

import com.example.order.domain.entity.Claim;
import com.example.order.domain.repository.ClaimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ClaimRepositoryImpl implements ClaimRepository {

    private final ClaimJpaRepository claimJpaRepository;

    @Override
    public List<Claim> saveAll(List<Claim> claims) {
        return claimJpaRepository.saveAll(claims);
    }
}