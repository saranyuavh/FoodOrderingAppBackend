package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.utils.ItemUtils;
import com.upgrad.FoodOrderingApp.api.model.CategoryDetailsResponse;
import com.upgrad.FoodOrderingApp.api.model.CategoryListResponse;
import com.upgrad.FoodOrderingApp.api.model.ItemList;
import com.upgrad.FoodOrderingApp.service.businness.CategoryService;
import com.upgrad.FoodOrderingApp.service.entity.CategoryEntity;
import com.upgrad.FoodOrderingApp.service.exception.CategoryNotFoundException;
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
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @RequestMapping(method = RequestMethod.GET, path = "/category", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity getAllCategories() {
        final List<CategoryEntity> allCategories = categoryService.getAllCategories();
        List<CategoryListResponse> categoriesList = new ArrayList<CategoryListResponse>();
        for (CategoryEntity categoryEntity : allCategories) {
            CategoryListResponse categoryDetail = new CategoryListResponse();
            categoryDetail.setCategoryName(categoryEntity.getCategoryName());
            categoryDetail.setId(UUID.fromString(categoryEntity.getUuid()));
            categoriesList.add(categoryDetail);
        }
        return new ResponseEntity<>(categoriesList, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/category/{category_id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity getCategoryById(@PathVariable String category_id) throws CategoryNotFoundException {

        if(category_id == null || category_id.isEmpty() || category_id.equalsIgnoreCase("\"\"")){
            throw new CategoryNotFoundException("CNF-001", "Category id field should not be empty");
        }

        CategoryEntity categoryEntity = categoryService.getCategoryEntityByUuid(category_id);

        if(categoryEntity == null){
            throw new CategoryNotFoundException("CNF-002", "No category by this id");
        }

        CategoryDetailsResponse categoryDetailsResponse = new CategoryDetailsResponse();
        categoryDetailsResponse.setCategoryName(categoryEntity.getCategoryName());
        categoryDetailsResponse.setId(UUID.fromString(categoryEntity.getUuid()));
        List<ItemList> itemLists = ItemUtils.serialiseItemList(categoryEntity.getItemEntities());
        categoryDetailsResponse.setItemList(itemLists);

        return new ResponseEntity<>(categoryDetailsResponse, HttpStatus.OK);
    }
}
