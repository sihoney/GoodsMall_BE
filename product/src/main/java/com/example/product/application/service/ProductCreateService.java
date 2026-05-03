package com.example.product.application.service;

import com.example.product.application.usecase.ProductCreateUseCase;
import com.example.product.domain.entity.Product;
import com.example.product.domain.entity.ProductImage;
import com.example.product.domain.repository.CategoryRepository;
import com.example.product.domain.repository.ProductImageRepository;
import com.example.product.domain.repository.ProductRepository;
import com.example.product.domain.repository.ProductSearchRepository;
import com.example.product.domain.repository.FileStorageRepository;
import com.example.product.infrastructure.messaging.kafka.ProductOutboxEventService;
import com.example.product.presentation.dto.request.ProductCreateRequest;
import com.example.product.presentation.dto.response.ProductResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductCreateService implements ProductCreateUseCase {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final FileStorageRepository fileStorageRepository;
    private final ProductOutboxEventService productOutboxEventService;
    private final ProductSearchRepository productSearchRepository;

    @Override
    public ProductResponse createProduct(
            String sellerId,
            ProductCreateRequest request,
            MultipartFile[] images,
            Integer thumbnailIndex
    ) {
        Product product = Product.create(
                sellerId,
                request.title(),
                request.description(),
                request.price(),
                request.stockQuantity(),
                categoryRepository.findById(request.categoryId()),
                request.type()
        );

        Product savedProduct = productRepository.save(product);
        log.info("Product created: productId={}", savedProduct.getProductId());

        List<ProductImage> savedImages = uploadImages(savedProduct.getProductId(), images, thumbnailIndex);

        indexToEsDirectly(savedProduct, savedImages);

        productOutboxEventService.saveCreatedEvent(savedProduct);

        // Presigned URL 생성하여 응답 생성
        return buildProductResponse(savedProduct, savedImages);
    }

    /**
     * ES 직접 인덱싱: 등록 즉시 검색 listing 에 반영되도록 동기 인덱싱한다.
     * Kafka outbox 경로는 보조(타 서비스 알림 + 실패 시 fallback)로만 사용.
     * ES 실패 시에도 등록은 성공시키고, 실패는 로그로 남겨 reconciliation 잡이 복구하도록 위임.
     */
    private void indexToEsDirectly(Product product, List<ProductImage> images) {
        try {
            String thumbnailS3Key = images.stream()
                    .filter(ProductImage::isThumbnail)
                    .map(ProductImage::getS3Key)
                    .findFirst()
                    .orElse(null);

            List<String> categoryIds = product.getCategory().collectIdHierarchy().stream()
                    .map(UUID::toString)
                    .toList();

            productSearchRepository.index(product, categoryIds, thumbnailS3Key);
            log.info("ES 직접 인덱싱 완료: productId={}", product.getProductId());
        } catch (Exception e) {
            log.error("ES 직접 인덱싱 실패 - outbox/reconciliation 으로 복구 위임: productId={}",
                    product.getProductId(), e);
        }
    }

    /**
     * ProductResponse 생성 (Presigned URL 포함)
     */
    private ProductResponse buildProductResponse(Product product, List<ProductImage> images) {
        List<com.example.product.presentation.dto.response.ProductImageResponse> imageResponses = images.stream()
                .map(image -> {
                    String presignedUrl = fileStorageRepository.generatePresignedUrl(image.getS3Key());
                    return com.example.product.presentation.dto.response.ProductImageResponse.from(image, presignedUrl);
                })
                .toList();

        return new ProductResponse(
                product.getProductId(),
                product.getSellerId(),
                product.getTitle(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getStatus(),
                product.getType(),
                product.getCategory().getCategoryId(),
                product.getCategory().getName(),
                product.getCreatedAt(),
                imageResponses
        );
    }

    /**
     * 이미지 업로드 및 저장
     * S3 업로드 실패 시 이미 업로드된 파일 정리
     */
    private List<ProductImage> uploadImages(UUID productId, MultipartFile[] images, Integer thumbnailIndex) {
        List<ProductImage> savedImages = new ArrayList<>();
        List<String> uploadedS3Keys = new ArrayList<>();

        try {
            for (int i = 0; i < images.length; i++) {
                MultipartFile file = images[i];

                String s3Key = fileStorageRepository.uploadImage(file);
                uploadedS3Keys.add(s3Key);  // 성공한 키 저장
                log.info("Image uploaded to S3: productId={}, s3Key={}, index={}", productId, s3Key, i);

                boolean isThumbnail = (i == thumbnailIndex);
                ProductImage productImage = ProductImage.create(
                        UUID.randomUUID(),
                        productId,
                        s3Key,
                        i,  // sortOrder는 배열 인덱스로 설정
                        isThumbnail,
                        LocalDateTime.now()
                );

                ProductImage savedImage = productImageRepository.save(productImage);
                savedImages.add(savedImage);

                log.info("ProductImage saved: imageId={}, isThumbnail={}", savedImage.getImageId(), isThumbnail);
            }

        } catch (Exception e) {
            // 에러 발생 시 S3에 업로드된 파일들 정리
            log.error("Failed to upload images, cleaning up S3. productId={}, uploadedCount={}",
                    productId, uploadedS3Keys.size(), e);
            cleanupS3Images(uploadedS3Keys);

            throw new RuntimeException("이미지 업로드 중 오류가 발생했습니다", e);
        }

        return savedImages;
    }

    private void cleanupS3Images(List<String> s3Keys) {
        for (String s3Key : s3Keys) {
            try {
                fileStorageRepository.deleteImage(s3Key);
                log.info("Cleaned up S3 image: {}", s3Key);
            } catch (Exception e) {
                log.error("Failed to cleanup S3 image: {}", s3Key, e);
            }
        }
    }
}
