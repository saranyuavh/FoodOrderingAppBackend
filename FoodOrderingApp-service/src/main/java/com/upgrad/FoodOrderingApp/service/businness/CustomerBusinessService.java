package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.CustomerDAO;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.exception.SignUpRestrictedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerBusinessService {
    @Autowired
    private CustomerDAO customerDAO;

    @Autowired
    private PasswordCryptographyProvider cryptographyProvider;


    public CustomerEntity signup(final CustomerEntity customerEntity) throws SignUpRestrictedException {
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


    //check if email is already registered
    public boolean emailExists(final String email) {
        return customerDAO.getUserByEmail(email) != null;
    }

    //check if email is already registered
    public boolean contactExists(final String contact) {
        return customerDAO.getUserByContact(contact) != null;
    }

}
