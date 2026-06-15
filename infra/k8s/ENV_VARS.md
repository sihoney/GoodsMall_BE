# K8s 환경변수 인터페이스

## Table of Contents

- [공통 (infra/common)](#공통-infracommon)
  - [ConfigMap (`common-config`)](#configmap-common-config)
  - [Secret (`common-secret`)](#secret-common-secret)
- [AI 서비스 (port: 8088)](#ai-서비스-port-8088)
  - [ConfigMap (`ai-config`)](#configmap-ai-config)
  - [Secret (`ai-secret`)](#secret-ai-secret)
- [Auction 서비스 (port: 8090)](#auction-서비스-port-8090)
  - [ConfigMap (`auction-config`)](#configmap-auction-config)
  - [Secret (`auction-secret`)](#secret-auction-secret)
- [Cart 서비스 (port: 8086)](#cart-서비스-port-8086)
- [Gateway 서비스 (port: 8080)](#gateway-서비스-port-8080)
  - [ConfigMap (`gateway-config`)](#configmap-gateway-config)
  - [Secret (`gateway-secret`)](#secret-gateway-secret)
- [Member 서비스 (port: 8083)](#member-서비스-port-8083)
  - [ConfigMap (`member-config`)](#configmap-member-config)
  - [Secret (`member-secret`)](#secret-member-secret)
- [Notification 서비스 (port: 8087)](#notification-서비스-port-8087)
- [Order 서비스 (port: 8084)](#order-서비스-port-8084)
  - [ConfigMap (`order-config`)](#configmap-order-config)
  - [Secret (`order-secret`)](#secret-order-secret)
- [Payment 서비스 (port: 8082)](#payment-서비스-port-8082)
  - [ConfigMap (`payment-config`)](#configmap-payment-config)
  - [Secret (`payment-secret`)](#secret-payment-secret)
- [Product 서비스 (port: 8081)](#product-서비스-port-8081)
  - [ConfigMap (`product-config`)](#configmap-product-config)
  - [Secret (`product-secret`)](#secret-product-secret)
- [Settlement 서비스 (port: 8085)](#settlement-서비스-port-8085)

각 서비스의 `application.yml`에서 참조하는 환경변수와 `infra/k8s/**/configmap.yaml`, `secret.example`의 매핑을 정리한다.
---

## 공통 (infra/common)

모든 서비스 Deployment는 `envFrom`으로 공통 값을 받는다.

### ConfigMap (`common-config`)

| 환경변수 | k8s 값 | 로컬 기본값 | 사용 서비스 |
|---|---|---|---|
| `DB_URL` | `jdbc:postgresql://postgres:5432/goods_mall` | `jdbc:postgresql://localhost:5432/goods_mall` | 전체 |
| `KAFKA_BOOTSTRAP_SERVERS` | `kafka:9092` | `localhost:29092` 또는 `localhost:9092` | 전체 |
| `REDIS_HOST` | `redis` | `localhost` | ai, gateway, member, payment |
| `REDIS_PORT` | `6379` | `6379` | ai, gateway, member, payment |

### Secret (`common-secret`)

| 환경변수 | 설명 | 사용 서비스 |
|---|---|---|
| `DB_USER_NAME` | PostgreSQL 계정 | 전체 |
| `DB_USER_PASSWORD` | PostgreSQL 비밀번호 | 전체 |

---

## AI 서비스 (port: 8088)

### ConfigMap (`ai-config`)

| 환경변수 | k8s 값 | 로컬 기본값 |
|---|---|---|
| `AI_PRODUCT_API_BASE_URL` | `http://product:8081` | `http://localhost:8081` |

### Secret (`ai-secret`)

| 환경변수 | 설명 |
|---|---|
| `OPENAI_API_KEY` | OpenAI API 키 |

---

## Auction 서비스 (port: 8090)

### ConfigMap (`auction-config`)

| 환경변수 | k8s 값 | 로컬 기본값 |
|---|---|---|
| `ELASTICSEARCH_URI` | `http://elasticsearch:9200` | `http://localhost:9200` |
| `SERVICES_PAYMENT_URL` | `http://payment:8082` | `http://localhost:8082` |
| `WEBSOCKET_ALLOWED_ORIGINS` | `http://localhost:5173,https://www.goods-mall.shop,https://goodsmall.vercel.app,https://3.36.235.7.nip.io` | 별도 기본값 없음 |
| `HIKARI_MAX_POOL_SIZE` | `30` | `5` |

### Secret (`auction-secret`)

| 환경변수 | 설명 |
|---|---|
| `AWS_ACCESS_KEY` | AWS access key |
| `AWS_SECRET_KEY` | AWS secret key |

---

## Cart 서비스 (port: 8086)

공통 ConfigMap/Secret만 사용한다. 별도 `cart-config`, `cart-secret`은 없다.

---

## Gateway 서비스 (port: 8080)

### ConfigMap (`gateway-config`)

| 환경변수 | k8s 값 | 로컬 기본값 |
|---|---|---|

### Secret (`gateway-secret`)

| 환경변수 | 설명 |
|---|---|
| `JWT_SECRET_KEY` | JWT 서명 secret |

---

## Member 서비스 (port: 8083)

### ConfigMap (`member-config`)

| 환경변수 | k8s 값 | 로컬 기본값 |
|---|---|---|
| `API_GATEWAY_HOST` | `http://gateway:8080` | `http://localhost:8080` |
| `FRONTEND_BASE_URL` | `https://www.goods-mall.shop` | `http://localhost:5173` |
| `SERVICES_ORDER_URL` | `http://order:8084` | `http://localhost:8084` |
| `SERVICES_PAYMENT_URL` | `http://payment:8082` | `http://localhost:8082` |
| `SERVICES_PRODUCT_URL` | `http://product:8081` | `http://localhost:8081` |
| `SERVICES_AUCTION_URL` | `http://auction:8090` | `http://localhost:8090` |
| `SERVICES_SETTLEMENT_URL` | `http://settlement:8085` | `http://localhost:8085` |
| `MEMBER_SIGNUP_REQUIRE_EMAIL_VERIFICATION` | `true` | `false` |
| `EMAIL_PROVIDER` | `smtp` | `logging` |
| `SMTP_HOST` | `live.smtp.mailtrap.io` | `localhost` |
| `SMTP_PORT` | `587` | `1025` |
| `KAKAO_CLIENT_ID` | `40bcc2744d37da5ed008a7a22fe45b7a` | 빈값 |

### Secret (`member-secret`)

| 환경변수 | 설명 |
|---|---|
| `JWT_SECRET_KEY` | JWT 서명 secret |
| `AWS_ACCESS_KEY_ID` | AWS access key |
| `AWS_SECRET_ACCESS_KEY` | AWS secret key |
| `KAKAO_CLIENT_SECRET` | Kakao OAuth secret |
| `SMTP_USERNAME` | SMTP 인증 사용자명 |
| `SMTP_PASSWORD` | SMTP 인증 비밀번호 |
| `ACCOUNT_VERIFICATION_SECRET_KEY` | 계정 인증 보호 키 |

---

## Notification 서비스 (port: 8087)

공통 ConfigMap/Secret만 사용한다. `notification-config`, `notification-secret`은 비어 있다.

---

## Order 서비스 (port: 8084)

### ConfigMap (`order-config`)

| 환경변수 | k8s 값 | 로컬 기본값 |
|---|---|---|
| `SERVICES_PAYMENT_URL` | `http://payment:8082` | `http://localhost:8082` |
| `SERVICES_PRODUCT_URL` | `http://product:8081` | `http://localhost:8081` |
| `SWEET_TRACKER_API_BASE_URL` | `https://info.sweettracker.co.kr` | `https://info.sweettracker.co.kr` |

### Secret (`order-secret`)

| 환경변수 | 설명 |
|---|---|
| `SWEET_TRACKER_API_KEY` | 스윗트래커 API 키 |

---

## Payment 서비스 (port: 8082)

### ConfigMap (`payment-config`)

| 환경변수 | k8s 값 | 로컬 기본값 |
|---|---|---|
| `SERVICES_ORDER_URL` | `http://order:8084` | `http://localhost:8084` |
| `FRONTEND_BASE_URL` | `https://www.goods-mall.shop` | `http://localhost:5173` |
| `TOSS_PAYMENTS_CLIENT_KEY` | `test_ck_DLJOpm5QrldyeOMlna9QrPNdxbWn` | `test-client-key` |
| `HIKARI_MAX_POOL_SIZE` | `20` | `5` |

### Secret (`payment-secret`)

| 환경변수 | 설명 |
|---|---|
| `TOSS_PAYMENTS_SECRET_KEY` | Toss Payments secret key |
| `PAYMENT_WITHDRAW_CRYPTO_SECRET_KEY` | 출금 보호 키 |

---

## Product 서비스 (port: 8081)

### ConfigMap (`product-config`)

| 환경변수 | k8s 값 | 로컬 기본값 |
|---|---|---|
| `ELASTICSEARCH_URI` | `http://elasticsearch:9200` | `http://localhost:9200` |

### Secret (`product-secret`)

| 환경변수 | 설명 |
|---|---|
| `AWS_ACCESS_KEY` | AWS access key |
| `AWS_SECRET_KEY` | AWS secret key |

---

## Settlement 서비스 (port: 8085)

공통 ConfigMap/Secret만 사용한다. 별도 `settlement-config`, `settlement-secret`은 없다.
