package br.com.bea.androidtools.api.sqlite;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import br.com.bea.androidtools.api.annotations.Column;
import br.com.bea.androidtools.api.annotations.Id;
import br.com.bea.androidtools.api.annotations.Table;
import br.com.bea.androidtools.api.model.Entity;
import br.com.bea.androidtools.api.model.EntityUtils;

public class EntityManagerImpl implements EntityManager {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    private static final EntityManager INSTANCE = new EntityManagerImpl();
    @SuppressWarnings("rawtypes")
    private static SQlite sqlite;

    public static EntityManager getInstance() {
        return INSTANCE;
    }

    private EntityManagerImpl() {
    }

    @Override
    public void close() {
        sqlite.close();
    }

    private Object convert(final Field field, final Cursor cursor) throws Exception {
        if (field.getType().equals(Integer.class))
            return cursor.getInt(cursor.getColumnIndex(field.getAnnotation(Column.class).name()));
        if (field.getType().equals(Date.class))
            return DATE_FORMAT.parse(cursor.getString(cursor.getColumnIndex(field.getAnnotation(Column.class).name())));
        return cursor.getString(cursor.getColumnIndex(field.getAnnotation(Column.class).name()));
    }

    @Override
    public <E extends Entity<?>> void delete(final E entity) {
        try {
            String idColumn = null;
            Integer id = 0;
            for (final Field field : EntityUtils.columnFields(entity.getClass())) {
                if (field.isAnnotationPresent(Id.class)) {
                    field.setAccessible(true);
                    idColumn = field.getAnnotation(Column.class).name();
                    id = (Integer) field.get(entity);
                    break;
                }
            }
            if (id == 0) throw new SQLiteException("Entidade n�o possui Id");
            sqlite.getWritableDatabase().delete(entity.getClass().getAnnotation(Table.class).name(),
                                                String.format("%s = ? ", idColumn), new String[] { id.toString() });
        } catch (final Exception e) {
            throw new SQLiteException(e.getLocalizedMessage());
        }
    }

    @Override
    public <E extends Entity<?>> E find(final E entity) {
        try {
            final List<String> columns = new ArrayList<String>(0);
            for (final Field field : EntityUtils.columnFields(entity.getClass()))
                columns.add(field.getAnnotation(Column.class).name());

            final Cursor cursor = sqlite.getReadableDatabase().query(entity.getClass().getAnnotation(Table.class)
                                                                         .name(),
                                                                     columns.toArray(new String[columns.size()]), null,
                                                                     null, null, null, null);
            @SuppressWarnings("unchecked")
            final E value = (E) entity.getClass().newInstance();
            if (cursor.moveToFirst()) while (cursor.moveToNext())
                for (final Field field : EntityUtils.columnFields(entity.getClass())) {
                    field.setAccessible(true);
                    field.set(value, convert(field, cursor));
                }
            return value;
        } catch (final Exception e) {
            throw new SQLiteException(e.getLocalizedMessage());
        }
    }

    @Override
    public <E extends Entity<?>> EntityManager init(final Context context,
                                                    final String database,
                                                    final List<Class<E>> targetClasses) {
        sqlite = new SQlite<E>(context, database, targetClasses);
        return this;
    }

    @Override
    public <E extends Entity<?>> E persist(final E entity) {
        try {
            final ContentValues values = new ContentValues();
            for (final Field field : EntityUtils.columnFields(entity.getClass())) {
                field.setAccessible(true);
                values.put(field.getAnnotation(Column.class).name(), String.valueOf(field.get(entity)));
            }
            sqlite.getWritableDatabase().insert(entity.getClass().getAnnotation(Table.class).name(), null, values);
        } catch (final Exception e) {
            throw new SQLiteException(e.getLocalizedMessage());
        }
        return entity;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E extends Entity<?>> List<E> search(final QueryBuilder query) {
        final List<E> result = new LinkedList<E>();
        final Cursor cursor = query.build(sqlite.getReadableDatabase());
        if (cursor.moveToFirst()) while (cursor.moveToNext())
            try {
                final E value = (E) query.getTargetClass().newInstance();
                for (final Field field : EntityUtils.columnFields((Class<E>) query.getTargetClass())) {
                    field.setAccessible(true);
                    field.set(value, convert(field, cursor));
                }
                result.add(value);
            } catch (final Exception e) {
                throw new SQLiteException(e.getLocalizedMessage());
            }

        return result;
    }

    @Override
    public <E extends Entity<?>> E update(final E entity) {
        try {
            final ContentValues values = new ContentValues();
            String idColumn = null;
            Integer id = 0;
            for (final Field field : EntityUtils.columnFields(entity.getClass())) {
                field.setAccessible(true);
                values.put(field.getAnnotation(Column.class).name(), String.valueOf(field.get(entity)));
                if (field.isAnnotationPresent(Id.class)) {
                    idColumn = field.getAnnotation(Column.class).name();
                    id = (Integer) field.get(entity);
                }
            }
            if (id == 0) throw new SQLiteException("Entidade n�o possui Id");
            sqlite.getWritableDatabase().update(entity.getClass().getAnnotation(Table.class).name(), values,
                                                String.format("%s = ? ", idColumn), new String[] { id.toString() });
        } catch (final Exception e) {
            throw new SQLiteException(e.getLocalizedMessage());
        }
        return entity;
    }

}