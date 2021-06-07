package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.CustomerDAO;
import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthenticationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SignUpRestrictedException;
import com.upgrad.FoodOrderingApp.service.exception.UpdateCustomerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CustomerService {
    @Autowired
    private CustomerDAO customerDAO;

    @Autowired
    private PasswordCryptographyProvider cryptographyProvider;

    @Transactional
    public CustomerEntity saveCustomer(final CustomerEntity customerEntity) throws SignUpRestrictedException {

        String regex = "^[a-zA-Z0-9]+@([a-zA-Z0-9]+\\.)+[a-zA-Z0-9]+$";
        if (!customerEntity.getEmail().matches(regex)) {
            throw new SignUpRestrictedException("SGR-002", "Invalid email-id format!");
        }

        if (this.contactExists(customerEntity.getContactNumber())) {
            throw new SignUpRestrictedException("SGR-001","This contact number is already registered! Try other contact number.");
        }

        if (this.emailExists(customerEntity.getEmail())) {
            throw new SignUpRestrictedException("SGR-002","This user has already been registered, try with any other emailId");
        }
        return this.createUser(customerEntity);
    }

    //@Transactional(propagation = Propagation.REQUIRED)
    public CustomerEntity createUser(final CustomerEntity customerEntity) {

        String[] encryptedText = cryptographyProvider.encrypt(customerEntity.getPassword());
        customerEntity.setSalt(encryptedText[0]);
        customerEntity.setPassword(encryptedText[1]);
        return customerDAO.createCustomer(customerEntity);

    }

    @Transactional
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

    //@javax.transaction.Transactional
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
    public boolean checkPassword(CustomerEntity customerEntity, String password){
        final String encryptedPassword = cryptographyProvider.encrypt(password, customerEntity.getSalt());
        if (encryptedPassword.equals(customerEntity.getPassword()))
        {
            return true;
        }
        return false;
    }

    public CustomerEntity updateCustomerPassword(String oldPwd, String newPwd, CustomerEntity customerEntity) throws UpdateCustomerException {

        if( oldPwd.isEmpty() || newPwd.isEmpty()){
            throw new UpdateCustomerException("UCR-003","No field should be empty");
        }
        String regex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[\\\\[#@$%&*!^\\\\] –[{}]:;',?/*~$^+=<>]).{8,20}$";
        if (!newPwd.matches(regex)) {
            throw new UpdateCustomerException("UCR-001", "Weak password!");
        }
        if(!this.checkPassword(customerEntity,oldPwd)){
            throw new UpdateCustomerException("UCR-004", "Incorrect old password!");
        }

        final String encryptedPassword = cryptographyProvider.encrypt(newPwd, customerEntity.getSalt());
        customerEntity.setPassword(encryptedPassword);
        return customerDAO.updateUser(customerEntity);
    }

    public CustomerEntity getCustomerByAuthToken(String accessToken) {
        return this.getCustomerAccessToken(accessToken).getCustomer();
    }

    public CustomerEntity getCustomer(String Uuid) {
        return customerDAO.getUser(Uuid);
    }

    public CustomerAuthEntity logout(String accessToken) {
        CustomerAuthEntity customerAuthEntity=this.getCustomerAccessToken(accessToken);
        customerAuthEntity.setLogoutAt(ZonedDateTime.now());
        customerDAO.updateAuthToken(customerAuthEntity);
        return customerAuthEntity;
    }
}
