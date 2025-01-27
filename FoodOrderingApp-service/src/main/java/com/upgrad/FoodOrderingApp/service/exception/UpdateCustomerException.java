package com.upgrad.FoodOrderingApp.service.exception;

import org.springframework.http.HttpStatus;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * UpdateCustomerException is thrown when the customer details can't be updated found in the database.
 */
public class UpdateCustomerException extends Exception {
    private final String code;
    private final String errorMessage;

    public UpdateCustomerException(final String code, final String errorMessage) {
        this.code = code;
        this.errorMessage = errorMessage;
    }

    @Override
    public void printStackTrace() {
        super.printStackTrace();
    }

    @Override
    public void printStackTrace(PrintStream s) {
        super.printStackTrace(s);
    }

    @Override
    public void printStackTrace(PrintWriter s) {
        super.printStackTrace(s);
    }

    public String getCode() {
        return code;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public HttpStatus getHttpCode() {
        switch(code) {
            case "UCR-001" : return HttpStatus.BAD_REQUEST;
            case "UCR-002" : return HttpStatus.BAD_REQUEST;
            case "UCR-003" : return HttpStatus.BAD_REQUEST;

        }
        return HttpStatus.BAD_REQUEST;
    }

}
