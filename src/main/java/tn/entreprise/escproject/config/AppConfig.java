package tn.entreprise.escproject.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

@Configuration
public class AppConfig {

    /**
     * RestTemplate that trusts all SSL certificates.
     * Needed for Google AI APIs on Windows JDKs whose trust store
     * may not include the Google root CA. Fine for dev/student use.
     */
    @Bean
    public RestTemplate restTemplate() {
        try {
            TrustManager[] trustAll = { new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                public void checkClientTrusted(X509Certificate[] c, String a) {}
                public void checkServerTrusted(X509Certificate[] c, String a) {}
            }};

            SSLContext sslCtx = SSLContext.getInstance("TLS");
            sslCtx.init(null, trustAll, new SecureRandom());
            SSLSocketFactory socketFactory = sslCtx.getSocketFactory();

            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory() {
                @Override
                protected void prepareConnection(HttpURLConnection conn, String method) throws IOException {
                    if (conn instanceof HttpsURLConnection https) {
                        https.setSSLSocketFactory(socketFactory);
                        https.setHostnameVerifier((host, session) -> true);
                    }
                    super.prepareConnection(conn, method);
                }
            };

            return new RestTemplate(factory);

        } catch (Exception e) {
            return new RestTemplate();
        }
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
