package br.com.bea.androidtools.api.model.converter;

import java.math.BigDecimal;
import java.util.Date;

public final class ConverterFactory {

    public static final synchronized Converter get(final Class<?> target) {
        if (target.equals(String.class)) return StringConverter.getInstance();
        if (target.equals(BigDecimal.class)) return BigDecimalConverter.getInstance();
        if (target.equals(Date.class)) return DateConverter.getInstance();
        if (target.equals(Long.class)) return LongConverter.getInstance();
        if (target.equals(byte[].class)) return ByteArrayConverter.getInstance();
        if (target.equals(boolean.class)) return BooleanConverter.getInstance();
        if (target.equals(char.class)) return CharConverter.getInstance();
        return null;
    }
}
