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

package br.com.bea.androidtools.api.storage;

import java.util.List;
import android.content.Context;
import br.com.bea.androidtools.api.model.Entity;

public interface StorageManager {

    void close();

    Long count(Query query);

    <E extends Entity<?>> void delete(E entity);

    <E extends Entity<?>> boolean deleteAll(Class<E> targetEntity);

    <E extends Entity<?>> E find(E entity);

    StorageManager init(Context context, String database, Class<?>... targetClasses);

    <E extends Entity<?>> E persist(E entity);

    <E extends Entity<?>> List<E> search(Query query);

    <E extends Entity<?>> E update(E entity);

}