package br.com.bea.androidtools.test.proxy;

import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import br.com.bea.androidtools.api.proxy.HttpProxy;
import br.com.bea.androidtools.api.proxy.Proxy;
import br.com.bea.androidtools.test.model.SimpleEntity;

public class AppProxyTest {

    private final Proxy<SimpleEntity> proxy = new HttpProxy<SimpleEntity>(SimpleEntity.class);

    public void testGet() throws ConnectException, MalformedURLException {
        proxy.connectTo(new URL("http://brunojensen.github.com/json/array.json")).encoding("ISO-8859-1").doGet();
    }

    public void testPost() throws ConnectException, MalformedURLException {
        proxy.connectTo(new URL("http://brunojensen.github.com/json/array.json")).encoding("ISO-8859-1").doPost();
    }

}
