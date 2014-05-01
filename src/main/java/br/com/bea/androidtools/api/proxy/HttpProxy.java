package br.com.bea.androidtools.api.proxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.ProtocolVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import br.com.bea.androidtools.api.android.AbstractEntityHandler;
import br.com.bea.androidtools.api.json.JSONContext;
import br.com.bea.androidtools.api.model.ValueObject;

public class HttpProxy<E extends ValueObject> implements Proxy<E> {

    private static final DefaultHttpClient client;
    static {
        final HttpParams params = new BasicHttpParams();
        params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        params.setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, HTTP.UTF_8);
        params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 15000);
        params.setParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false);
        final SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        final ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
        client = new DefaultHttpClient(cm, params);
    }
    private String encoding = "UTF-8";
    private Handler handler;
    private final Map<String, String> parameters = new HashMap<String, String>(0);
    private String password;
    private final Class<E> targetClass;
    private URL url;
    private String user;

    public HttpProxy(final Class<E> targetClass) {
        this.targetClass = targetClass;
    }

    @Override
    public Proxy<E> addHandler(final Handler handler) {
        this.handler = handler;
        return this;
    }

    @Override
    public Proxy<E> addParameter(final String key, final String value) {
        this.parameters.put(key, value);
        return this;
    }

    private HttpParams adjustToGet(final Map<String, String> params) {
        final HttpParams requestParams = new BasicHttpParams();
        for (final String key : params.keySet())
            requestParams.setParameter(key, params.get(key));
        return requestParams;
    }

    private List<NameValuePair> adjustToPost(final Map<String, String> params) {
        final List<NameValuePair> list = new ArrayList<NameValuePair>(0);
        for (final String key : params.keySet())
            list.add(new BasicNameValuePair(key, params.get(key)));
        return list;
    }

    @Override
    public Proxy<E> authenticateWith(final String user, final String password) {
        this.user = user;
        this.password = password;
        return this;
    }

    @Override
    public Proxy<E> connectTo(final URL url) {
        this.url = url;
        return this;
    }

    private HttpRequestBase createRequest(final boolean post) throws ConnectException {
        if (null != url) {
            if (post) {
                final HttpPost method = new HttpPost(url.toString());
                final List<NameValuePair> adjustParams = adjustToPost(parameters);
                if (!adjustParams.isEmpty()) try {
                    method.setEntity(new UrlEncodedFormEntity(adjustParams, HTTP.UTF_8));
                } catch (final UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                return method;
            }
            final HttpGet method = new HttpGet(url.toString());
            method.setParams(adjustToGet(parameters));
            return method;
        }
        throw new ConnectException("Nao foi possivel criar request !");
    }

    private ResponseHandler<List<E>> createResponseHandler(final Handler handler) {
        return new ResponseHandler<List<E>>() {
            @Override
            public List<E> handleResponse(final HttpResponse response) {
                final HttpEntity httpEntity = response.getEntity();
                if (null != httpEntity)
                    try {
                        final Message message = handler.obtainMessage();
                        message.setData(new Bundle());
                        final StringBuilder builder = read(httpEntity);
                        if (handler instanceof AbstractEntityHandler) {
                            final List<E> list = new JSONContext<E>(targetClass).unmarshal(new JSONArray(builder
                                .toString()));
                            message.getData().putSerializable(RESPONSE, (Serializable) list);
                        } else message.getData().putSerializable(RESPONSE, builder.toString());
                        handler.sendMessage(message);
                    } catch (final Exception e) {
                        e.printStackTrace();
                    }
                return Collections.emptyList();
            }

        };
    }

    @Override
    public List<E> doGet() {
        if (null != user && null != password)
            client.getCredentialsProvider().setCredentials(AuthScope.ANY,
                                                           new UsernamePasswordCredentials(user, password));
        if (null != handler) {
            final ResponseHandler<List<E>> responseHandler = createResponseHandler(handler);
            try {
                client.execute(createRequest(false), responseHandler);
            } catch (final Exception e) {
                throwException(responseHandler, e);
            }
        } else try {
            return new JSONContext<E>(targetClass).unmarshal(new JSONArray(read(
                                                                                client.execute(createRequest(false))
                                                                                    .getEntity()).toString()));
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    @Override
    public List<E> doPost() {
        if (null != user && null != password)
            client.getCredentialsProvider().setCredentials(AuthScope.ANY,
                                                           new UsernamePasswordCredentials(user, password));
        final Map<String, String> headers = new HashMap<String, String>(0);
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        client.addRequestInterceptor(new HttpRequestInterceptor() {
            @Override
            public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
                for (final String key : headers.keySet())
                    if (!request.containsHeader(key)) request.addHeader(key, headers.get(key));
            }
        });
        if (null != handler) {
            final ResponseHandler<List<E>> responseHandler = createResponseHandler(handler);
            try {
                client.execute(createRequest(true), responseHandler);
            } catch (final Exception e) {
                throwException(responseHandler, e);
            }
        } else try {
            return new JSONContext<E>(targetClass).unmarshal(new JSONArray(read(
                                                                                client.execute(createRequest(false))
                                                                                    .getEntity()).toString()));
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    @Override
    public Proxy<E> encoding(final String encoding) {
        this.encoding = encoding;
        return this;
    }

    private StringBuilder read(final HttpEntity httpEntity) throws UnsupportedEncodingException, IOException {
        final StringBuilder builder = new StringBuilder();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(httpEntity.getContent(), encoding));
        String line = null;
        while ((line = reader.readLine()) != null)
            builder.append(line);
        return builder;
    }

    private void throwException(final ResponseHandler<List<E>> responseHandler, final Exception e) {
        final BasicHttpResponse errorResponse = new BasicHttpResponse(new ProtocolVersion("HTTP_ERROR", 1, 1),
                                                                      500,
                                                                      "ERROR");
        errorResponse.setReasonPhrase(e.getMessage());
        try {
            responseHandler.handleResponse(errorResponse);
        } catch (final Exception ex) {
        }
    }

}