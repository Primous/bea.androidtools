package br.com.bea.androidtools.api.model.converter;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import org.json.JSONObject;
import android.content.ContentValues;
import android.database.Cursor;
import br.com.bea.androidtools.api.model.annotations.Column;
import br.com.bea.androidtools.api.model.annotations.Metadata;

class BigDecimalConverter extends NullableConverter {

    private static final ThreadLocal<BigDecimalConverter> INSTANCE = new ThreadLocal<BigDecimalConverter>() {
        @Override
        protected BigDecimalConverter initialValue() {
            return new BigDecimalConverter();
        };
    };

    static BigDecimalConverter getInstance() {
        return BigDecimalConverter.INSTANCE.get();
    }

    private BigDecimalConverter() {
    }

    @Override
    public void convert(final ContentValues values, final Field field, final Object value) {
        if (nullable(values, field, value)) return;
        try {
            values.put(field.getAnnotation(Column.class).name(), ((Number) value).doubleValue());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void convert(final JSONObject object, final Field field, final Object value) {
        if (nullable(object, field, value)) return;
        try {
            object.put(field.getAnnotation(Metadata.class).value(), ((Number) value).doubleValue());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <S extends Serializable> void convert(final S serializable, final Field field, final Cursor cursor) {
        try {
            field.set(serializable, BigDecimal.valueOf(cursor.getDouble(cursor.getColumnIndex(field
                .getAnnotation(Column.class).name()))));
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <S extends Serializable> void convert(final S serializable, final Field field, final JSONObject object) {
        try {
            field.set(serializable, BigDecimal.valueOf(object.getDouble(field.getAnnotation(Metadata.class).value())));
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
