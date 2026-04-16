package com.example.order.domain.repository;

import com.example.order.domain.entity.Claim;

import java.util.List;

public interface ClaimRepository {

    List<Claim> saveAll(List<Claim> claims);
}
