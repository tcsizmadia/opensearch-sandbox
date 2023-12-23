package com.github.tcsizmadia.opensearchsandbox.components;

import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.core5.http.HttpHost;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

@Configuration
public class ApplicationConfiguration {


    @Value("${opensearch.transport.host:localhost}")
    private String host;

    @Value("${opensearch.transport.port:9200}")
    private int port;

    @Value("${opensearch.transport.scheme:https}")
    private String scheme;

    @Value("${opensearch.username:admin}")
    private String username;

    @Value("${opensearch.password:admin}")
    private String password;

    @Value("${opensearch.skipSslVerification:false}")
    private boolean skipSslVerification;

    private SSLContext getTrustAllSslContext() {
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, this.getAllTrustTrustManager(), new SecureRandom());
            return sslContext;
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }

    private TrustManager[] getAllTrustTrustManager() {
        return new TrustManager[]{
            new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] x509Certificates, String s)
                    throws CertificateException {
                    // We trust all clients -- never do this in production
                }

                @Override
                public void checkServerTrusted(X509Certificate[] x509Certificates, String s)
                    throws CertificateException {
                    // We trust all servers -- never do this in production
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }
        };
    }

    @Bean
    public OpenSearchClient openSearchClient() {
        final var httpHost =
            new HttpHost(
                this.scheme,
                this.host,
                this.port
            );

        final var credentialsProvider = new BasicCredentialsProvider();

        credentialsProvider.setCredentials(
            new AuthScope(httpHost),
            new UsernamePasswordCredentials(this.username,
            this.password.toCharArray())
        );

        if (this.skipSslVerification) {
            SSLContext sslContext = this.getTrustAllSslContext();
            SSLContext.setDefault(sslContext);
        }

        final var builder = ApacheHttpClient5TransportBuilder.builder(httpHost);

        builder.setHttpClientConfigCallback(httpClientBuilder -> {
            httpClientBuilder.disableAuthCaching();
            return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
        });

        return new OpenSearchClient(builder.build());
    }
}
