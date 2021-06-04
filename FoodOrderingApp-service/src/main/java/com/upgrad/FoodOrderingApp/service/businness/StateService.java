package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.StateDAO;
import com.upgrad.FoodOrderingApp.service.entity.StateEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class StateService<StateDao> {
    @Autowired
    private StateDAO stateDAO  ;

    @Transactional
    public StateEntity getStateById(final Long stateId) {
        return stateDAO.getStateById(stateId);
    }
}
