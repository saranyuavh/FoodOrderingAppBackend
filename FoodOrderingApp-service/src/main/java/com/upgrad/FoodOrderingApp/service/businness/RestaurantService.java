package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.CustomerDAO;
import com.upgrad.FoodOrderingApp.service.dao.RestaurantDAO;
import com.upgrad.FoodOrderingApp.service.entity.RestaurantCategoryEntity;
import com.upgrad.FoodOrderingApp.service.entity.RestaurantEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.InvalidRatingException;
import com.upgrad.FoodOrderingApp.service.exception.RestaurantNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

@Service
public class RestaurantService {

    @Autowired
    RestaurantDAO restaurantDAO;

    @Autowired
    private CustomerService customerService;

    public List<RestaurantEntity> getAllRestaurants() {
        return restaurantDAO.getAllRestaurants();
    }

    public List<RestaurantEntity> getRestaurantsByName(String restaurant_name) {
        return restaurantDAO.getRestaurantsByName(restaurant_name);
    }

    public List<RestaurantCategoryEntity> getRestaurantByCategoryId(final Long categoryID) {
        return restaurantDAO.getRestaurantByCategoryId(categoryID);
    }

    public RestaurantEntity restaurantByUUID(String restaurantUUID) {
        return restaurantDAO.getRestaurantByUUId(restaurantUUID);
    }

    @Transactional
    public RestaurantEntity updateCustomerRating (final Double customerRating, final String restaurant_id, final String authorizationToken)
            throws AuthorizationFailedException, RestaurantNotFoundException, InvalidRatingException {

        final ZonedDateTime now = ZonedDateTime.now();

        customerService.validateAccessToken(authorizationToken);

        if(restaurant_id == null || restaurant_id.isEmpty() || restaurant_id.equalsIgnoreCase("\"\"")){
            throw new RestaurantNotFoundException("RNF-002", "Restaurant id field should not be empty");
        }

        RestaurantEntity restaurantEntity =  restaurantDAO.getRestaurantByUUId(restaurant_id);

        if (restaurantEntity == null) {
            throw new RestaurantNotFoundException("RNF-001", "No restaurant by this id");
        }

        if(customerRating == null || customerRating.isNaN() || customerRating < 1 || customerRating > 5 ){
            throw new InvalidRatingException("IRE-001", "Restaurant should be in the range of 1 to 5");
        }

        BigDecimal oldRatingCalculation = (restaurantEntity.getCustomerRating().multiply(new BigDecimal(restaurantEntity.getNumCustomersRated())));
        BigDecimal calculatedRating = (oldRatingCalculation.add(new BigDecimal(customerRating))).divide(new BigDecimal(restaurantEntity.getNumCustomersRated() + 1));
        restaurantEntity.setCustomerRating(calculatedRating);
        restaurantEntity.setNumCustomersRated(restaurantEntity.getNumCustomersRated() + 1);

        restaurantDAO.updateRestaurant(restaurantEntity);
        return restaurantEntity;
    }

}
