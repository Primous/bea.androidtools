/*
The MIT License (MIT)
Copyright (c) 2013 B&A Tecnologia

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
package br.com.bea.androidtools.api.model;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import br.com.bea.androidtools.api.model.annotations.Column;
import br.com.bea.androidtools.api.model.annotations.Metadata;
import br.com.bea.androidtools.api.model.annotations.Name;
import br.com.bea.androidtools.api.model.annotations.Table;
import br.com.bea.androidtools.api.model.annotations.Transient;

public final class EntityMapper {
    private static final Map<Integer, EntityMapper> CACHE = new LinkedHashMap<Integer, EntityMapper>();

    private static final synchronized <S extends ValueObject> EntityMapper create(final Class<S> targetClass) {
        return new EntityMapper(targetClass);
    }

    public static final synchronized <S extends ValueObject> EntityMapper get(final Class<S> targetClass) {
        if (!EntityMapper.CACHE.containsKey(targetClass.hashCode())) EntityMapper.create(targetClass);
        return EntityMapper.CACHE.get(targetClass.hashCode());
    }

    private final Integer classHashCode;
    private final Map<String, FieldMapper> columnsFields = new LinkedHashMap<String, FieldMapper>(0);
    private final Map<String, FieldMapper> metadataFields = new LinkedHashMap<String, FieldMapper>(0);
    private String name;

    private <S extends ValueObject> EntityMapper(final Class<S> targetClass) {
        classHashCode = targetClass.hashCode();
        if (targetClass.isAnnotationPresent(Table.class)) {
            final Table annotation = targetClass.getAnnotation(Table.class);
            name = annotation.name();
        }
        if (targetClass.isAnnotationPresent(Name.class)) {
            final Name annotation = targetClass.getAnnotation(Name.class);
            name = annotation.value();
        }
        for (final Field field : targetClass.getDeclaredFields())
            if (!field.isAnnotationPresent(Transient.class)) {
                final FieldMapper fieldMapper = FieldMapper.create(field);
                if (field.isAnnotationPresent(Column.class))
                    columnsFields.put(field.getAnnotation(Column.class).name(), fieldMapper);
                if (field.isAnnotationPresent(Metadata.class))
                    metadataFields.put(field.getAnnotation(Metadata.class).value(), fieldMapper);
            }
        EntityMapper.CACHE.put(getClassHashCode(), this);
    }

    public Integer getClassHashCode() {
        return classHashCode;
    }

    public Map<String, FieldMapper> getColumnsFields() {
        return new LinkedHashMap<String, FieldMapper>(columnsFields);
    }

    public Map<String, FieldMapper> getMetadataFields() {
        return new LinkedHashMap<String, FieldMapper>(metadataFields);
    }

    public String getName() {
        return name;
    }
}
