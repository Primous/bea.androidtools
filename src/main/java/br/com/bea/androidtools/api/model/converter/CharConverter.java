package br.com.bea.androidtools.api.model.converter;

import java.io.Serializable;
import java.lang.reflect.Field;
import org.json.JSONObject;
import android.content.ContentValues;
import android.database.Cursor;
import br.com.bea.androidtools.api.model.annotations.Column;
import br.com.bea.androidtools.api.model.annotations.Metadata;

class CharConverter extends NullableConverter {

    private static final ThreadLocal<CharConverter> INSTANCE = new ThreadLocal<CharConverter>() {
        @Override
        protected CharConverter initialValue() {
            return new CharConverter();
        };
    };

    static CharConverter getInstance() {
        return CharConverter.INSTANCE.get();
    }

    private CharConverter() {
    }

    @Override
    public void convert(final ContentValues values, final Field field, final Object value) {
        if (nullable(values, field, value)) return;
        try {
            values.put(field.getAnnotation(Column.class).name(), String.valueOf(value));
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void convert(final JSONObject object, final Field field, final Object value) {
        if (nullable(object, field, value)) return;
        try {
            object.put(field.getAnnotation(Metadata.class).value(), String.valueOf(value));
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <S extends Serializable> void convert(final S serializable, final Field field, final Cursor cursor) {
        try {
            field.set(serializable, cursor.getString(cursor.getColumnIndex(field.getAnnotation(Column.class).name())));
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <S extends Serializable> void convert(final S serializable, final Field field, final JSONObject object) {
        try {
            field.set(serializable, object.getString(field.getAnnotation(Metadata.class).value()));
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

}
