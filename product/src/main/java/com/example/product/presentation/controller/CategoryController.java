package com.example.product.presentation.controller;

import com.example.product.application.usecase.CategoryCreateUseCase;
import com.example.product.application.usecase.CategoryDeleteUseCase;
import com.example.product.application.usecase.CategorySearchUseCase;
import com.example.product.application.usecase.CategoryUpdateUseCase;
import com.example.product.presentation.dto.request.CategoryCreateRequest;
import com.example.product.presentation.dto.request.CategoryUpdateRequest;
import com.example.product.presentation.dto.response.CategoryResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 카테고리 API Controller
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryCreateUseCase categoryCreateUseCase;
    private final CategorySearchUseCase categorySearchUseCase;
    private final CategoryUpdateUseCase categoryUpdateUseCase;
    private final CategoryDeleteUseCase categoryDeleteUseCase;

    /**
     * 카테고리 생성 (관리자)
     * Gateway에서 JWT 검증 및 ADMIN 권한 확인 완료
     * - parentId가 없으면 대분류 생성
     * - parentId가 있으면 중/소분류 생성
     *
     * @param request 생성 요청 (parentId 선택)
     * @return 생성된 카테고리
     */
    @PostMapping("/admin")
    public ResponseEntity<CategoryResponse> createCategoryByAdmin(
            @Valid @RequestBody CategoryCreateRequest request
    ) {
        CategoryResponse response = categoryCreateUseCase.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 카테고리 생성 (판매자)
     * Gateway에서 JWT 검증 및 SELLER 권한 확인 완료
     *
     * @param sellerId 판매자 ID (Gateway에서 X-User-Id 헤더로 주입)
     * @param request  생성 요청
     * @return 생성된 카테고리
     */
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategoryBySeller(
            @RequestHeader("X-User-Id") String sellerId,
            @Valid @RequestBody CategoryCreateRequest request
    ) {
        CategoryResponse response = categoryCreateUseCase.createCategoryBySeller(sellerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 전체 카테고리 조회 (계층 구조)
     * <p>
     * depth 파라미터가 있으면 해당 depth만 조회, 없으면 전체 계층 구조 조회
     *
     * @param depth 조회할 depth (선택)
     * @return 카테고리 목록
     */
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getCategories(
            @RequestParam(required = false) Integer depth
    ) {
        if (depth != null) {
            List<CategoryResponse> response = categorySearchUseCase.getCategoriesByDepth(depth);
            return ResponseEntity.ok(response);
        }

        List<CategoryResponse> response = categorySearchUseCase.getAllCategories();
        return ResponseEntity.ok(response);
    }

    /**
     * 단일 카테고리 조회
     *
     * @param categoryId 카테고리 ID
     * @return 카테고리 정보
     */
    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> getCategoryById(
            @PathVariable UUID categoryId
    ) {
        CategoryResponse response = categorySearchUseCase.getCategoryById(categoryId);
        return ResponseEntity.ok(response);
    }

    /**
     * 하위 카테고리 조회
     *
     * @param categoryId 부모 카테고리 ID
     * @return 하위 카테고리 목록
     */
    @GetMapping("/{categoryId}/children")
    public ResponseEntity<List<CategoryResponse>> getChildCategories(
            @PathVariable UUID categoryId
    ) {
        List<CategoryResponse> response = categorySearchUseCase.getChildCategories(categoryId);
        return ResponseEntity.ok(response);
    }

    /**
     * 카테고리 수정 (관리자)
     * Gateway에서 JWT 검증 후 X-User-Role: ADMIN 헤더 필수
     *
     * @param categoryId 카테고리 ID
     * @param request    수정 요청
     * @return 수정된 카테고리
     */
    @PutMapping("/admin/{categoryId}")
    public ResponseEntity<CategoryResponse> updateCategoryByAdmin(
            @PathVariable UUID categoryId,
            @Valid @RequestBody CategoryUpdateRequest request
    ) {
        CategoryResponse response = categoryUpdateUseCase.updateCategory(categoryId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 카테고리 수정 (판매자)
     * Gateway에서 JWT 검증 및 SELLER 권한 확인 완료
     * 리소스 소유권 검증: 자신이 생성한 카테고리만 수정 가능
     *
     * @param categoryId 카테고리 ID
     * @param sellerId   판매자 ID (Gateway에서 X-User-Id 헤더로 주입)
     * @param request    수정 요청
     * @return 수정된 카테고리
     */
    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> updateCategoryBySeller(
            @PathVariable UUID categoryId,
            @RequestHeader("X-User-Id") String sellerId,
            @Valid @RequestBody CategoryUpdateRequest request
    ) {
        CategoryResponse response = categoryUpdateUseCase.updateCategoryBySeller(categoryId, sellerId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 카테고리 삭제
     *
     * @param categoryId 카테고리 ID
     * @return 204 No Content
     */
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable UUID categoryId
    ) {
        categoryDeleteUseCase.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }
}
