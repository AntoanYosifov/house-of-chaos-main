package com.antdevrealm.housechaosmain.order.repository;

import com.antdevrealm.housechaosmain.order.model.entity.OrderEntity;
import com.antdevrealm.housechaosmain.order.model.entity.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItemEntity, UUID> {
    List<OrderItemEntity> findAllByOrder(OrderEntity orderEntity);
    void deleteAllByOrder(OrderEntity orderEntity);
}
