package br.com.bea.androidtools.api.storage.sharedpreferences;

import java.util.Arrays;
import java.util.List;
import br.com.bea.androidtools.api.model.Entity;
import br.com.bea.androidtools.api.storage.Query;

public class PreferencesQuery implements Query {

    public static synchronized PreferencesQuery select() {
        return new PreferencesQuery();
    }

    private Object id;
    private Class<?> targetClass;

    @SuppressWarnings("unchecked")
    <E extends Entity<?>> List<E> build(final List<E> list) {
        if (null != id) for (final E e : list)
            if (e.getId().equals(id)) return Arrays.asList(e);
        return list;
    }

    public <E extends Entity<?>> PreferencesQuery from(final Class<E> targetClass) {
        this.targetClass = targetClass;
        return this;
    }

    public Object getSelection() {
        return id;
    }

    @Override
    public Class<?> getTargetClass() {
        return targetClass;
    }

    public <E extends Entity<?>> PreferencesQuery whereId(final E e) {
        id = e.getId();
        return this;
    }
}
