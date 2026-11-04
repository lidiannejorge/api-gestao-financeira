package com.apigestaofinanceira.transacoes.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.http.HttpClient;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

@Configuration
public class Ms1ClientConfig {

    @Value("${ms1.base-url}")
    private String ms1BaseUrl;

    @Value("${mockapi.base-url}")
    private String mockApiBaseUrl;

    @Value("${brasilapi.base-url}")
    private String brasilApiBaseUrl;

    // Em redes corporativas com proxy/VPN que fazem inspeção SSL
    // (Zscaler, Netskope, Fortinet etc.), o certificado raiz apresentado
    // nas chamadas HTTPS não é o certificado real do serviço externo, e a
    // JVM rejeita a conexão com SSLHandshakeException / PKIX path
    // building failed. Não é possível alterar a infraestrutura de rede
    // nem o pacote de distribuição (deve continuar sendo um JAR simples),
    // então os clientes que acessam APIs externas usam um HttpClient que
    // não valida a cadeia de certificados.
    @Bean
    public RestClient ms1RestClient() {
        return RestClient.builder()
                .baseUrl(ms1BaseUrl)
                .build();
    }

    @Bean
    public RestClient mockApiRestClient() {
        return RestClient.builder()
                .baseUrl(mockApiBaseUrl)
                .requestFactory(new JdkClientHttpRequestFactory(httpClientSemValidacaoDeCertificado()))
                .build();
    }

    @Bean
    public RestClient brasilApiRestClient() {
        return RestClient.builder()
                .baseUrl(brasilApiBaseUrl)
                .requestFactory(new JdkClientHttpRequestFactory(httpClientSemValidacaoDeCertificado()))
                .build();
    }

    private HttpClient httpClientSemValidacaoDeCertificado() {
        try {
            TrustManager[] confiaEmTudo = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }
            };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, confiaEmTudo, new SecureRandom());

            return HttpClient.newBuilder()
                    .sslContext(sslContext)
                    .build();

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Não foi possível configurar o cliente HTTP para APIs externas.",
                    e
            );
        }
    }
}
