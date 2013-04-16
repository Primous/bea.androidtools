package br.com.bea.androidtools.api.model.converter;

import java.io.Serializable;
import java.lang.reflect.Field;
import org.json.JSONObject;
import android.content.ContentValues;
import android.database.Cursor;

public interface Converter {

    void convert(final ContentValues values, final Field field, final Object value);

    void convert(final JSONObject object, final Field field, final Object value);

    <S extends Serializable> void convert(final S serializable, Field field, final Cursor cursor);

    <S extends Serializable> void convert(final S serializable, Field field, final JSONObject object);
}
