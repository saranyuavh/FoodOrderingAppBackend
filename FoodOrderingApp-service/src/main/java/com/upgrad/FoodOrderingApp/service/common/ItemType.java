package com.upgrad.FoodOrderingApp.service.common;

public enum ItemType {
    NON_VEG("NON VEG","Basically Non Veg");

    private final String code;

    private final String desc;

    private ItemType(final String code, final String desc) {
        this.code = code;
        this.desc = desc;
    }
}
