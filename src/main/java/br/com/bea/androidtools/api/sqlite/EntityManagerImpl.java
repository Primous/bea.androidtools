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

package br.com.bea.androidtools.api.sqlite;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import br.com.bea.androidtools.api.annotations.Column;
import br.com.bea.androidtools.api.annotations.Id;
import br.com.bea.androidtools.api.annotations.Table;
import br.com.bea.androidtools.api.model.Entity;
import br.com.bea.androidtools.api.model.EntityUtils;
import br.com.bea.androidtools.api.storage.EntityManager;
import br.com.bea.androidtools.api.storage.Query;
import br.com.bea.androidtools.api.storage.WrongQueryImplementatioException;

public class EntityManagerImpl implements EntityManager {

    private static final EntityManager INSTANCE = new EntityManagerImpl();
    @SuppressWarnings("rawtypes")
    private static SQlite sqlite;

    public static EntityManager getInstance() {
        return EntityManagerImpl.INSTANCE;
    }

    private EntityManagerImpl() {
    }

    @Override
    public void close() {
        EntityManagerImpl.sqlite.close();
    }

    @Override
    public <E extends Entity<?>> void delete(final E entity) {
        try {
            String idColumn = null;
            Integer id = 0;
            for (final Field field : EntityUtils.columnFields(entity.getClass()))
                if (field.isAnnotationPresent(Id.class)) {
                    field.setAccessible(true);
                    idColumn = field.getAnnotation(Column.class).name();
                    id = (Integer) field.get(entity);
                    break;
                }
            if (id == 0) throw new SQLiteException("Entidade n�o possui Id");
            EntityManagerImpl.sqlite.getWritableDatabase().delete(entity.getClass().getAnnotation(Table.class).name(),
                                                                  String.format("%s = ? ", idColumn),
                                                                  new String[] { id.toString() });
            close();
        } catch (final Exception e) {
            throw new SQLiteException(e.getLocalizedMessage());
        }
    }

    @Override
    public <E extends Entity<?>> boolean deleteAll(final Class<E> targetEntity) {
        try {
            final boolean result = sqlite.getWritableDatabase().delete(targetEntity.getAnnotation(Table.class).name(),
                                                                       null, null) > 1 ? true : false;
            close();
            return result;
        } catch (final Exception e) {
            throw new SQLiteException(e.getLocalizedMessage());
        }
    }

    @Override
    public <E extends Entity<?>> E find(final E entity) {
        try {
            final List<E> result = search(SQLQuery.select().from(entity.getClass()).where(Restriction.eq("id", entity
                                                                                              .getId())));
            close();
            return !result.isEmpty() ? result.get(0) : null;
        } catch (final Exception e) {
            throw new SQLiteException(e.getLocalizedMessage());
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public EntityManager init(final Context context, final String database, final Class<?>... targetClasses) {
        EntityManagerImpl.sqlite = new SQlite(context, database, Arrays.asList(targetClasses));
        EntityManagerImpl.sqlite.getReadableDatabase().setLocale(Locale.US);
        EntityManagerImpl.sqlite.getWritableDatabase().setLocale(Locale.US);
        return this;
    }

    @Override
    public <E extends Entity<?>> E persist(final E entity) {
        try {
            final ContentValues values = new ContentValues();
            for (final Field field : EntityUtils.columnFields(entity.getClass())) {
                field.setAccessible(true);
                values.put(field.getAnnotation(Column.class).name(), EntityUtils.valueOf(field, entity));
            }
            EntityManagerImpl.sqlite.getWritableDatabase().insert(entity.getClass().getAnnotation(Table.class).name(),
                                                                  null, values);
            close();
        } catch (final Exception e) {
            throw new SQLiteException(e.getLocalizedMessage());
        }
        return entity;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E extends Entity<?>> List<E> search(final Query query) {
        if (!(query instanceof SQLQuery))
            throw new WrongQueryImplementatioException(String.format("Use: %s", SQLQuery.class.getSimpleName()));
        final SQLQuery queryBuilder = (SQLQuery) query;
        final List<E> result = new LinkedList<E>();
        final Cursor cursor = queryBuilder.build(EntityManagerImpl.sqlite.getReadableDatabase());
        if (cursor.moveToFirst()) do
            try {
                final E value = (E) queryBuilder.getTargetClass().newInstance();
                for (final Field field : EntityUtils.columnFields((Class<E>) queryBuilder.getTargetClass())) {
                    field.setAccessible(true);
                    field.set(value, EntityUtils.convert(field, cursor));
                }
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
            for (final Field field : EntityUtils.columnFields(entity.getClass())) {
                field.setAccessible(true);
                values.put(field.getAnnotation(Column.class).name(), EntityUtils.valueOf(field, entity));
                if (field.isAnnotationPresent(Id.class)) {
                    idColumn = field.getAnnotation(Column.class).name();
                    id = (Integer) field.get(entity);
                }
            }
            if (id == 0) throw new SQLiteException("Entidade n�o possui Id");
            EntityManagerImpl.sqlite.getWritableDatabase().update(entity.getClass().getAnnotation(Table.class).name(),
                                                                  values, String.format("%s = ? ", idColumn),
                                                                  new String[] { id.toString() });
            close();
        } catch (final Exception e) {
            throw new SQLiteException(e.getLocalizedMessage());
        }
        return entity;
    }

}
