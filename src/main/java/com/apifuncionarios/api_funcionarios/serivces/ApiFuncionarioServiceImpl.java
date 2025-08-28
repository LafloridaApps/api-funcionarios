package com.apifuncionarios.api_funcionarios.serivces;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.apifuncionarios.api_funcionarios.config.ApiProperties;
import com.apifuncionarios.api_funcionarios.dto.ApiFuncionarioResponse;
import com.apifuncionarios.api_funcionarios.dto.ResumenAdm;
import com.apifuncionarios.api_funcionarios.dto.ResumenFeriadoLegal;
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

    @Override
    public ResumenFeriadoLegal obtenerFeriadosLegales(Integer rut, Integer ident) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/funcionario/feriados")
                        .queryParam("rut", rut)
                        .queryParam("ident", ident)
                        .build())

                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> Mono.empty())
                .bodyToMono(ResumenFeriadoLegal.class)

                .onErrorResume(Exception.class, e -> Mono.empty())
                .block();
    }

    @Override
    public ResumenAdm obtenerAdministrativos(Integer rut, Integer ident) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/funcionario/administrativos")
                        .queryParam("rut", rut)
                        .queryParam("ident", ident)
                        .build())

                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> Mono.empty())
                .bodyToMono(ResumenAdm.class)

                .onErrorResume(Exception.class, e -> Mono.empty())
                .block();
    }

}
