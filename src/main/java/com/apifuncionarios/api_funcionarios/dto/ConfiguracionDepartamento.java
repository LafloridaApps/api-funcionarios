package com.apifuncionarios.api_funcionarios.dto;

/** Conserva el diagnóstico antes de ocultar un departamento no validado de la respuesta operativa. */
public record ConfiguracionDepartamento(String estado, String codigoExterno, String nombreExterno,
        Long idDeptoInterno, Long idDeptoAsociado) {
    public ConfiguracionDepartamento(String estado, String codigoExterno, String nombreExterno) {
        this(estado, codigoExterno, nombreExterno, null, null);
    }

    public boolean permiteSolicitudes() { return "VALIDADO".equals(estado); }
}
