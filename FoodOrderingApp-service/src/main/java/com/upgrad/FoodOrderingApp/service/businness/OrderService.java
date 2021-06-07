package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.AddressDAO;
import com.upgrad.FoodOrderingApp.service.dao.OrderDAO;
import com.upgrad.FoodOrderingApp.service.dao.OrderItemDAO;
import com.upgrad.FoodOrderingApp.service.entity.*;
import com.upgrad.FoodOrderingApp.service.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {
    @Autowired
    private OrderDAO orderDao;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private AddressService addressService;

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
    public CouponEntity getCouponByName(String couponName, final String authorizationToken) throws AuthorizationFailedException {
        customerService.validateAccessToken(authorizationToken);
        return orderDao.getCouponByName(couponName);
    }

    @Transactional
    public List<OrdersEntity> getCustomerOrders(final CustomerEntity customerEntity) {

        return orderDao.getCustomerOrders(customerEntity);
    }

    @Transactional
    public CouponEntity getCouponByUuid(final String couponUuid) {
        return orderDao.getCouponByUuid(couponUuid);
    }

    @Transactional
    public OrdersEntity saveOrder(String addressId, String couponId, String restaurantId, String paymentId, BigDecimal bill,BigDecimal discount,List<OrderItemEntity> orderItemEntityList, final String authorizationToken)
            throws AuthorizationFailedException, CouponNotFoundException, AddressNotFoundException,
            PaymentMethodNotFoundException, RestaurantNotFoundException, ItemNotFoundException {

        customerService.validateAccessToken(authorizationToken);

        CustomerEntity customerEntity = customerService.getCustomer(authorizationToken);

        AddressEntity addressEntity = addressService.getAddressByUUID(addressId, customerEntity);

        CouponEntity couponEntity = getCouponByUuid(couponId);

        RestaurantEntity restaurantEntity = restaurantService.getRestaurantByUUId(restaurantId.toString());

        PaymentEntity paymentEntity = paymentService.getPaymentByUuid(paymentId);

        if (couponEntity == null) {
            throw new CouponNotFoundException("CPF-002", "No coupon by this id");
        } else if (addressEntity == null) {
            throw new AddressNotFoundException("ANF-003", "No address by this id");
        } else if (paymentEntity ==  null) {
            throw new PaymentMethodNotFoundException("PNF-002", "No payment method found by this id");
        } else if (restaurantEntity == null) {
            throw new RestaurantNotFoundException("RNF-001", "No restaurant by this id");
        }

        final ZonedDateTime now = ZonedDateTime.now();

        OrdersEntity ordersEntity = new OrdersEntity();
        ordersEntity.setUuid(UUID.randomUUID().toString());
        ordersEntity.setCoupon(couponEntity);
        ordersEntity.setRestaurant(restaurantEntity);
        ordersEntity.setCustomer(customerEntity);
        ordersEntity.setAddress(addressEntity);
        ordersEntity.setBill(bill);
        ordersEntity.setDiscount(discount);
        ordersEntity.setDate(now);

        OrdersEntity savedOrderEntity = orderDao.saveOrder(ordersEntity);

        for (OrderItemEntity orderItemEntity :orderItemEntityList) {
            orderItemEntity.setOrders(savedOrderEntity);
            orderItemDAO.createOrderItemEntity(orderItemEntity);
        }

        return orderDao.saveOrder(savedOrderEntity);
    }

}
