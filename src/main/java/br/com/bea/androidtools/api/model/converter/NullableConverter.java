package br.com.bea.androidtools.api.model.converter;

import java.lang.reflect.Field;
import org.json.JSONObject;
import android.content.ContentValues;
import br.com.bea.androidtools.api.model.annotations.Column;
import br.com.bea.androidtools.api.model.annotations.Metadata;

public abstract class NullableConverter implements Converter {

    private boolean converting(final ContentValues values, final Field field) {
        try {
            values.putNull(field.getAnnotation(Column.class).name());
            return true;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean converting(final JSONObject object, final Field field) {
        try {
            object.put(field.getAnnotation(Metadata.class).value(), JSONObject.NULL);
            return true;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected boolean nullable(final ContentValues values, final Field field, final Object value) {
        return null == value ? converting(values, field) : false;
    }

    protected boolean nullable(final JSONObject object, final Field field, final Object value) {
        return null == value ? converting(object, field) : false;
    }
}
