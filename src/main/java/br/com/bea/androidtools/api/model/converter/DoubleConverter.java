package br.com.bea.androidtools.api.model.converter;

import java.io.Serializable;
import java.lang.reflect.Field;
import org.json.JSONObject;
import android.content.ContentValues;
import android.database.Cursor;
import br.com.bea.androidtools.api.model.annotations.Column;
import br.com.bea.androidtools.api.model.annotations.Metadata;

class DoubleConverter extends NullableConverter {

    private static final ThreadLocal<DoubleConverter> INSTANCE = new ThreadLocal<DoubleConverter>() {
        @Override
        protected DoubleConverter initialValue() {
            return new DoubleConverter();
        };
    };

    static DoubleConverter getInstance() {
        return DoubleConverter.INSTANCE.get();
    }

    private DoubleConverter() {
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
            field.set(serializable, cursor.getDouble(cursor.getColumnIndex(field.getAnnotation(Column.class).name())));
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <S extends Serializable> void convert(final S serializable, final Field field, final JSONObject object) {
        try {
            field.set(serializable, object.getDouble(field.getAnnotation(Metadata.class).value()));
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
