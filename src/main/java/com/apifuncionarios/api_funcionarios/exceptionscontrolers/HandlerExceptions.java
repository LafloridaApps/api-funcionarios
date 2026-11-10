package com.apifuncionarios.api_funcionarios.exceptionscontrolers;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.apifuncionarios.api_funcionarios.dto.ErrorResponse;
import com.apifuncionarios.api_funcionarios.exceptions.FuncionarioException;
import com.apifuncionarios.api_funcionarios.exceptions.NotFounException;
import com.apifuncionarios.api_funcionarios.utils.FechaUtils;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ControllerAdvice
public class HandlerExceptions {

    private static final Logger logger = LoggerFactory.getLogger(HandlerExceptions.class);

    @ExceptionHandler(FuncionarioException.class)
    public ResponseEntity<Object> handlerFuncionarioException(FuncionarioException e, HttpServletRequest request) {

        logger.warn("event=request_rejected status=400 error_type={}", e.getClass().getSimpleName());
        ErrorResponse error = maptoErrorResponse(e, request, HttpStatus.BAD_REQUEST);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);

    }

    @ExceptionHandler(NotFounException.class)
    public ResponseEntity<Object> handlerNotFoundException(NotFounException e, HttpServletRequest request) {

        logger.debug("event=resource_not_found status=404");
        ErrorResponse error = maptoErrorResponse(e, request, HttpStatus.NOT_FOUND);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);

    }

    @ExceptionHandler(com.apifuncionarios.api_funcionarios.exceptions.ExternalServiceException.class)
    public ResponseEntity<Object> handlerExternalException(
            com.apifuncionarios.api_funcionarios.exceptions.ExternalServiceException e, HttpServletRequest request) {
        logger.warn("event=request_dependency_failed status=502 upstream_status={}", e.getUpstreamStatus());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(maptoErrorResponse(e, request, HttpStatus.BAD_GATEWAY));
    }

    @ExceptionHandler({org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,
            org.springframework.web.bind.MissingServletRequestParameterException.class})
    public ResponseEntity<Object> handlerInvalidParameters(Exception e, HttpServletRequest request) {
        logger.warn("event=request_rejected status=400 error_type={}", e.getClass().getSimpleName());
        return ResponseEntity.badRequest().body(maptoErrorResponse(
                new IllegalArgumentException("Parámetros de consulta inválidos o incompletos"), request, HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handlerUnexpectedException(Exception e, HttpServletRequest request) {
        if (e instanceof org.springframework.web.ErrorResponse frameworkError) {
            HttpStatus status = HttpStatus.valueOf(frameworkError.getStatusCode().value());
            logger.debug("event=request_rejected status={} error_type={}", status.value(), e.getClass().getSimpleName());
            return ResponseEntity.status(status).headers(frameworkError.getHeaders()).body(maptoErrorResponse(
                    new IllegalArgumentException(status.getReasonPhrase()), request, status));
        }
        logger.error("event=request_failed status=500 error_type={}", e.getClass().getSimpleName(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(maptoErrorResponse(
                new IllegalStateException("Error interno. Utilice X-Request-ID para reportar el incidente"),
                request, HttpStatus.INTERNAL_SERVER_ERROR));
    }

    private <T extends Exception> ErrorResponse maptoErrorResponse(T e, HttpServletRequest request, HttpStatus status) {

        return ErrorResponse.builder()
                .timestamp(FechaUtils.fechaActualHora())
                .status(status.value())
                .error(status.getReasonPhrase())
                .mensaje(e.getMessage())
                .ruta(com.apifuncionarios.api_funcionarios.config.RequestLoggingFilter.route(request))
                .build();

    }

}
