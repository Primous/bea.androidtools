package br.com.bea.androidtools.api.model.converter;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.json.JSONObject;
import android.content.ContentValues;
import android.database.Cursor;
import br.com.bea.androidtools.api.model.annotations.Column;
import br.com.bea.androidtools.api.model.annotations.DateFormat;
import br.com.bea.androidtools.api.model.annotations.Metadata;

class DateConverter extends NullableConverter {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final ThreadLocal<DateConverter> INSTANCE = new ThreadLocal<DateConverter>() {
        @Override
        protected DateConverter initialValue() {
            return new DateConverter();
        };
    };

    static DateConverter getInstance() {
        return DateConverter.INSTANCE.get();
    }

    private DateConverter() {
    }

    @Override
    public void convert(final ContentValues values, final Field field, final Object value) {
        if (nullable(values, field, value)) return;
        try {
            values.put(field.getAnnotation(Column.class).name(), ((Date) value).getTime());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void convert(final JSONObject object, final Field field, final Object value) {
        if (nullable(object, field, value)) return;
        try {
            object.put(field.getAnnotation(Metadata.class).value(),
                       field.isAnnotationPresent(DateFormat.class) ? new SimpleDateFormat(field
                           .getAnnotation(DateFormat.class).pattern()).format((Date) value) : DATE_FORMAT
                           .format((Date) value));
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <S extends Serializable> void convert(final S serializable, final Field field, final Cursor cursor) {
        try {
            field.set(serializable, new Date(cursor.getLong(cursor.getColumnIndex(field.getAnnotation(Column.class)
                .name()))));
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <S extends Serializable> void convert(final S serializable, final Field field, final JSONObject object) {
        try {
            field.set(serializable, field.isAnnotationPresent(DateFormat.class) ? new SimpleDateFormat(field
                .getAnnotation(DateFormat.class).pattern()).parse(object.getString(field.getAnnotation(Metadata.class)
                .value())) : DATE_FORMAT.parse(object.getString(field.getAnnotation(Metadata.class).value())));
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
