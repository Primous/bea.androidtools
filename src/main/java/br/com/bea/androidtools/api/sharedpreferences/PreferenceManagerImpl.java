package br.com.bea.androidtools.api.sharedpreferences;

import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import br.com.bea.androidtools.api.annotations.Table;
import br.com.bea.androidtools.api.json.JSONContextImpl;
import br.com.bea.androidtools.api.model.Entity;
import br.com.bea.androidtools.api.storage.EntityManager;
import br.com.bea.androidtools.api.storage.Query;
import br.com.bea.androidtools.api.storage.WrongQueryImplementatioException;

public class PreferenceManagerImpl implements EntityManager {

    private static final String EMPTY = new JSONArray().toString();
    private static final EntityManager INSTANCE = new PreferenceManagerImpl();
    private static SharedPreferences preferences;

    public static EntityManager getInstance() {
        return PreferenceManagerImpl.INSTANCE;
    }

    private PreferenceManagerImpl() {
    }

    @Override
    public void close() {
    }

    @Override
    public <E extends Entity<?>> void delete(final E entity) {
        final List<E> list = search(PreferenceQuery.select().from(entity.getClass()));
        final E e = find(entity);
        list.remove(e);
    }

    @Override
    public <E extends Entity<?>> boolean deleteAll(final Class<E> targetEntity) {
        try {
            final Editor editor = preferences.edit();
            editor.putString(targetEntity.getAnnotation(Table.class).name(), EMPTY);
            editor.commit();
            return true;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <E extends Entity<?>> E find(final E entity) {
        final List<E> list = search(PreferenceQuery.select().from(entity.getClass()).whereId(entity));
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public EntityManager init(final Context context, final String database, final Class<?>... targetClasses) {
        preferences = context.getSharedPreferences(database, Context.MODE_PRIVATE);
        return this;
    }

    @Override
    public <E extends Entity<?>> E persist(final E entity) {
        final List<E> list = search(PreferenceQuery.select().from(entity.getClass()));
        if (!list.contains(entity)) {
            list.add(entity);
            persistAll(entity.getClass(), list);
        }
        return entity;
    }

    @SuppressWarnings("unchecked")
    private <E extends Entity<?>> void persistAll(final Class<?> targetClass, final List<E> list) {
        final Editor editor = preferences.edit();
        editor.putString(targetClass.getAnnotation(Table.class).name(), new JSONContextImpl<E>((Class<E>) targetClass)
            .marshal(list).toString());
        editor.commit();
    }

    @Override
    public <E extends Entity<?>> List<E> search(final Query query) {
        if (!(query instanceof PreferenceQuery))
            throw new WrongQueryImplementatioException(String.format("Use: %s", PreferenceQuery.class.getSimpleName()));
        final PreferenceQuery preferenceQuery = (PreferenceQuery) query;
        @SuppressWarnings("unchecked")
        final Class<E> targetClass = (Class<E>) preferenceQuery.getTargetClass();
        try {
            final List<E> list = new JSONContextImpl<E>(targetClass).unmarshal(new JSONArray(preferences
                .getString(targetClass.getAnnotation(Table.class).name(), EMPTY)));
            return preferenceQuery.build(list);
        } catch (final JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <E extends Entity<?>> E update(final E entity) {
        final List<E> list = search(PreferenceQuery.select().from(entity.getClass()));
        final E e = find(entity);
        list.remove(e);
        list.add(entity);
        persistAll(entity.getClass(), list);
        return entity;
    }

}
