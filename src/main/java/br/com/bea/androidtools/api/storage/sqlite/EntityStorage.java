/*
The MIT License (MIT)
Copyright (c) 2013 B&A Tecnologia and Collaborators

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
documentation files (the "Software"), to deal in the Software without restriction, including without limitation
the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions 
of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED 
TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
IN THE SOFTWARE.
 */

package br.com.bea.androidtools.api.storage.sqlite;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import br.com.bea.androidtools.api.model.Entity;
import br.com.bea.androidtools.api.model.EntityMapper;
import br.com.bea.androidtools.api.model.FieldMapper;
import br.com.bea.androidtools.api.model.annotations.Column;
import br.com.bea.androidtools.api.model.annotations.Id;
import br.com.bea.androidtools.api.model.annotations.Table;
import br.com.bea.androidtools.api.storage.Query;
import br.com.bea.androidtools.api.storage.StorageManager;
import br.com.bea.androidtools.api.storage.WrongQueryImplementatioException;

public class EntityStorage implements StorageManager {

    private static final StorageManager INSTANCE = new EntityStorage();
    @SuppressWarnings("rawtypes")
    private static ThreadLocal<SQlite> sqlite;

    public static synchronized StorageManager getInstance() {
        return EntityStorage.INSTANCE;
    }

    private EntityStorage() {
    }

    @Override
    public void close() {
        getSqlite().close();
    }

    @Override
    public Long count(final Query query) {
        if (!(query instanceof EntityQuery))
            throw new WrongQueryImplementatioException(String.format("Use: %s", EntityQuery.class.getSimpleName()));
        final EntityQuery queryBuilder = (EntityQuery) query;
        final Cursor cursor = queryBuilder.build(getSqlite().getReadableDatabase());
        final Long result = new Long(cursor.getCount());
        close();
        return result;
    }

    @Override
    public <E extends Entity<?>> void delete(final E entity) {
        try {
            String idColumn = null;
            Integer id = 0;
            for (final Entry<String, FieldMapper> entry : EntityMapper.get(entity.getClass()).getColumnsFields()
                .entrySet()) {
                final Field field = entry.getValue().getField();
                if (field.isAnnotationPresent(Id.class)) {
                    id = ((Number) entry.getValue().getValue(entity)).intValue();
                    idColumn = entry.getKey();
                    break;
                }
            }
            if (id == 0) throw new SQLiteException("Entidade não possui Id");
            getSqlite().getWritableDatabase()
                .delete(entity.getClass().getAnnotation(Table.class).name(), String.format("%s = ? ", idColumn),
                        new String[] { id.toString() });
            close();
        } catch (final Exception e) {
            throw new SQLiteException(e.getLocalizedMessage());
        }
    }

    @Override
    public <E extends Entity<?>> boolean deleteAll(final Class<E> targetEntity) {
        try {
            final boolean result = getSqlite().getWritableDatabase().delete(targetEntity.getAnnotation(Table.class)
                                                                                .name(), null, null) > 1 ? true : false;
            close();
            return result;
        } catch (final Exception e) {
            throw new SQLiteException(e.getLocalizedMessage());
        }
    }

    @Override
    public <E extends Entity<?>> E find(final E entity) {
        try {
            final List<E> result = search(EntityQuery.select().from(entity.getClass()).where(Restriction
                                                                                                 .eq("id", entity
                                                                                                     .getId())));
            close();
            return !result.isEmpty() ? result.get(0) : null;
        } catch (final Exception e) {
            throw new SQLiteException(e.getLocalizedMessage());
        }
    }

    @SuppressWarnings("rawtypes")
    private SQlite getSqlite() {
        return EntityStorage.sqlite.get();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public StorageManager init(final Context context, final String database, final Class<?>... targetClasses) {
        if (null == EntityStorage.sqlite) EntityStorage.sqlite = new ThreadLocal<SQlite>() {
            @Override
            protected SQlite initialValue() {
                return new SQlite(context, database, Arrays.asList(targetClasses));
            }
        };
        getSqlite().getReadableDatabase().setLocale(Locale.US);
        getSqlite().getWritableDatabase().setLocale(Locale.US);
        return this;
    }

    @Override
    public <E extends Entity<?>> E persist(final E entity) {
        try {
            final ContentValues values = new ContentValues();
            for (final Entry<String, FieldMapper> entry : EntityMapper.get(entity.getClass()).getColumnsFields()
                .entrySet())
                entry.getValue().convert(values, entry.getValue().getField(), entry.getValue().getValue(entity));
            getSqlite().getWritableDatabase().insert(entity.getClass().getAnnotation(Table.class).name(), null, values);
            close();
        } catch (final Exception e) {
            throw new SQLiteException(e.getLocalizedMessage());
        }
        return entity;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E extends Entity<?>> List<E> search(final Query query) {
        if (!(query instanceof EntityQuery))
            throw new WrongQueryImplementatioException(String.format("Use: %s", EntityQuery.class.getSimpleName()));
        final EntityQuery queryBuilder = (EntityQuery) query;
        final List<E> result = new LinkedList<E>();
        final Cursor cursor = queryBuilder.build(getSqlite().getReadableDatabase());
        if (cursor.moveToFirst())
            do
                try {
                    final E value = (E) queryBuilder.getTargetClass().newInstance();
                    for (final Entry<String, FieldMapper> entry : EntityMapper.get((Class<E>) queryBuilder
                                                                                       .getTargetClass())
                        .getColumnsFields().entrySet())
                        entry.getValue().convert(value, entry.getValue().getField(), cursor);
                    result.add(value);

                } catch (final Exception e) {
                    throw new SQLiteException(e.getLocalizedMessage());
                }
            while (cursor.moveToNext());
        cursor.close();
        close();
        return result;
    }

    @Override
    public <E extends Entity<?>> E update(final E entity) {
        try {
            final ContentValues values = new ContentValues();
            String idColumn = null;
            Integer id = 0;
            for (final Entry<String, FieldMapper> entry : EntityMapper.get(entity.getClass()).getColumnsFields()
                .entrySet()) {
                entry.getValue().convert(values, entry.getValue().getField(), entry.getValue().getValue(entity));
                if (entry.getValue().getField().isAnnotationPresent(Id.class)) {
                    idColumn = entry.getValue().getField().getAnnotation(Column.class).name();
                    id = ((Number) entry.getValue().getValue(entity)).intValue();
                }
            }
            if (id == 0) throw new SQLiteException("Entidade não possui Id");
            getSqlite().getWritableDatabase()
                .update(entity.getClass().getAnnotation(Table.class).name(), values,
                        String.format("%s = ? ", idColumn), new String[] { id.toString() });
            close();
        } catch (final Exception e) {
            throw new SQLiteException(e.getLocalizedMessage());
        }
        return entity;
    }

}
