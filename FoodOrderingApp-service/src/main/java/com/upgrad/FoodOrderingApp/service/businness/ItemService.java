package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.OrderItemDAO;
import com.upgrad.FoodOrderingApp.service.entity.OrderItemEntity;
import com.upgrad.FoodOrderingApp.service.entity.OrdersEntity;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class ItemService {
    @Autowired
    private OrderItemDAO orderItemDAO;

    public List<OrderItemEntity> getItemsByOrder(OrdersEntity orderEntity) {
        return orderItemDAO.getItemsByOrder(orderEntity);
    }
}
