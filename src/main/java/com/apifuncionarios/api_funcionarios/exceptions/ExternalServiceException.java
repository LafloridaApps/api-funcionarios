package com.apifuncionarios.api_funcionarios.exceptions;

public class ExternalServiceException extends FuncionarioException {
    private final int upstreamStatus;

    public ExternalServiceException(int upstreamStatus, Throwable cause) {
        super("No se pudo completar la consulta al servicio externo");
        this.upstreamStatus = upstreamStatus;
        if (cause != null) initCause(cause);
    }

    public int getUpstreamStatus() { return upstreamStatus; }
}
