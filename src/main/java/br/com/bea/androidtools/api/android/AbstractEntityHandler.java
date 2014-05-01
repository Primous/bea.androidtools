package br.com.bea.androidtools.api.android;

import java.util.Collections;
import java.util.List;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import br.com.bea.androidtools.api.model.ValueObject;
import br.com.bea.androidtools.api.proxy.Proxy;

public abstract class AbstractEntityHandler<E extends ValueObject> extends Handler {

    @SuppressWarnings("unchecked")
    @Override
    public void handleMessage(final Message message) {
        final Bundle bundle = message.getData();
        resultCallback((List<E>) (null != bundle ? bundle.getSerializable(Proxy.RESPONSE) : Collections.emptyList()));
    }

    public abstract void resultCallback(final List<E> result);
}
