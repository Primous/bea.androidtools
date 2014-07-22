package br.com.bea.androidtools.api.storage.sharedpreferences;

import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import br.com.bea.androidtools.api.json.JSONContext;
import br.com.bea.androidtools.api.model.Entity;
import br.com.bea.androidtools.api.model.annotations.Table;
import br.com.bea.androidtools.api.storage.Query;
import br.com.bea.androidtools.api.storage.StorageManager;
import br.com.bea.androidtools.api.storage.WrongQueryImplementatioException;

public class PreferencesStorage implements StorageManager {

    private static final String EMPTY = new JSONArray().toString();
    private static final StorageManager INSTANCE = new PreferencesStorage();

    public static synchronized StorageManager getInstance() {
        return PreferencesStorage.INSTANCE;
    }

    private SharedPreferences preferences;

    private PreferencesStorage() {
    }

    @Override
    public void close() {
    }

    @Override
    public Long count(final Query query) {
        return null;
    }

    @Override
    public <E extends Entity<?>> void delete(final E entity) {
        final List<E> list = search(PreferencesQuery.select().from(entity.getClass()));
        final E e = find(entity);
        list.remove(e);
        persistAll(entity.getClass(), list);
    }

    @Override
    public <E extends Entity<?>> boolean deleteAll(final Class<E> targetEntity) {
        try {
            final Editor editor = getPreferences().edit();
            editor.putString(targetEntity.getAnnotation(Table.class).name(), EMPTY);
            editor.commit();
            return true;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <E extends Entity<?>> E find(final E entity) {
        final List<E> list = search(PreferencesQuery.select().from(entity.getClass()).whereId(entity));
        return list.isEmpty() ? null : list.get(0);
    }

    private synchronized SharedPreferences getPreferences() {
        return preferences;
    }

    @Override
    public StorageManager init(final Context context, final String database, final Class<?>... targetClasses) {
        if (null == getPreferences()) setPreferences(context.getSharedPreferences(database, Context.MODE_PRIVATE));
        return this;
    }

    @Override
    public <E extends Entity<?>> E persist(final E entity) {
        final List<E> list = search(PreferencesQuery.select().from(entity.getClass()));
        if (!list.contains(entity)) {
            list.add(entity);
            persistAll(entity.getClass(), list);
        }
        return entity;
    }

    @SuppressWarnings("unchecked")
    private <E extends Entity<?>> void persistAll(final Class<?> targetClass, final List<E> list) {
        final Editor editor = preferences.edit();
        editor.putString(targetClass.getAnnotation(Table.class).name(), new JSONContext<E>((Class<E>) targetClass)
                         .marshal(list).toString());
        editor.commit();
    }

    @Override
    public <E extends Entity<?>> List<E> search(final Query query) {
        if (!(query instanceof PreferencesQuery))
            throw new WrongQueryImplementatioException(String.format("Use: %s", PreferencesQuery.class.getSimpleName()));
        final PreferencesQuery preferenceQuery = (PreferencesQuery) query;
        @SuppressWarnings("unchecked")
        final Class<E> targetClass = (Class<E>) preferenceQuery.getTargetClass();
        try {
            final List<E> list = new JSONContext<E>(targetClass).unmarshal(new JSONArray(preferences
                                                                                         .getString(targetClass.getAnnotation(Table.class).name(), EMPTY)));
            return preferenceQuery.build(list);
        } catch (final JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized void setPreferences(final SharedPreferences sharedPreferences) {
        preferences = sharedPreferences;
    }

    @Override
    public <E extends Entity<?>> E update(final E entity) {
        delete(entity);
        persist(entity);
        return entity;
    }

}
