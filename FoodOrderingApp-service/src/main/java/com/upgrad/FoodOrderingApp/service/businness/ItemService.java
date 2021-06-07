package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.ItemDAO;
import com.upgrad.FoodOrderingApp.service.dao.OrderDAO;
import com.upgrad.FoodOrderingApp.service.dao.OrderItemDAO;
import com.upgrad.FoodOrderingApp.service.entity.ItemEntity;
import com.upgrad.FoodOrderingApp.service.entity.OrderItemEntity;
import com.upgrad.FoodOrderingApp.service.entity.OrdersEntity;
import com.upgrad.FoodOrderingApp.service.entity.RestaurantEntity;
import com.upgrad.FoodOrderingApp.service.exception.ItemNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

@Service
public class ItemService {
    @Autowired
    private OrderItemDAO orderItemDAO;

    @Autowired
    ItemDAO itemDAO;

    @Autowired
    OrderDAO orderDAO;
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

    @Transactional
    public List<ItemEntity> getItemsByPopularity(RestaurantEntity restaurantEntity) {

        List<ItemEntity> itemEntityList = new ArrayList<>();

        for (OrdersEntity orderEntity : orderDAO.getOrdersByRestaurant(restaurantEntity)) {
            for (OrderItemEntity orderItemEntity : orderItemDAO.getItemsByOrder(orderEntity)) {
                itemEntityList.add(orderItemEntity.getItem());
            }
        }

        Map<String, Integer> map = new HashMap<String, Integer>();
        for (ItemEntity itemEntity : itemEntityList) {
            Integer count = map.get(itemEntity.getUuid());
            map.put(itemEntity.getUuid(), (count == null) ? 1 : count + 1);
        }

        Map<String, Integer> treeMap = new TreeMap<String, Integer>(map);
        List<ItemEntity> sortedItemEntityList = new ArrayList<ItemEntity>();
        for (Map.Entry<String, Integer> entry : treeMap.entrySet()) {
            sortedItemEntityList.add(itemDAO.getItemByUuid(entry.getKey()));
        }

        Collections.reverse(sortedItemEntityList);

        return sortedItemEntityList;
    }
}
