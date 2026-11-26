package com.example.product_service.repository;

import com.example.product_service.Entities.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {

        List<Product> findByIdIn(List<String> productIds);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("select p from Product p where p.id in :productIds")
        List<Product> findByIdInForUpdate(@Param("productIds") List<String> productIds);

        // Search với pagination: theo keyword (name) hoặc categoryId
        @Query("""
                        select p from Product p
                        where (:keyword is null or lower(p.name) like lower(concat('%', :keyword, '%')))
                          and (:categoryId is null or p.categoryId = :categoryId)
                          and (:#{#productIds == null || #productIds.isEmpty()} = true or p.id in :productIds)
                        """)
        Page<Product> searchProducts(
                        @Param("keyword") String keyword,
                        @Param("categoryId") String categoryId,
                        @Param("productIds") List<String> productIds,
                        Pageable pageable);
}
