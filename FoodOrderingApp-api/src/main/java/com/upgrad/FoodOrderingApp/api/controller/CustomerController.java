package com.upgrad.FoodOrderingApp.api.controller;


import com.upgrad.FoodOrderingApp.api.model.LoginResponse;
import com.upgrad.FoodOrderingApp.api.model.SignupCustomerRequest;
import com.upgrad.FoodOrderingApp.api.model.SignupCustomerResponse;
import com.upgrad.FoodOrderingApp.service.businness.CustomerService;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthenticationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SignUpRestrictedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Base64;
import java.util.Set;
import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping("/customer")
public class CustomerController {
    @Autowired
    private CustomerService customerService;

    @RequestMapping(method = RequestMethod.POST, path = "/signup", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SignupCustomerResponse> signup(final SignupCustomerRequest signupUserRequest) throws SignUpRestrictedException {

        //Lets do some validations
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<SignupCustomerRequest>> violations = validator.validate(signupUserRequest);
        if (violations.size() > 0) {
            throw new SignUpRestrictedException("SGR-005", "Except last name all fields should be filled");
        }
        String regex = "^[a-zA-Z0-9]+@([a-zA-Z0-9]+\\.)+[a-zA-Z0-9]+$";
        if (!signupUserRequest.getEmailAddress().matches(regex)) {
            throw new SignUpRestrictedException("SGR-002", "Invalid email-id format!");
        }
        regex = "^\\d{10}$";
        if (!signupUserRequest.getContactNumber().matches(regex)) {
            throw new SignUpRestrictedException("SGR-003", "Invalid contact number!");
        }
        regex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[\\\\[#@$%&*!^\\\\] –[{}]:;',?/*~$^+=<>]).{8,20}$";
        if (!signupUserRequest.getPassword().matches(regex)) {
            throw new SignUpRestrictedException("SGR-003", "Invalid contact number!");
        }

        final CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setUuid(UUID.randomUUID().toString());
        customerEntity.setFirstName(signupUserRequest.getFirstName());
        customerEntity.setLastName(signupUserRequest.getLastName());
        customerEntity.setEmail(signupUserRequest.getEmailAddress());
        customerEntity.setPassword(signupUserRequest.getPassword());
        customerEntity.setSalt("1234abc");
        customerEntity.setContactNumber(signupUserRequest.getContactNumber());
        final CustomerEntity createdUserEntity = customerService.saveCustomer(customerEntity);
        SignupCustomerResponse userResponse = new SignupCustomerResponse().id(createdUserEntity.getUuid()).status("CUSTOMER SUCCESSFULLY REGISTERED");
        return new ResponseEntity<>(userResponse, HttpStatus.CREATED);
    }


    @RequestMapping(method = RequestMethod.POST, path = "/login", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<LoginResponse> login(@RequestHeader("authorization") final String authorization) throws AuthenticationFailedException {

        String [] authEncoded = authorization.split("Basic ");
        String encoded = "";
        if (authEncoded.length > 1) {
            encoded = authEncoded[1];
        } else {
            throw new AuthenticationFailedException("ATH-003","Incorrect format of decoded customer name and password");
        }
        byte[] decode;
        try {
            decode = Base64.getDecoder().decode(encoded);
        } catch (IllegalArgumentException ex) {
            throw new AuthenticationFailedException("ATH-003","Incorrect format of decoded customer name and password");
        }
        String decodedText = new String(decode);
        String[] decodedArray = decodedText.split(":");

        //who knows may be it might not work when splitting invalid format token
        String contact ="rather peculiar username";
        String passowrd = "and a strange password";

        if(decodedArray.length> 1) {
            contact = decodedArray[0];
            passowrd = decodedArray[1];
        } else {
            throw new AuthenticationFailedException("ATH-003","Incorrect format of decoded customer name and password");
        }
        if (!customerService.contactExists(contact)) {
            throw new AuthenticationFailedException("ATH-001","This contact number has not been registered!");
        }
        CustomerAuthEntity customerAuthEntity = customerService.authenticate(contact,passowrd);
        CustomerEntity user = customerAuthEntity.getCustomer();

        LoginResponse authorizedUserResponse = new LoginResponse().id(user.getUuid()).message("LOGGED IN SUCCESSFULLY").firstName(user.getFirstName()).lastName(user.getLastName()).contactNumber(user.getContactNumber()).emailAddress(user.getEmail());

        HttpHeaders headers = new HttpHeaders();
        headers.add("access-token", customerAuthEntity.getAccessToken());
        return new ResponseEntity<LoginResponse>(authorizedUserResponse, headers, HttpStatus.OK);
    }
}
