package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.CategoryDAO;
import com.upgrad.FoodOrderingApp.service.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;

public class CategoryService {

    @Autowired
    CategoryDAO categoryDAO;
    public CategoryEntity getCategoryEntityByUuid(final String categoryUUId){
        return  categoryDAO.getCategoryByUUId(categoryUUId);
    }
}
