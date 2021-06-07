package com.upgrad.FoodOrderingApp.service.exception;

import org.springframework.http.HttpStatus;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * SignUpRestrictedException is thrown when a customer is restricted to register in the application due to repeated customername or email.
 */
public class SignUpRestrictedException extends Exception {
    private final String code;
    private final String errorMessage;

    public SignUpRestrictedException(final String code, final String errorMessage) {
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
            case "ATH-001" : return HttpStatus.UNAUTHORIZED;
            case "ATH-002" : return HttpStatus.UNAUTHORIZED;
            case "SGR-001" : return HttpStatus.BAD_REQUEST;
            case "SGR-002" : return HttpStatus.BAD_REQUEST;
            case "SGR-003" : return HttpStatus.BAD_REQUEST;
            case "SGR-004" : return HttpStatus.BAD_REQUEST;
            case "SGR-005" : return HttpStatus.BAD_REQUEST;
        }
        return HttpStatus.NOT_FOUND;
    }
}

