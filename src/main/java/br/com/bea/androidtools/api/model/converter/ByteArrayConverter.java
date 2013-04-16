package br.com.bea.androidtools.api.model.converter;

import java.io.Serializable;
import java.lang.reflect.Field;
import org.json.JSONObject;
import android.content.ContentValues;
import android.database.Cursor;
import br.com.bea.androidtools.api.model.annotations.Column;
import br.com.bea.androidtools.api.model.annotations.Metadata;

class ByteArrayConverter extends NullableConverter {

    private static final ThreadLocal<ByteArrayConverter> INSTANCE = new ThreadLocal<ByteArrayConverter>() {
        @Override
        protected ByteArrayConverter initialValue() {
            return new ByteArrayConverter();
        };
    };

    static ByteArrayConverter getInstance() {
        return ByteArrayConverter.INSTANCE.get();
    }

    private ByteArrayConverter() {
    }

    @Override
    public void convert(final ContentValues values, final Field field, final Object value) {
        if (nullable(values, field, value)) return;
        try {
            values.put(field.getAnnotation(Column.class).name(), (byte[]) value);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void convert(final JSONObject object, final Field field, final Object value) {
        if (nullable(object, field, value)) return;
        try {
            object.put(field.getAnnotation(Metadata.class).value(), new String((byte[]) value));
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <S extends Serializable> void convert(final S serializable, final Field field, final Cursor cursor) {
        try {
            field.set(serializable, cursor.getBlob(cursor.getColumnIndex(field.getAnnotation(Column.class).name())));
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <S extends Serializable> void convert(final S serializable, final Field field, final JSONObject object) {
        try {
            field.set(serializable, object.getString(field.getAnnotation(Metadata.class).value()).getBytes());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
