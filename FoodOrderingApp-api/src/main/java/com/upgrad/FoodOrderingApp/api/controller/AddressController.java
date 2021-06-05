package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.*;
import com.upgrad.FoodOrderingApp.service.businness.AddressService;
import com.upgrad.FoodOrderingApp.service.businness.CustomerService;
import com.upgrad.FoodOrderingApp.service.businness.StateService;
import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.StateEntity;
import com.upgrad.FoodOrderingApp.service.exception.AddressNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SaveAddressException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping("/address")
public class AddressController {
    @Autowired
    private AddressService addressService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private StateService stateService;

    @RequestMapping(method = RequestMethod.POST, path = "/", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SaveAddressResponse> saveAddress(@RequestHeader("authorization") final String authorization, final SaveAddressRequest saveAddressRequest) throws AuthorizationFailedException, SaveAddressException, AddressNotFoundException {
        String authToken = authorization.split(" ")[1];

        if (!customerService.isAuthorized(authToken)){
            throw new AuthorizationFailedException("ATHR-001","Customer is not Logged in.");
        }

        if(customerService.isLoggedOut(authToken)) {
            throw new AuthorizationFailedException("ATHR-002","Customer is logged out. Log in again to access this endpoint");
        }

        if(customerService.isSessionExpired(authToken)) {
            throw new AuthorizationFailedException("ATHR-003","Your session is expired. Log in again to access this endpoint.");
        }
        //Lets do some validations
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<SaveAddressRequest>> violations = validator.validate(saveAddressRequest);
        if (violations.size() > 0) {
            throw new SaveAddressException("SAR-001", "No field can be empty");
        }
        String regex = "^\\d{1,6}$";
        if(!saveAddressRequest.getPincode().matches(regex)) {
            throw new SaveAddressException("SAR-002", "Invalid pincode");
        }
        AddressEntity addressEntity = new AddressEntity();
        addressEntity.setFlatBuildingNumber(saveAddressRequest.getFlatBuildingName());
        addressEntity.setLocality(saveAddressRequest.getLocality());
        addressEntity.setCity(saveAddressRequest.getCity());
        addressEntity.setPincode(saveAddressRequest.getPincode());
        StateEntity stateEntity= stateService.getStateByUuid(saveAddressRequest.getStateUuid());
        if(stateEntity == null) {
            throw new AddressNotFoundException("ANF-002","No state by this id");
        }

        addressEntity = addressService.saveAddress(addressEntity);
        SaveAddressResponse saveAddressResponse = new SaveAddressResponse().id(addressEntity.getUuid()).status("ADDRESS SUCCESSFULLY REGISTERED");
        return new ResponseEntity<>(saveAddressResponse, HttpStatus.OK);
    }
    @RequestMapping(method = RequestMethod.GET, path = "/customer", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AddressListResponse> getAllSavedAddresses(@RequestHeader("authorization") final String authorization) throws AuthorizationFailedException {
        String authToken =authorization.split(" ")[1];
        customerService.validateAccessToken(authToken);
        List<AddressEntity> addressEntities = customerService.getCustomerAddress(authToken);
        List<AddressList> addressList = new ArrayList<>();
        for (AddressEntity addressEntity :addressEntities) {
            AddressList addTmp = new AddressList();
            addTmp.setId(UUID.fromString(addressEntity.getUuid()));
            addTmp.setFlatBuildingName(addressEntity.getFlatBuildingNumber());
            addTmp.locality(addressEntity.getLocality());
            addTmp.city(addressEntity.getCity());
            addTmp.pincode(addressEntity.getPincode());
            AddressListState state = new AddressListState();
            state.id(UUID.fromString(addressEntity.getState().getUuid())).stateName(addressEntity.getState().getStateName());
            addTmp.setState(state);
            addressList.add(addTmp);
        }
        AddressListResponse response = new AddressListResponse();
        response.setAddresses(addressList);
        return new ResponseEntity<AddressListResponse>(response, HttpStatus.OK);
    }

}