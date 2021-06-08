package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.AddressDAO;
import com.upgrad.FoodOrderingApp.service.dao.OrderDAO;
import com.upgrad.FoodOrderingApp.service.dao.OrderItemDAO;
import com.upgrad.FoodOrderingApp.service.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class OrderService {
    @Autowired
    private OrderDAO orderDao;

    @Autowired
    private CustomerService customerService;

    @Autowired
    AddressDAO addressDAO;

    @Autowired
    ItemService itemService;

    @Autowired
    OrderItemDAO orderItemDAO;

    @Autowired
    RestaurantService restaurantService;

    @Autowired
    PaymentService paymentService;

    @Transactional
    public CouponEntity getCouponByCouponName(String couponName){
        return orderDao.getCouponByName(couponName);
    }

    @Transactional
    public List<OrderEntity> getOrdersByCustomers(final String uuid) {
        CustomerEntity customerEntity = customerService.getCustomerByUUID(uuid);
        return orderDao.getCustomerOrders(customerEntity);
    }

    @Transactional
    public CouponEntity getCouponByCouponId(final String couponUuid) {
        return orderDao.getCouponByUuid(couponUuid);
    }

    @Transactional
    public OrderEntity saveOrderItem(OrderEntity savedOrderEntity){
        return orderDao.saveOrder(savedOrderEntity);
    }


    @Transactional
    public OrderItemEntity createOrderItemEntity(OrderItemEntity orderItemEntity) {
        return orderItemDAO.createOrderItemEntity(orderItemEntity);
    }
}
