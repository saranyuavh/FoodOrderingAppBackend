package com.upgrad.FoodOrderingApp.service.common;

public class AppConstants {

    public static final String REG_EXP_PASSWD_UPPER_CASE_CHAR = "^.*[A-Z].*$";
    public static final String REG_EXP_PASSWD_DIGIT = "^.*[0-9].*$";
    public static final String REG_EXP_PASSWD_SPECIAL_CHAR = "^.*[\\#\\@\\$\\%\\&\\*\\!\\^].*$";
    public static final String REG_EXP_VALID_EMAIL = "^[a-zA-Z0-9]*[\\@]{1,1}[a-zA-Z0-9]*[\\.]{1,1}[a-zA-Z0-9]*$";
    public static final String REG_EXP_BASIC_AUTH = "^.+[\\:]{1,1}.+$";

    public static final Integer ONE_1 = 1;

    public static final Integer SEVEN_7 = 7;
    public static final Integer EIGHT_8 = 8;
    public static final Integer NUMBER_10 = 10;
    public static final String COLON = ":";
    public static final String HTTP_ACCESS_TOKEN_HEADER = "access-token";
    public static final String PREFIX_BASIC = "Basic ";
    public static final String PREFIX_BEARER = "Bearer ";

}
