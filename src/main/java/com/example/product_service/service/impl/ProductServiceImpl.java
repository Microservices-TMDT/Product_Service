package com.example.product_service.service.impl;

import com.example.product_service.Entities.Product;
import com.example.product_service.config.CacheConfig;
import com.example.product_service.dto.request.CreateProductRequest;
import com.example.product_service.dto.request.LockProductItem;
import com.example.product_service.dto.request.LockProductRequet;
import com.example.product_service.dto.request.ProductFilter;
import com.example.product_service.dto.request.SearchProductRequest;
import com.example.product_service.dto.request.UpdateProductRequest;
import com.example.product_service.dto.response.PageResponse;
import com.example.product_service.dto.response.ProductResponse;
import com.example.product_service.exception.ApplicationException;
import com.example.product_service.kafka.event.ProductUpdatedEvent;
import com.example.product_service.mapper.ProductMapper;
import com.example.product_service.repository.CategoryRepository;
import com.example.product_service.repository.ProductRepository;
import com.example.product_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final RedissonClient redissonClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Qualifier(CacheConfig.L1_CACHE_MANAGER)
    private final CacheManager l1CacheManager;

    @Qualifier(CacheConfig.L2_CACHE_MANAGER)
    private final CacheManager l2CacheManager;

    @Override
    @Transactional
    public ProductResponse createProduct(CreateProductRequest productRequest) {
        categoryRepository.findById(productRequest.getCategoryId())
                .orElseThrow(() -> new ApplicationException("Category not found"));

        Product product = productMapper.toProduct(productRequest);
        ProductResponse response = productMapper.toProductResponse(productRepository.save(product));
        log.info("Created product id={}", response.getId());
        return response;
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(String productId, UpdateProductRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApplicationException("Product not found: " + productId));

        categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ApplicationException("Category not found"));

        productMapper.updateProductFromRequest(request, product);
        ProductResponse response = productMapper.toProductResponse(productRepository.save(product));

        // F1.4 – Invalidate both cache tiers
        evictProductCache(productId);

        // F1.4 – Publish product.updated event
        ProductUpdatedEvent event = ProductUpdatedEvent.builder()
                .productId(productId)
                .name(response.getName())
                .price(response.getPrice())
                .stock(response.getStock())
                .categoryId(response.getCategoryId())
                .build();
        kafkaTemplate.send("product.updated", productId, event);
        log.info("Published product.updated for id={}", productId);

        return response;
    }

    @Override
    @Transactional
    public void deleteProduct(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ApplicationException("Product not found: " + productId));

        productRepository.delete(product);

        // Invalidate cache
        evictProductCache(productId);
        log.info("Deleted product id={}", productId);
    }

    @Override
    public List<ProductResponse> getProducts() {
        return productRepository.findAll()
                .stream()
                .map(productMapper::toProductResponse)
                .toList();
    }

    @Override
    public List<ProductResponse> searchProducts(ProductFilter productFilter) {
        return productRepository.findByIdIn(productFilter.getProductIds()).stream()
                .map(productMapper::toProductResponse)
                .toList();
    }

    @Override
    public PageResponse<ProductResponse> searchProducts(SearchProductRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());

        // Normalize
        String keyword = (request.getKeyword() != null && !request.getKeyword().isBlank())
                ? request.getKeyword()
                : null;
        String categoryId = (request.getCategoryId() != null && !request.getCategoryId().isBlank())
                ? request.getCategoryId()
                : null;
        List<String> ids = (request.getProductIds() != null && !request.getProductIds().isEmpty())
                ? request.getProductIds()
                : null;

        Page<Product> page = productRepository.searchProducts(keyword, categoryId, ids, pageable);

        List<ProductResponse> content = page.getContent()
                .stream()
                .map(productMapper::toProductResponse)
                .toList();

        return PageResponse.<ProductResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Override
    @Transactional
    public void lock(LockProductRequet lockProductRequet) {
        List<LockProductItem> items = lockProductRequet.getItems();

        List<String> sortedIds = items.stream()
                .map(LockProductItem::getId)
                .sorted()
                .toList();

        String lockKey = "lock:products:" + String.join(",", sortedIds);
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // waitTime=10s, leaseTime=5s (F1.5 yêu cầu lease 5s)
            if (lock.tryLock(10, 5, TimeUnit.SECONDS)) {
                log.info("Acquired Redis lock for: {}", lockKey);
                var productIdQuantityMap = items.stream()
                        .collect(Collectors.toMap(LockProductItem::getId, LockProductItem::getQuantity));

                List<Product> products = productRepository.findByIdInForUpdate(
                        new ArrayList<>(productIdQuantityMap.keySet()));

                if (products.isEmpty()) {
                    throw new ApplicationException("Product not found");
                }

                products.forEach(product -> {
                    Integer quantityToLock = productIdQuantityMap.get(product.getId());
                    if (product.getStock() < quantityToLock) {
                        throw new ApplicationException("Not enough stock for product: " + product.getId());
                    }
                    product.setStock(product.getStock() - quantityToLock);
                });

                productRepository.saveAll(products);

                // Invalidate cache for updated products
                products.forEach(p -> evictProductCache(p.getId()));

            } else {
                throw new ApplicationException("Server busy – could not acquire lock");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApplicationException("Lock interrupted: " + e.getMessage());
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("Released Redis lock for: {}", lockKey);
            }
        }
    }

    private void evictProductCache(String productId) {
        // L1
        Objects.requireNonNull(l1CacheManager.getCache(CacheConfig.CACHE_PRODUCT))
                .evict(productId);
        Objects.requireNonNull(l1CacheManager.getCache(CacheConfig.CACHE_PRODUCTS))
                .clear();
        // L2
        Objects.requireNonNull(l2CacheManager.getCache(CacheConfig.CACHE_PRODUCT))
                .evict(productId);
        Objects.requireNonNull(l2CacheManager.getCache(CacheConfig.CACHE_PRODUCTS))
                .clear();
        log.debug("Evicted cache for productId={}", productId);
    }
}
