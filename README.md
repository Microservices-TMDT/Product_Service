# Product Service

Microservice quản lý sản phẩm, danh mục và khóa tồn kho tạm thời trong hệ thống thương mại điện tử.

---

## Tổng quan kiến trúc

```
┌──────────────────────────────────────────────────────────────────┐
│                        Product Service                           │
│                                                                  │
│  REST API  ──►  ProductController                                │
│                       │                                          │
│                 ProductServiceImpl                               │
│                 ┌─────┴──────────────────────┐                  │
│                 │        Cache Layer          │                  │
│                 │  L1: Caffeine (TTL = 5s)    │                  │
│                 │  L2: Redis    (TTL = 60s)   │                  │
│                 └─────┬──────────────────────┘                  │
│                       │                                          │
│               ProductRepository (JPA / MySQL)                    │
│                                                                  │
│  Kafka Consumer ──► OrderCreatedConsumer ──► lock()             │
│  Kafka Producer ──► product.updated  (cache invalidation)       │
│                 ──► product_locked   (order saga)               │
└──────────────────────────────────────────────────────────────────┘
```

---

## Yêu cầu chức năng

| ID   | Mô tả                                                                                       | Trạng thái |
|------|---------------------------------------------------------------------------------------------|------------|
| F1.1 | Tạo / cập nhật / xóa sản phẩm qua `POST/PUT/DELETE /api/v1/products`                        | ✅          |
| F1.2 | Tìm kiếm sản phẩm cơ bản `POST /api/v1/products/search` với pagination                      | ✅          |
| F1.3 | Cache 2 tầng: **Caffeine** (L1, TTL=5s) + **Redis** (L2, TTL=60s)                           | ✅          |
| F1.4 | Cache invalidation tự động khi product update (publish `product.updated` Kafka event)       | ✅          |
| F1.5 | `PUT /api/v1/products/lock` khóa tồn kho tạm thời (Redisson `RLock`, lease time = 5s)       | ✅          |

---

## Stack công nghệ

| Thành phần       | Công nghệ                                               |
|------------------|---------------------------------------------------------|
| Framework        | Spring Boot 3.4.5, Java 21                              |
| ORM              | Spring Data JPA + Hibernate                             |
| Database         | MySQL 8                                                 |
| Cache L1         | Caffeine (in-process, TTL = 5s, maxSize = 500)          |
| Cache L2         | Redis (distributed, TTL = 60s)                          |
| Distributed Lock | Redisson `RLock` (lease = 5s, waitTime = 10s)           |
| Messaging        | Apache Kafka                                            |
| Service Registry | Netflix Eureka Client                                   |
| Mapping          | MapStruct 1.6.3                                         |
| Utilities        | Lombok                                                  |

---

## API Endpoints

### Products `/api/v1/products`

| Method   | Path                        | Mô tả                                               |
|----------|-----------------------------|-----------------------------------------------------|
| `POST`   | `/api/v1/products`          | Tạo sản phẩm mới                                    |
| `PUT`    | `/api/v1/products/{id}`     | Cập nhật sản phẩm (trigger cache evict + Kafka)      |
| `DELETE` | `/api/v1/products/{id}`     | Xóa sản phẩm (trigger cache evict)                  |
| `GET`    | `/api/v1/products`          | Lấy toàn bộ sản phẩm (không cache)                  |
| `POST`   | `/api/v1/products/search`   | Tìm kiếm theo danh sách ID (backward-compat)         |
| `POST`   | `/api/v1/products/search/page` | Tìm kiếm nâng cao với pagination                 |
| `PUT`    | `/api/v1/products/lock`     | Khóa tồn kho tạm thời (Redisson RLock, lease 5s)    |

### Categories `/api/v1/categories`

| Method | Path                    | Mô tả           |
|--------|-------------------------|-----------------|
| `POST` | `/api/v1/categories`    | Tạo danh mục    |
| `GET`  | `/api/v1/categories`    | Lấy danh mục    |

---

## Chiến lược Cache (F1.3 & F1.4)

```
Request
  │
  ▼
L1 Caffeine (5s TTL, in-process)
  │ MISS
  ▼
L2 Redis (60s TTL, distributed)
  │ MISS
  ▼
MySQL (source of truth)
  │
  ▼
Populate L2 → L1

ON UPDATE / DELETE:
  evict L1 (Caffeine) ──► evict L2 (Redis)
```

- **L1 `CacheManager`**: `caffeineCacheManager` (Primary)  
- **L2 `CacheManager`**: `redisCacheManager`  
- Invalidation thủ công qua helper `evictProductCache(productId)` để xóa đồng thời cả 2 tầng.  

---

## Kafka Topics

| Topic            | Producer            | Consumer                       | Mô tả                               |
|------------------|---------------------|--------------------------------|--------------------------------------|
| `order_created`  | order-service       | `OrderCreatedConsumer`         | Trigger khóa tồn kho                 |
| `product_locked` | product-service     | order-service                  | Báo lock thành công                  |

---

## Khởi chạy

### Yêu cầu
- Java 21+
- Maven 3.9+
- MySQL 8 đang chạy tại `localhost:3306`
- Redis đang chạy tại `localhost:6379`
- Kafka đang chạy tại `localhost:9092`
- Eureka Server tại `localhost:8761`

### Chạy local
```bash
mvn clean install -DskipTests

mvn spring-boot:run

java -jar target/product_service-0.0.1-SNAPSHOT.jar
```

Service khởi động tại **http://localhost:8083**

### Cấu hình môi trường (`application.yaml`)

| Property                        | Default                            | Mô tả                |
|---------------------------------|------------------------------------|----------------------|
| `server.port`                   | `8083`                             | Port HTTP            |
| `spring.datasource.url`         | `jdbc:mysql://localhost:3306/...`  | MySQL URL            |
| `spring.data.redis.host`        | `localhost`                        | Redis host           |
| `spring.data.redis.port`        | `6379`                             | Redis port           |
| `spring.kafka.bootstrap-servers`| `localhost:9092`                   | Kafka brokers        |
| `eureka.client.service-url`     | `http://localhost:8761/eureka/`    | Eureka server        |

---


