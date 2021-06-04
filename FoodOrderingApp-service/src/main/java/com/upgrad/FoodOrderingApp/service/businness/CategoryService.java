package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.CategoryDAO;
import com.upgrad.FoodOrderingApp.service.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class CategoryService {

    @Autowired
    CategoryDAO categoryDAO;

    public CategoryEntity getCategoryEntityByUuid(final String categoryUUId) {
        return categoryDAO.getCategoryByUUId(categoryUUId);
    }

    public List<CategoryEntity> getAllCategories() {
        return categoryDAO.getAllCategories();
    }
}
