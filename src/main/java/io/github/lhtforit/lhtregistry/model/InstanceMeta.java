package io.github.lhtforit.lhtregistry.model;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Leo
 * @date 2024/04/22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"scheme", "host", "port", "context"})
public class InstanceMeta {

    private String scheme;
    private String host;
    private int port;
    private String context;//path

    private boolean status;// 上下线状态 online or offline
    private Map<String, String> parameters = new HashMap<>();//表示哪个机房的，可以加各种参数

    public InstanceMeta(String scheme, String host, int port,String context) {
        this.scheme = scheme;
        this.host = host;
        this.port = port;
        this.context = context;
    }

    public static InstanceMeta from(String url) {
        URI uri = URI.create(url);
        return new InstanceMeta(uri.getScheme(),
                uri.getHost(),
                uri.getPort(),
                uri.getPath().substring(1));//path有个/去掉
    }

    public String toPath() {
        return String.format("%s_%d", host, port);
    }

    public String toUrl() {
        return String.format("%s://%s:%d/%s", scheme, host, port, context);
    }

    public static InstanceMeta http(String host, int port) {
        return new InstanceMeta("http", host, port,"lhtrpc");
    }

    public String toMetas() {
        return JSON.toJSONString(this.parameters);
    }
}
