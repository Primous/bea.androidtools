package br.com.bea.androidtools.test.proxy;

import java.net.ConnectException;
import java.util.Properties;
import org.json.JSONArray;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import br.com.bea.androidtools.api.proxy.HttpProxy;
import br.com.bea.androidtools.api.proxy.Proxy;

public class AppProxyTest {

    private final Properties mockProperties = new Properties();
    private final Properties mockWrongProperties = new Properties();
    private final Proxy<JSONArray> proxy = new HttpProxy();

    @Before
    public void setUp() throws Exception {
        mockProperties.setProperty("url_connection", "http://brunojensen.github.com/json/array.json");
        mockProperties.setProperty("method", "GET");
        mockWrongProperties.setProperty("url_connection", "http://www.google.com.br/");
    }

    @Test
    public void testConnection() {
        proxy.connect(mockProperties);
        Assert.assertTrue(proxy.isConnected());
    }

    @Test
    public void testRequest() throws ConnectException {
        testConnection();
        proxy.request(null);
    }

    @Test
    public void testWrongConnection() {
        proxy.connect(mockWrongProperties);
        Assert.assertFalse(proxy.isConnected());
    }

}
