package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.CategoryDAO;
import com.upgrad.FoodOrderingApp.service.dao.ItemDAO;
import com.upgrad.FoodOrderingApp.service.entity.CategoryEntity;
import com.upgrad.FoodOrderingApp.service.entity.CategoryItemEntity;
import com.upgrad.FoodOrderingApp.service.entity.ItemEntity;
import com.upgrad.FoodOrderingApp.service.exception.CategoryNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryService {

    @Autowired
    CategoryDAO categoryDAO;

    @Autowired
    ItemDAO itemDAO;

    public CategoryEntity getCategoryEntityByUuid(final String categoryUUId) throws CategoryNotFoundException{
        if(categoryUUId == null || categoryUUId.isEmpty() || categoryUUId.equalsIgnoreCase("\"\"")){
            throw new CategoryNotFoundException("CNF-001", "Category id field should not be empty");
        }
        CategoryEntity categoryEntity =  categoryDAO.getCategoryByUUId(categoryUUId);
        if(categoryEntity == null){
            throw new CategoryNotFoundException("CNF-002", "No category by this id");
        }
        return categoryEntity;
    }

    public List<CategoryEntity> getAllCategories() {
        return categoryDAO.getAllCategories();
    }

    public List<ItemEntity> getItemsById(CategoryEntity categoryEntity) {
        ItemEntity itemEntity = new ItemEntity();
        List<ItemEntity> itemEntities = new ArrayList<>();
        List<CategoryItemEntity>  categoryItemEntity = new ArrayList<>();
        categoryItemEntity = categoryDAO.getItemByCategoryId(categoryEntity);
        for (CategoryItemEntity ce: categoryItemEntity)
        {
            itemEntity = itemDAO.getItemById(ce.getItem().getUuid());
            itemEntities.add(itemEntity);
        }
        return  itemEntities;
    }
}
