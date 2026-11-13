package com.apifuncionarios.api_funcionarios.dto;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Contrato del listado; no modifica la respuesta individual del funcionario. */
public record FuncionarioContratoResponse(Integer rut, String vrut, String paterno, String materno, String email,
        String codDeptoExt, String nombres, String foto, String departamento, Integer ident,
        String tipoContrato, String escalafon, Integer grado, LocalDate fechainicioContrato,
        LocalDate fechafinContrato, Boolean vigente, LocalDate fechaNacimiento, String jefe,
        @JsonProperty("numresolcontr") String numResolContr,
        @JsonProperty("obs_adicional_cont") String obsAdicionalCont) {
}
