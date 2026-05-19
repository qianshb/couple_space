package com.couple.config;

import io.minio.MinioClient;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

@Configuration
public class MinioConfig {

    private static final Logger log = LoggerFactory.getLogger(MinioConfig.class);

    @Value("${app.minio.endpoint}")
    private String endpoint;

    @Value("${app.minio.access-key}")
    private String accessKey;

    @Value("${app.minio.secret-key}")
    private String secretKey;

    @Bean
    public MinioClient minioClient() {
        try {
            URL url = new URL(endpoint);
            boolean secure = "https".equals(url.getProtocol());
            String host = url.getHost();
            int port = url.getPort();
            if (port == -1) {
                port = secure ? 443 : 9000;
            }

            log.info("Connecting to Minio: {}://{}:{} (secure={})", url.getProtocol(), host, port, secure);

            OkHttpClient httpClient = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            return MinioClient.builder()
                    .endpoint(host, port, secure)
                    .credentials(accessKey, secretKey)
                    .httpClient(httpClient)
                    .build();
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid Minio endpoint: " + endpoint, e);
        }
    }
}
