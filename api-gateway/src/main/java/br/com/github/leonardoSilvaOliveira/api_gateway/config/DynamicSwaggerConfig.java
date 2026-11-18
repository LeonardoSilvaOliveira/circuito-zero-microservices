package br.com.github.leonardoSilvaOliveira.api_gateway.config;

import org.springdoc.core.properties.AbstractSwaggerUiConfigProperties.SwaggerUrl;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Swagger UI unificado no API Gateway.
 *
 * Consulta o Eureka e registra no seletor do Swagger UI a documentação
 * (/{servico}/v3/api-docs) de cada microsserviço registrado, exceto o próprio gateway.
 * A lista é atualizada a cada 10 s, então serviços que sobem depois também aparecem.
 */
@Component
@EnableScheduling
public class DynamicSwaggerConfig {

    private final DiscoveryClient discoveryClient;
    private final SwaggerUiConfigProperties swaggerUiConfigProperties;

    public DynamicSwaggerConfig(DiscoveryClient discoveryClient,
                                SwaggerUiConfigProperties swaggerUiConfigProperties) {
        this.discoveryClient = discoveryClient;
        this.swaggerUiConfigProperties = swaggerUiConfigProperties;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        atualizarUrls();
    }

    @Scheduled(initialDelay = 10000, fixedDelay = 10000)
    public void atualizarUrls() {
        Set<SwaggerUrl> urls = new HashSet<>();

        for (String service : discoveryClient.getServices()) {
            if (!service.equalsIgnoreCase("api-gateway")) {
                urls.add(new SwaggerUrl(service, "/" + service + "/v3/api-docs", service));
            }
        }

        swaggerUiConfigProperties.setUrl(null);
        swaggerUiConfigProperties.setUrls(urls);
    }
}
