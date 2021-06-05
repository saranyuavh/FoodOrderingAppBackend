package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.ItemDAO;
import com.upgrad.FoodOrderingApp.service.dao.OrderItemDAO;
import com.upgrad.FoodOrderingApp.service.entity.ItemEntity;
import com.upgrad.FoodOrderingApp.service.entity.OrderItemEntity;
import com.upgrad.FoodOrderingApp.service.entity.OrdersEntity;
import com.upgrad.FoodOrderingApp.service.exception.ItemNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

import javax.transaction.Transactional;
import java.util.List;

public class ItemService {
    @Autowired
    private OrderItemDAO orderItemDAO;

    @Autowired
    ItemDAO itemDAO;
    public List<OrderItemEntity> getItemsByOrder(OrdersEntity orderEntity) {
        return orderItemDAO.getItemsByOrder(orderEntity);
    }

    @Transactional
    public ItemEntity getItemEntityByUuid(final String itemUuid) throws ItemNotFoundException {

        ItemEntity itemEntity = itemDAO.getItemByUuid(itemUuid);
        if (itemEntity == null) {
            throw new ItemNotFoundException("INF-003", "No item by this id exist");
        } else {
            return itemEntity;
        }
    }
}
