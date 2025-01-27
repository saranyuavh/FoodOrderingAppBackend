package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.OrderItemEntity;
import com.upgrad.FoodOrderingApp.service.entity.OrderEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class OrderItemDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public List<OrderItemEntity> getItemsByOrder(OrderEntity order) {
        try {
            return entityManager.createNamedQuery("itemsByOrder", OrderItemEntity.class).setParameter("order", order)
                    .getResultList();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public OrderItemEntity createOrderItemEntity(OrderItemEntity orderItemEntity) {
        entityManager.persist(orderItemEntity);
        return orderItemEntity;
    }
}
