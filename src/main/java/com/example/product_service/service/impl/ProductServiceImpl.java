package com.example.product_service.service.impl;

import com.example.product_service.Entities.Product;
import com.example.product_service.dto.request.CreateProductRequest;
import com.example.product_service.dto.request.LockProductItem;
import com.example.product_service.dto.request.LockProductRequet;
import com.example.product_service.dto.request.ProductFilter;
import com.example.product_service.dto.response.ProductResponse;
import com.example.product_service.exception.ApplicationException;
import com.example.product_service.mapper.ProductMapper;
import com.example.product_service.repository.CategoryRepository;
import com.example.product_service.repository.ProductRepository;
import com.example.product_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
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

    @Override
    @Transactional
    public ProductResponse createProduct(CreateProductRequest productRequest) {
        categoryRepository.findById(productRequest.getCategoryId())
                .orElseThrow(() -> new ApplicationException("Category not found"));

        Product product = productMapper.toProduct(productRequest);
        return productMapper.toProductResponse(productRepository.save(product));
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
    @Transactional
    public void lock(LockProductRequet lockProductRequet) {
        List<LockProductItem> items = lockProductRequet.getItems();

        List<String> storredIds = items.stream()
                .map(LockProductItem::getId)
                .sorted()
                .toList();

        String lockKey = "lock:products:" + String.join(",",storredIds);
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if(lock.tryLock(10, 5, TimeUnit.SECONDS)) {
                Thread.sleep(4000);
                log.info("Acquired Redis lock for: ", lockKey);
                var productIdQuantityMap = items.stream()
                        .collect(Collectors.toMap(LockProductItem::getId, LockProductItem::getQuantity));

                List<Product> products = productRepository.findByIdInForUpdate(
                        new ArrayList<>(productIdQuantityMap.keySet())
                );

                if (products.isEmpty()) {
                    throw new RuntimeException("Product not found");
                }

                products.forEach(product -> {
                    Integer quantityToLock = productIdQuantityMap.get(product.getId());
                    if (product.getStock() < quantityToLock) {
                        throw new RuntimeException("Not enough stock for product: " + product.getId());
                    }
                    product.setStock(product.getStock() - quantityToLock);
                });

                productRepository.saveAll(products);
            } else {
                throw new RuntimeException("Server busy");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            log.info("Waiting for unlock", lockKey);
            if (lock.isHeldByCurrentThread()){
                lock.unlock();
                log.info("Unlock success for ", lockKey);
            }
        }


    }

}