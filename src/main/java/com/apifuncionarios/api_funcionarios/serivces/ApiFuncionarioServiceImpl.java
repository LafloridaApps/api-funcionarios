package com.apifuncionarios.api_funcionarios.serivces;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.apifuncionarios.api_funcionarios.config.ApiProperties;
import com.apifuncionarios.api_funcionarios.dto.ApiFuncionarioResponse;
import com.apifuncionarios.api_funcionarios.serivces.interfaces.ApiFuncionarioService;

import reactor.core.publisher.Mono;

@Service
public class ApiFuncionarioServiceImpl implements ApiFuncionarioService {

    private final WebClient webClient;

    public ApiFuncionarioServiceImpl(WebClient.Builder webClientBuilder, ApiProperties apiProperties) {
        this.webClient = webClientBuilder.baseUrl(apiProperties.getFuncionarioSmcUrl()).build();
    }

    @Override
    public ApiFuncionarioResponse obtenerDetalleColaborador(Integer rut) {
        return webClient.get()
                .uri("/api/funcionario/{rut}", rut)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> Mono.empty())
                .bodyToMono(ApiFuncionarioResponse.class)
               
                .onErrorResume(Exception.class, e -> Mono.empty())
                .block();
    }

}
