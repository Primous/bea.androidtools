package br.com.bea.androidtools.api.model;

import java.io.Serializable;
import java.lang.reflect.Field;
import org.json.JSONObject;
import android.content.ContentValues;
import android.database.Cursor;
import br.com.bea.androidtools.api.model.converter.Converter;
import br.com.bea.androidtools.api.model.converter.ConverterFactory;

public final class FieldMapper {
    static final synchronized FieldMapper create(final Field field) {
        return new FieldMapper(field);
    }

    private final Converter converter;
    private final Field field;

    private FieldMapper(final Field field) {
        this.field = field;
        converter = ConverterFactory.get(field.getType());
    }

    public void convert(final ContentValues values, final Field field, final Object value) {
        converter.convert(values, field, value);
    }

    public void convert(final JSONObject object, final Field field, final Object value) {
        converter.convert(object, field, value);
    }

    public <S extends Serializable> void convert(final S serializable, final Field field, final Cursor cursor) {
        converter.convert(serializable, field, cursor);
    }

    public <S extends Serializable> void convert(final S serializable, final Field field, final JSONObject object) {
        converter.convert(serializable, field, object);
    }

    public Field getField() {
        field.setAccessible(true);
        return field;
    }

    public <E extends ValueObject> Object getValue(final E e) throws IllegalArgumentException, IllegalAccessException {
        field.setAccessible(true);
        return field.get(e);
    }

}
