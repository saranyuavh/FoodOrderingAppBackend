package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.AddressDAO;
import com.upgrad.FoodOrderingApp.service.dao.StateDAO;
import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.entity.StateEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class AddressService {
    @Autowired
    private AddressDAO addressDAO;

    @Autowired
    private StateDAO stateDAO;


    public AddressEntity saveAddress(AddressEntity addressEntity, CustomerEntity customerEntity) {
        return addressDAO.saveAddress(addressEntity);
    }

  
    @Transactional
    public AddressEntity getAddressById(final Long addressId) {
        return addressDAO.getAddressById(addressId);
    }

    public StateEntity getStateByUUID(String stateUuid) {
        return stateDAO.getStateByUuid(stateUuid);
    }

    public AddressEntity getAddressByUUID(String uuid, CustomerEntity customerEntity) {
        return addressDAO.getAddressByUuid(uuid);
    }

    public AddressEntity deleteAddress(AddressEntity addressEntity) {
        addressDAO.deleteAddress(addressEntity.getId());
        return addressEntity;
    }

    public List<AddressEntity> getAllAddress(CustomerEntity customerEntity) {
        return customerEntity.getSortedAddresses();
    }

    public List<StateEntity> getAllStates() {
        return null;
    }
}
