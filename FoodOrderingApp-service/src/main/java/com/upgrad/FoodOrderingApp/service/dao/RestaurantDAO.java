package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.RestaurantEntity;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

public class RestaurantDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public List<RestaurantEntity> getAllRestaurants() {
        try {
            return entityManager.createNamedQuery("allRestaurants", RestaurantEntity.class).getResultList();
        } catch(NoResultException nre) {
            return null;
        }
    }

    public List<RestaurantEntity> getRestaurantsByName(String restaurantName) {
        try {
            return entityManager.createNamedQuery("findByName", RestaurantEntity.class).setParameter("restaurantName","%" + restaurantName.toLowerCase() + "%" ).getResultList();
        } catch(NoResultException nre) {
            return null;
        }
    }
}