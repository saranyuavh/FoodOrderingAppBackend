package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.AddressDAO;
import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.StateEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AddressService {
    @Autowired
    private AddressDAO addressDAO;

    public AddressEntity saveAddress(AddressEntity addressEntity) {
        return addressDAO.saveAddress(addressEntity);
    }

    public StateEntity getStateByUUID(String uuid) {
        return addressDAO.getStateByUUID(uuid);
    }
}
