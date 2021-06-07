package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.PaymentDAO;
import com.upgrad.FoodOrderingApp.service.entity.PaymentEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class PaymentService {

    @Autowired
    private PaymentDAO paymentDao;

    @Transactional
    public List<PaymentEntity> getPaymentMethods() {
        return paymentDao.getPaymentMethods();
    }

    @Transactional
    public PaymentEntity getPaymentByUUID(final String paymentUuid) {
        return paymentDao.getPaymentByUuid(paymentUuid);
    }

}
