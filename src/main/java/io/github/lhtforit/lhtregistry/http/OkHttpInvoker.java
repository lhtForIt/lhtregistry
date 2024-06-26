package io.github.lhtforit.lhtregistry.http;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author Leo
 * @date 2024/03/26
 */
@Slf4j
public class OkHttpInvoker implements HttpInvoker {

    private final static MediaType MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");

    private OkHttpClient client;

    public OkHttpInvoker(int readTimeout, int writeTimeout, int connectTimeout) {
        this.client = new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(16,60, TimeUnit.SECONDS))
                .readTimeout(readTimeout, TimeUnit.MILLISECONDS)
                .writeTimeout(writeTimeout,TimeUnit.MILLISECONDS)
                .connectTimeout(connectTimeout,TimeUnit.MILLISECONDS)
                .build();
    }

    @Override
    public String post(String requestString,String url) {
        log.debug(" ===> post  url = {}, requestString = {}", url, requestString);
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(requestString, MEDIA_TYPE))
                .build();
        try {
            String responseJson = client.newCall(request).execute().body().string();
            log.debug(" ===> resJson = " + responseJson);
            return responseJson;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String get(String url) {
        log.debug(" ===> get url = " + url);
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        try {
            String respJson = client.newCall(request).execute().body().string();
            log.debug(" ===> respJson = " + respJson);
            return respJson;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
