package mybar;

import org.junit.platform.commons.util.Preconditions;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public final class CommonPaths implements InitializingBean {
    public static final String API_PATH = "http://localhost:8080/api/bar/v1/";

    @Autowired
    private Environment env;

    @Value("${http.protocol}")
    private String protocol;

    @Value("${http.host}")
    private String host;

    @Value("${http.port}")
    private int port;

    public CommonPaths() {
        super();
    }

    // API

    public String getServerRoot() {
        if (port == 80) {
            return protocol + "://" + host;
        }
        return protocol + "://" + host + ":" + port;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    //

    @Override
    public void afterPropertiesSet() {
        if (protocol == null || protocol.equals("${http.protocol}")) {
            protocol = Preconditions.notBlank(env.getProperty("http.protocol"), "protocol should not be empty");
        }
        if (host == null || host.equals("${http.host}")) {
            host = Preconditions.notBlank(env.getProperty("http.host"), "host should not be empty");
        }
        var portVar = Preconditions.notNull(env.getProperty("http.port"), "port should not be empty");
        this.port = Integer.parseInt(portVar);
    }

}
