package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.CustomerDAO;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthenticationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SignUpRestrictedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.UUID;

@Service
public class CustomerService {
    @Autowired
    private CustomerDAO customerDAO;

    @Autowired
    private PasswordCryptographyProvider cryptographyProvider;


    public CustomerEntity saveCustomer(final CustomerEntity customerEntity) throws SignUpRestrictedException {
        if (this.contactExists(customerEntity.getContactNumber())) {
            throw new SignUpRestrictedException("SGR-001","This contact number is already registered! Try other contact number.");
        }

        if (this.emailExists(customerEntity.getEmail())) {
            throw new SignUpRestrictedException("SGR-002","This user has already been registered, try with any other emailId");
        }
        return this.createUser(customerEntity);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerEntity createUser(final CustomerEntity customerEntity) {

        String[] encryptedText = cryptographyProvider.encrypt(customerEntity.getPassword());
        customerEntity.setSalt(encryptedText[0]);
        customerEntity.setPassword(encryptedText[1]);
        return customerDAO.createCustomer(customerEntity);

    }

    public CustomerAuthEntity authenticate(String contact, String password) throws AuthenticationFailedException {
        CustomerEntity customerEntity = customerDAO.getUserByContact(contact);
        final String encryptedPassword = cryptographyProvider.encrypt(password, customerEntity.getSalt());
        if (encryptedPassword.equals(customerEntity.getPassword())) {
            JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(encryptedPassword);
            CustomerAuthEntity customerAuthEntity = new CustomerAuthEntity();
            customerAuthEntity.setCustomer(customerEntity);
            final ZonedDateTime now = ZonedDateTime.now();
            final ZonedDateTime expiresAt = now.plusHours(8);
            customerAuthEntity.setAccessToken(jwtTokenProvider.generateToken(customerEntity.getUuid(), now, expiresAt));
            customerAuthEntity.setLoginAt(now);
            customerAuthEntity.setExpiresAt(expiresAt);
            customerAuthEntity.setUuid(UUID.randomUUID().toString());
            customerDAO.createAuthToken(customerAuthEntity);
            return customerAuthEntity;
        } else {
            throw new AuthenticationFailedException("ATH-002", "Invalid Credentials");
        }

    }


    //check if email is already registered
    public boolean emailExists(final String email) {
        return customerDAO.getUserByEmail(email) != null;
    }

    //check if email is already registered
    public boolean contactExists(final String contact) {
        return customerDAO.getUserByContact(contact) != null;
    }

    public CustomerAuthEntity getCustomerAccessToken(String authorization) {
        CustomerAuthEntity authEntity =  customerDAO.getCustomerAuthToken(authorization);
        return authEntity;
    }

    public CustomerEntity updateCustomer(CustomerEntity customerEntity) {
        customerDAO.updateUser(customerEntity);
        return customerEntity;
    }

    @javax.transaction.Transactional
    public void validateAccessToken(final String authorizationToken) throws AuthorizationFailedException {

        CustomerAuthEntity customerAuthTokenEntity = customerDAO.getCustomerAuthToken(authorizationToken);

        final ZonedDateTime now = ZonedDateTime.now();

        if (customerAuthTokenEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "Customer is not Logged in.");
        } else if (customerAuthTokenEntity.getLogoutAt() != null) {
            throw new AuthorizationFailedException("ATHR-002", "Customer is logged out. Log in again to access this endpoint.");
        } else if (now.isAfter(customerAuthTokenEntity.getExpiresAt()) ) {
            throw new AuthorizationFailedException("ATHR-003", "Your session is expired. Log in again to access this endpoint.");
        }
    }

}
