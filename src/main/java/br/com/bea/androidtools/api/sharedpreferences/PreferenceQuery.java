package br.com.bea.androidtools.api.sharedpreferences;

import java.util.Arrays;
import java.util.List;
import br.com.bea.androidtools.api.model.Entity;
import br.com.bea.androidtools.api.storage.Query;

public class PreferenceQuery implements Query {

    public static synchronized PreferenceQuery select() {
        return new PreferenceQuery();
    }

    private Object id;
    private Class<?> targetClass;

    @SuppressWarnings("unchecked")
    <E extends Entity<?>> List<E> build(final List<E> list) {
        if (null != id) for (final E e : list)
            if (e.getId().equals(id)) return Arrays.asList(e);
        return list;
    }

    public <E extends Entity<?>> PreferenceQuery from(final Class<E> targetClass) {
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

    public <E extends Entity<?>> PreferenceQuery whereId(final E e) {
        id = e.getId();
        return this;
    }
}
