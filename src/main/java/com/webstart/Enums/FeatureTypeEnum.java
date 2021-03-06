package com.webstart.Enums;

public enum FeatureTypeEnum {
    CROP(1),
    STATION(2),
    END_DEVICE(3);

    private final int value;

    FeatureTypeEnum(final int newValue) {
        value = newValue;
    }

    public int getValue() {
        return value;
    }
}

