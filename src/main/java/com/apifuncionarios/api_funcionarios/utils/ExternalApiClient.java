package com.apifuncionarios.api_funcionarios.utils;

import java.time.Duration;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import com.apifuncionarios.api_funcionarios.exceptions.ExternalServiceException;

/** Logs de llamadas externas sin URL, query string ni cuerpos con datos personales. */
public final class ExternalApiClient {
    private static final Logger logger = LoggerFactory.getLogger(ExternalApiClient.class);
    private ExternalApiClient() { }

    public static <T> T get(String service, String operation,
            Supplier<WebClient.RequestHeadersSpec<?>> request, ParameterizedTypeReference<T> type) {
        long start = System.nanoTime();
        int[] status = {0};
        try {
            T result = request.get().exchangeToMono(response -> {
                status[0] = response.statusCode().value();
                if (status[0] == 404 || status[0] == 204) {
                    return response.releaseBody().then(Mono.<T>empty());
                }
                if (!response.statusCode().is2xxSuccessful()) {
                    return response.releaseBody().then(Mono.<T>error(new ExternalServiceException(status[0], null)));
                }
                return response.bodyToMono(type);
            }).timeout(Duration.ofSeconds(15)).block();
            logger.debug("event=external_completed service={} operation={} status={} result={} duration_ms={}",
                    service, operation, status[0], result == null ? "empty" : "ok", elapsed(start));
            return result;
        } catch (RuntimeException error) {
            logger.warn("event=external_failed service={} operation={} status={} error_type={} duration_ms={}",
                    service, operation, status[0], rootType(error), elapsed(start));
            if (error instanceof ExternalServiceException external) throw external;
            throw new ExternalServiceException(status[0], error);
        }
    }

    private static String rootType(Throwable error) {
        Throwable root = error;
        while (root.getCause() != null && root.getCause() != root) root = root.getCause();
        return root.getClass().getSimpleName();
    }

    private static long elapsed(long start) { return (System.nanoTime() - start) / 1_000_000; }
}
