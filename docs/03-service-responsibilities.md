# Service Responsibilities

## Overview

이 프로젝트는 도메인 책임 분리와 서비스 독립성을 확보하기 위해 MSA 구조를 사용하였다.

각 서비스는 자신의 상태(state)를 소유하며, 서비스 간 상태 변화는 이벤트 기반으로 연결된다.

---

## Service Responsibility Map

| Service | Main Responsibility | Owned State |
|---|---|---|
| Gateway | 요청 라우팅 / 인증 처리 | - |
| Member | 회원 인증 / 사용자 식별 | Member, Seller, Verification |
| Product | 상품 카탈로그 / 재고 관리 | Product, Category, ProductImage, Stock |
| Cart | 장바구니 / 찜 관리 | Cart, CartItem, Wish |
| Auction | 경매 생명주기 / 입찰 관리 | Auction, Bid |
| Order | 주문 생명주기 / 배송 / 클레임 관리 | Order, OrderItem, Delivery, Claim |
| Payment | 결제 / 지갑 / 에스크로 관리 | Payment, Wallet, Escrow, Refund |
| Settlement | 판매자 정산 처리 | Settlement, SettlementItem |
| Notification | 알림 생성 / 전달 | Notification |
| AI | 상품 추천 / 상품 등록 보조 | Embedding, Recommendation Data |
