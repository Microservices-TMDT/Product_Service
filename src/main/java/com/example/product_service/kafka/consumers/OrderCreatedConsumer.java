package com.example.product_service.kafka.consumers;

import com.example.product_service.dto.request.LockProductItem;
import com.example.product_service.dto.request.LockProductRequet;
import com.example.product_service.kafka.event.OrderCreatedEvent;
import com.example.product_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderCreatedConsumer {

    private final ProductService productService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "order_created")
//    @RetryableTopic(
//            attempts = "4",
//            backoff = @Backoff(delay = 2000, multiplier = 2.0),
//            exclude = {NullPointerException.class, IllegalAccessException.class}
//    )
    public void handlerOrderCreatedEvent(OrderCreatedEvent orderCreatedEvent) {
        log.info("Receive order message {}", orderCreatedEvent);

//        if (orderCreatedEvent != null) {
//            throw new RuntimeException("test non-blocking");
//        }

        List<LockProductItem> lockProductItems = new ArrayList<>();

        orderCreatedEvent.getItems().forEach(orderItem -> {
            LockProductItem lockProductItem = new LockProductItem();
            lockProductItem.setId(orderItem.getProductId());
            lockProductItem.setQuantity(orderItem.getQuantity());
            lockProductItems.add(lockProductItem);
        });

        LockProductRequet lockProductRequet = new LockProductRequet();
        lockProductRequet.setItems(lockProductItems);

        productService.lock(lockProductRequet);
        log.info("Success to lock product item of {}", orderCreatedEvent.getId());

        // Publish event để order-service biết lock thành công => NEW → PREPARED
        kafkaTemplate.send("product_locked", orderCreatedEvent.getId());
        log.info("Published product_locked event for order {}", orderCreatedEvent.getId());
    }
}

