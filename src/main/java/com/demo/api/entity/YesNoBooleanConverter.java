package com.demo.api.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class YesNoBooleanConverter implements AttributeConverter<Boolean, String> {
    @Override public String convertToDatabaseColumn(Boolean value) {
        return Boolean.TRUE.equals(value) ? "Y" : "N";
    }
    @Override public Boolean convertToEntityAttribute(String db) {
        return "Y".equalsIgnoreCase(db);
    }
}