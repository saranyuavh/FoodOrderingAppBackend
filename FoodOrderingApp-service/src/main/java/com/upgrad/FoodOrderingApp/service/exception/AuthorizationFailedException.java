package com.upgrad.FoodOrderingApp.service.exception;

import org.springframework.http.HttpStatus;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * AuthorizationFailedException is thrown when customer is not authorized to access that endpoint.
 */
public class AuthorizationFailedException extends Exception {
    private final String code;
    private final String errorMessage;

    public AuthorizationFailedException(final String code, final String errorMessage) {
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
            case "ATHR-001" : return HttpStatus.FORBIDDEN;
            case "ATHR-002" : return HttpStatus.FORBIDDEN;
            case "ATHR-003" : return HttpStatus.FORBIDDEN;
            case "ATHR-004" : return HttpStatus.FORBIDDEN;
        }
        return HttpStatus.FORBIDDEN;
    }
}

