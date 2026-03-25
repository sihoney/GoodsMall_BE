package com.example.product.presentation.controller;

import com.example.product.application.usecase.ProductCreateUseCase;
import com.example.product.presentation.dto.request.ProductCreateRequest;
import com.example.product.presentation.dto.response.ProductResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 상품 API Controller
 */
@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductCreateUseCase productCreateUseCase;

    /**
     * 상품 등록 API
     *
     * 추후에 Authentication 객체에서 가져와서 sellerId를 사용할 수 있도록 진행
     * @param request  상품 등록 요청
     * @return 생성된 상품 정보 (201 Created)
     */
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductCreateRequest request
    ) {
        ProductResponse response = productCreateUseCase.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
