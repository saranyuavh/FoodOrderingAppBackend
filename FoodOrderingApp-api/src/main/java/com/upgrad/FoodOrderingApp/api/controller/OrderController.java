package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.*;
import com.upgrad.FoodOrderingApp.service.businness.CustomerService;
import com.upgrad.FoodOrderingApp.service.businness.ItemService;
import com.upgrad.FoodOrderingApp.service.businness.OrderService;
import com.upgrad.FoodOrderingApp.service.entity.*;
import com.upgrad.FoodOrderingApp.service.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping("/")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ItemService itemService;

    @RequestMapping(method = RequestMethod.GET, path = "/order/coupon/{coupon_name}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CouponDetailsResponse> getCouponByName(@RequestHeader("authorization")  final String authorization, @PathVariable("coupon_name") final String couponName)
            throws AuthorizationFailedException, CouponNotFoundException {

        String authToken = authorization.split( " ")[1];

        if(couponName == null || couponName.isEmpty() || couponName.equalsIgnoreCase("\"\"")){
            throw new CouponNotFoundException("CPF-002", "Coupon name field should not be empty");
        }

        CouponEntity couponEntity = orderService.getCouponByName(couponName, authToken);

        if (couponEntity == null) {
            throw new CouponNotFoundException("CPF-001", "No coupon by this name");
        }

        CouponDetailsResponse couponDetailsResponse = new CouponDetailsResponse().id(UUID.fromString(couponEntity.getUuid()))
                .couponName(couponEntity.getCouponName()).percent(couponEntity.getPercent());

        return new ResponseEntity<CouponDetailsResponse>(couponDetailsResponse, HttpStatus.OK);

    }

    @RequestMapping(method = RequestMethod.GET, path = "/order", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CustomerOrderResponse> getPastOrdersOfCustomer(@RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException {

        String authToken = authorization.split(" ")[1];

        customerService.validateAccessToken(authToken);

        CustomerEntity customerEntity = customerService .getCustomer(authToken);

        final List<OrdersEntity> ordersEntityList = orderService.getCustomerOrders(customerEntity);

        CustomerOrderResponse customerOrderResponse = new CustomerOrderResponse();

        for (OrdersEntity ordersEntity: ordersEntityList) {

            OrderListCustomer orderListCustomer = new OrderListCustomer();
            orderListCustomer.setId(UUID.fromString(ordersEntity.getCustomer().getUuid()));
            orderListCustomer.setFirstName(ordersEntity.getCustomer().getFirstName());
            orderListCustomer.setLastName(ordersEntity.getCustomer().getLastName());
            orderListCustomer.setContactNumber(ordersEntity.getCustomer().getContactNumber());
            orderListCustomer.setEmailAddress(ordersEntity.getCustomer().getEmail());

            OrderListAddressState orderListAddressState = new OrderListAddressState();
            orderListAddressState.setId(UUID.fromString(ordersEntity.getAddress().getState().getUuid()));
            orderListAddressState.setStateName(ordersEntity.getAddress().getState().getStateName());

            OrderListAddress orderListAddress = new OrderListAddress();
            orderListAddress.setId(UUID.fromString(ordersEntity.getAddress().getUuid()));
            orderListAddress.setFlatBuildingName(ordersEntity.getAddress().getFlatBuilNo());
            orderListAddress.setLocality(ordersEntity.getAddress().getLocality());
            orderListAddress.setCity(ordersEntity.getAddress().getCity());
            orderListAddress.setPincode(ordersEntity.getAddress().getPincode());
            orderListAddress.setState(orderListAddressState);

            OrderListCoupon orderListCoupon = new OrderListCoupon();
            orderListCoupon.setId(UUID.fromString(ordersEntity.getCoupon().getUuid()));
            orderListCoupon.setCouponName(ordersEntity.getCoupon().getCouponName());
            orderListCoupon.setPercent(ordersEntity.getCoupon().getPercent());

            OrderListPayment orderListPayment = new OrderListPayment();
            orderListPayment.setId(UUID.fromString(ordersEntity.getUuid()));
            orderListPayment.setPaymentName(ordersEntity.getPayment().getPaymentName());

            OrderList orderList = new OrderList();
            orderList.setId(UUID.fromString(ordersEntity.getUuid()));
            orderList.setDate(ordersEntity.getDate().toString());
            orderList.setAddress(orderListAddress);
            orderList.setCustomer(orderListCustomer);
            orderList.setPayment(orderListPayment);
            orderList.setCoupon(orderListCoupon);
            orderList.setBill(ordersEntity.getBill());
            orderList.setDiscount(ordersEntity.getDiscount());

            for (OrderItemEntity orderItemEntity : itemService.getItemsByOrder(ordersEntity)) {

                ItemQuantityResponseItem itemQuantityResponseItem = new ItemQuantityResponseItem();
                itemQuantityResponseItem.setId(UUID.fromString(orderItemEntity.getItem().getUuid()));
                itemQuantityResponseItem.setItemName(orderItemEntity.getItem().getItemName());
                itemQuantityResponseItem.setItemPrice(orderItemEntity.getItem().getPrice());
                itemQuantityResponseItem.setType(ItemQuantityResponseItem.TypeEnum.valueOf(orderItemEntity.getItem().getType().toString()));

                ItemQuantityResponse itemQuantityResponse = new ItemQuantityResponse();
                itemQuantityResponse.setItem(itemQuantityResponseItem);
                itemQuantityResponse.setPrice(orderItemEntity.getPrice());
                itemQuantityResponse.setQuantity(orderItemEntity.getQuantity());

                orderList.addItemQuantitiesItem(itemQuantityResponse);
            }

            customerOrderResponse.addOrdersItem(orderList);

        }

        return new ResponseEntity<CustomerOrderResponse>(customerOrderResponse, HttpStatus.OK);

    }

    @CrossOrigin
    @RequestMapping(method = RequestMethod.POST, path = "/order", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SaveOrderResponse> saveOrder(@RequestHeader("authorization") final String authorization, final SaveOrderRequest saveOrderRequest)
            throws AuthorizationFailedException, CouponNotFoundException,
            AddressNotFoundException, PaymentMethodNotFoundException,
            RestaurantNotFoundException, ItemNotFoundException {

        String authToken = authorization.split( " ")[1];

        final OrdersEntity savedOrderEntity = orderService.saveOrder(saveOrderRequest, authToken);

        SaveOrderResponse saveOrderResponse = new SaveOrderResponse().id(savedOrderEntity.getUuid())
                .status("ORDER SUCCESSFULLY PLACED");

        return new ResponseEntity<SaveOrderResponse>(saveOrderResponse, HttpStatus.CREATED);
    }
}
