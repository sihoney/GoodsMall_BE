package com.example.order.application.port;

import com.example.order.application.port.dto.request.ProductStockDeductRequest;
import com.example.order.application.port.dto.response.ProductInfo;

import java.util.List;

public interface ProductPort {

    List<ProductInfo> deductStock(List<ProductStockDeductRequest> requests);
}
