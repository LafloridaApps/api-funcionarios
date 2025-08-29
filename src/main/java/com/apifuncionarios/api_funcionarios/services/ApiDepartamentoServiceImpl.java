package com.apifuncionarios.api_funcionarios.services;

import java.time.Duration;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.apifuncionarios.api_funcionarios.config.ApiProperties;
import com.apifuncionarios.api_funcionarios.dto.DepartamentoResponse;
import com.apifuncionarios.api_funcionarios.services.interfaces.ApiDepartamentoService;

import io.netty.channel.ChannelOption;
import reactor.core.publisher.Mono;
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
        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/departamentos/codex")
                        .queryParam("codex", codex)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> Mono.empty())
                .bodyToMono(DepartamentoResponse.class)
                .onErrorResume(Exception.class, e -> Mono.empty())
                .block();
    }

    @Override
    public DepartamentoResponse obtenerDetalleDepartamentoById(Long id) {
       return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/departamentos")
                        .queryParam("id", id)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> Mono.empty())
                .bodyToMono(DepartamentoResponse.class)
                .onErrorResume(Exception.class, e -> Mono.empty())
                .block();
    }

}