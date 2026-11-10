package com.apifuncionarios.api_funcionarios.services;

import java.net.URI;
import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.apifuncionarios.api_funcionarios.config.ApiProperties;
import com.apifuncionarios.api_funcionarios.dto.ApiFuncionarioResponse;
import com.apifuncionarios.api_funcionarios.dto.ResumenAdm;
import com.apifuncionarios.api_funcionarios.dto.ResumenFeriadoLegal;
import com.apifuncionarios.api_funcionarios.services.interfaces.ApiFuncionarioService;

import io.netty.channel.ChannelOption;
import reactor.netty.http.client.HttpClient;

@Service
public class ApiFuncionarioServiceImpl implements ApiFuncionarioService {

    private static final Logger logger = LoggerFactory.getLogger(ApiFuncionarioServiceImpl.class);

    private final String baseUrl;
    private final WebClient webClient;

    public ApiFuncionarioServiceImpl(WebClient.Builder webClientBuilder, ApiProperties apiProperties) {
        this.baseUrl = apiProperties.getFuncionarioSmcUrl();

        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(5))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);

        this.webClient = webClientBuilder
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(baseUrl)
                .build();
        logger.info("event=external_client_configured service=funcionarios");
    }

    private URI buildUri(String path, Object... uriVars) {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .path(path)
                .buildAndExpand(uriVars)
                .toUri();
    }

    private URI buildUriWithQuery(String path, Object... queryParams) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl).path(path);
        for (int i = 0; i < queryParams.length; i += 2) {
            builder.queryParam((String) queryParams[i], queryParams[i + 1]);
        }
        return builder.build().toUri();
    }

    @Override
    public ApiFuncionarioResponse obtenerDetalleColaborador(Integer rut) {
        return consultar("detalle", buildUri("/api/funcionario/{rut}", rut),
                new org.springframework.core.ParameterizedTypeReference<ApiFuncionarioResponse>() {});
    }

    @Override
    public ResumenFeriadoLegal obtenerFeriadosLegales(Integer rut, Integer ident) {
        return consultar("feriados", buildUriWithQuery("/api/funcionario/feriados", "rut", rut, "ident", ident),
                new org.springframework.core.ParameterizedTypeReference<ResumenFeriadoLegal>() {});
    }

    @Override
    public ResumenAdm obtenerAdministrativos(Integer rut, Integer ident) {
        return consultar("administrativos", buildUriWithQuery("/api/funcionario/administrativos", "rut", rut, "ident", ident),
                new org.springframework.core.ParameterizedTypeReference<ResumenAdm>() {});
    }

    @Override
    public List<ApiFuncionarioResponse> obtenerListDetalleColaborador(Integer rut) {
        return consultar("lista", buildUri("/api/funcionario/{rut}/list", rut),
                new org.springframework.core.ParameterizedTypeReference<List<ApiFuncionarioResponse>>() {});
    }

    private <T> T consultar(String operation, URI uri, org.springframework.core.ParameterizedTypeReference<T> type) {
        return com.apifuncionarios.api_funcionarios.utils.ExternalApiClient.get(
                "funcionarios", operation, () -> webClient.get().uri(uri), type);
    }
}
