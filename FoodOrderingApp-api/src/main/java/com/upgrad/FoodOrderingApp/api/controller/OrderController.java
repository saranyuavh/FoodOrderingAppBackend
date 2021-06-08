package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.*;
import com.upgrad.FoodOrderingApp.service.businness.*;
import com.upgrad.FoodOrderingApp.service.entity.*;
import com.upgrad.FoodOrderingApp.service.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping("/")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private AddressService addressService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ItemService itemService;

    @Autowired
    RestaurantService restaurantService;

    @Autowired
    PaymentService paymentService;

    @RequestMapping(method = RequestMethod.GET, path = "/order/coupon/{coupon_name}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CouponDetailsResponse> getCouponByName(@RequestHeader("authorization")  final String authorization, @PathVariable("coupon_name") final String couponName)
            throws AuthorizationFailedException, CouponNotFoundException {

        String authToken = authorization.split( " ")[1];

        if(couponName == null || couponName.isEmpty() || couponName.equalsIgnoreCase("\"\"")){
            throw new CouponNotFoundException("CPF-002", "Coupon name field should not be empty");
        }

        customerService.validateAccessToken(authToken);

        CouponEntity couponEntity = orderService.getCouponByCouponName(couponName);

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

        final List<OrderEntity> orderEntityList = orderService.getOrdersByCustomers(customerEntity.getUuid());

        CustomerOrderResponse customerOrderResponse = new CustomerOrderResponse();

        for (OrderEntity orderEntity : orderEntityList) {

            OrderListCustomer orderListCustomer = new OrderListCustomer();
            orderListCustomer.setId(UUID.fromString(orderEntity.getCustomer().getUuid()));
            orderListCustomer.setFirstName(orderEntity.getCustomer().getFirstName());
            orderListCustomer.setLastName(orderEntity.getCustomer().getLastName());
            orderListCustomer.setContactNumber(orderEntity.getCustomer().getContactNumber());
            orderListCustomer.setEmailAddress(orderEntity.getCustomer().getEmail());

            OrderListAddressState orderListAddressState = new OrderListAddressState();
            orderListAddressState.setId(UUID.fromString(orderEntity.getAddress().getState().getUuid()));
            orderListAddressState.setStateName(orderEntity.getAddress().getState().getStateName());

            OrderListAddress orderListAddress = new OrderListAddress();
            orderListAddress.setId(UUID.fromString(orderEntity.getAddress().getUuid()));
            orderListAddress.setFlatBuildingName(orderEntity.getAddress().getFlatBuilNo());
            orderListAddress.setLocality(orderEntity.getAddress().getLocality());
            orderListAddress.setCity(orderEntity.getAddress().getCity());
            orderListAddress.setPincode(orderEntity.getAddress().getPincode());
            orderListAddress.setState(orderListAddressState);

            OrderListCoupon orderListCoupon = new OrderListCoupon();
            orderListCoupon.setId(UUID.fromString(orderEntity.getCoupon().getUuid()));
            orderListCoupon.setCouponName(orderEntity.getCoupon().getCouponName());
            orderListCoupon.setPercent(orderEntity.getCoupon().getPercent());

            OrderListPayment orderListPayment = new OrderListPayment();
            orderListPayment.setId(UUID.fromString(orderEntity.getUuid()));
            orderListPayment.setPaymentName(orderEntity.getPayment().getPaymentName());

            OrderList orderList = new OrderList();
            orderList.setId(UUID.fromString(orderEntity.getUuid()));
            orderList.setDate(orderEntity.getDate().toString());
            orderList.setAddress(orderListAddress);
            orderList.setCustomer(orderListCustomer);
            orderList.setPayment(orderListPayment);
            orderList.setCoupon(orderListCoupon);
            orderList.setBill(orderEntity.getBill());
            orderList.setDiscount(orderEntity.getDiscount());

            for (OrderItemEntity orderItemEntity : itemService.getItemsByOrder(orderEntity)) {

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

        customerService.validateAccessToken(authToken);

        CustomerEntity customerEntity = customerService.getCustomer(authToken);

        AddressEntity addressEntity = addressService.getAddressByUUID(saveOrderRequest.getAddressId(), customerEntity);

        CouponEntity couponEntity = orderService.getCouponByCouponId(saveOrderRequest.getCouponId().toString());

        RestaurantEntity restaurantEntity = restaurantService.restaurantByUUID(saveOrderRequest.getRestaurantId().toString());

        PaymentEntity paymentEntity = paymentService.getPaymentByUUID(saveOrderRequest.getPaymentId().toString());

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

        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setUuid(UUID.randomUUID().toString());
        orderEntity.setCoupon(couponEntity);
        orderEntity.setRestaurant(restaurantEntity);
        orderEntity.setCustomer(customerEntity);
        orderEntity.setAddress(addressEntity);
        orderEntity.setBill(saveOrderRequest.getBill());
        orderEntity.setDiscount(saveOrderRequest.getDiscount());
        orderEntity.setDate(now);


        for (ItemQuantity itemQuantity : saveOrderRequest.getItemQuantities()) {
            OrderItemEntity orderItemEntity = new OrderItemEntity();
            orderItemEntity.setItem(itemService.getItemEntityByUuid(itemQuantity.getItemId().toString()));
            orderItemEntity.setQuantity(itemQuantity.getQuantity());
            orderItemEntity.setPrice(itemQuantity.getPrice());
            orderItemEntity.setOrders(orderEntity);
            orderService.createOrderItemEntity(orderItemEntity);
        }

        final OrderEntity savedOrderEntity = orderService.saveOrderItem(orderEntity);

        SaveOrderResponse saveOrderResponse = new SaveOrderResponse().id(savedOrderEntity.getUuid())
                .status("ORDER SUCCESSFULLY PLACED");

        return new ResponseEntity<SaveOrderResponse>(saveOrderResponse, HttpStatus.CREATED);
    }
}
