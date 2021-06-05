package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.*;
import com.upgrad.FoodOrderingApp.service.businness.AddressService;
import com.upgrad.FoodOrderingApp.service.businness.CustomerService;
import com.upgrad.FoodOrderingApp.service.businness.StateService;
import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
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
        customerService.validateAccessToken(authToken);
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
        addressEntity.setFlatBuilNo(saveAddressRequest.getFlatBuildingName());
        addressEntity.setLocality(saveAddressRequest.getLocality());
        addressEntity.setCity(saveAddressRequest.getCity());
        addressEntity.setPincode(saveAddressRequest.getPincode());
        StateEntity stateEntity= stateService.getStateByUuid(saveAddressRequest.getStateUuid());
        if(stateEntity == null) {
            throw new AddressNotFoundException("ANF-002","No state by this id");
        }
        addressEntity.setState(stateEntity);
        addressEntity = addressService.saveAddress(addressEntity, customerService.getCustomer(authToken));
        SaveAddressResponse saveAddressResponse = new SaveAddressResponse().id(addressEntity.getUuid()).status("ADDRESS SUCCESSFULLY REGISTERED");
        return new ResponseEntity<>(saveAddressResponse, HttpStatus.OK);
    }
    @RequestMapping(method = RequestMethod.GET, path = "/customer", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AddressListResponse> getAllSavedAddresses(@RequestHeader("authorization") final String authorization) throws AuthorizationFailedException {
        String authToken =authorization.split(" ")[1];
        customerService.validateAccessToken(authToken);
        List<AddressEntity> addressEntities = addressService.getAllAddress(customerService.getCustomer(authToken));
        List<AddressList> addressList = new ArrayList<>();
        for (AddressEntity addressEntity :addressEntities) {
            AddressList addTmp = new AddressList();
            addTmp.setId(UUID.fromString(addressEntity.getUuid()));
            addTmp.setFlatBuildingName(addressEntity.getFlatBuilNo());
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

    @RequestMapping(method = RequestMethod.DELETE, path = "/{address_id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<DeleteAddressResponse> deleteSavedAddress(@PathVariable("address_id") String addressUuid, @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, AddressNotFoundException {
        String authToken = authorization.split(" ")[1];
        customerService.validateAccessToken(authorization);
        CustomerEntity custmer = customerService.getCustomer(authToken);
        if (! custmer.hasAddress(addressUuid)) {
            throw new AuthorizationFailedException("ATHR-004","You are not authorized to view/update/delete any one else's address");
        }
        if(addressUuid.isEmpty()) {
            throw new AddressNotFoundException("ANF-005","Address id can not be empty");
        }
        if(addressService.getAddressByUUID(addressUuid,custmer) == null) {
            throw new AddressNotFoundException("ANF-003","No address by this id");
        }
        DeleteAddressResponse deleteAddressResponse = new DeleteAddressResponse().id(UUID.fromString(addressUuid)).status("ADDRESS DELETED SUCCESSFULLY");
        return new ResponseEntity<DeleteAddressResponse>(deleteAddressResponse, HttpStatus.OK);
    }

}
