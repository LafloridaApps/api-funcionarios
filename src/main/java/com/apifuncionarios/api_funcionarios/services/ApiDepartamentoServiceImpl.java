package com.apifuncionarios.api_funcionarios.services;

import java.time.Duration;

import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.apifuncionarios.api_funcionarios.config.ApiProperties;
import com.apifuncionarios.api_funcionarios.dto.DepartamentoResponse;
import com.apifuncionarios.api_funcionarios.services.interfaces.ApiDepartamentoService;

import io.netty.channel.ChannelOption;
import reactor.netty.http.client.HttpClient;

@Service
public class ApiDepartamentoServiceImpl implements ApiDepartamentoService {

    private final WebClient webClient;

    public ApiDepartamentoServiceImpl(WebClient.Builder webClientBuilder, ApiProperties apiProperties) {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(10))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
                
        this.webClient = webClientBuilder
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(apiProperties.getDepartamentoUrl())
                .build();
    }

    @Override
    public DepartamentoResponse obtenerDetalleDepartamentoByCodigoEx(String codex) {
        return com.apifuncionarios.api_funcionarios.utils.ExternalApiClient.get("departamentos", "por_codigo",
                () -> webClient.get().uri(builder -> builder.path("/api/departamentos/codex")
                        .queryParam("codex", codex).build()),
                new org.springframework.core.ParameterizedTypeReference<DepartamentoResponse>() {});
    }

    @Override
    public DepartamentoResponse obtenerDetalleDepartamentoById(Long id) {
        return com.apifuncionarios.api_funcionarios.utils.ExternalApiClient.get("departamentos", "por_id",
                () -> webClient.get().uri(builder -> builder.path("/api/departamentos")
                        .queryParam("id", id).build()),
                new org.springframework.core.ParameterizedTypeReference<DepartamentoResponse>() {});
    }
}
