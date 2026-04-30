package com.todaylunch.auction.application.service;

import com.todaylunch.auction.application.usecase.AuctionSearchUseCase;
import com.todaylunch.auction.domain.entity.Auction;
import com.todaylunch.auction.domain.enumtype.AuctionStatus;
import com.todaylunch.auction.domain.repository.AuctionRepository;
import com.todaylunch.auction.presentation.dto.response.AuctionResponse;
import com.todaylunch.auction.presentation.dto.response.PagedResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuctionSearchService implements AuctionSearchUseCase {

    private static final int MAX_PAGE_SIZE = 100;

    private final AuctionRepository auctionRepository;

    @Override
    public AuctionResponse findById(UUID auctionId) {
        Auction auction = auctionRepository.findById(auctionId);
        return AuctionResponse.from(auction);
    }

    @Override
    public PagedResponse<AuctionResponse> search(AuctionStatus status, int page, int size) {
        Page<Auction> result = findAuctions(status, page, size);

        List<AuctionResponse> items = result.getContent().stream()
                .map(AuctionResponse::from)
                .toList();

        return new PagedResponse<>(
                items,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.hasNext()
        );
    }

    @Override
    public PagedResponse<AuctionResponse> searchBySeller(UUID sellerId, AuctionStatus status, int page, int size) {
        Page<Auction> result = findSellerAuctions(sellerId, status, page, size);

        List<AuctionResponse> items = result.getContent().stream()
                .map(AuctionResponse::from)
                .toList();

        return new PagedResponse<>(
                items,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.hasNext()
        );
    }

    private Page<Auction> findAuctions(AuctionStatus status, int page, int size) {
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

        PageRequest pageRequest = PageRequest.of(
                normalizedPage,
                normalizedSize,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return auctionRepository.findAllByStatus(status, pageRequest);
    }

    private Page<Auction> findSellerAuctions(UUID sellerId, AuctionStatus status, int page, int size) {
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

        PageRequest pageRequest = PageRequest.of(
                normalizedPage,
                normalizedSize,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return auctionRepository.findAllBySellerIdAndStatus(sellerId, status, pageRequest);
    }
}
