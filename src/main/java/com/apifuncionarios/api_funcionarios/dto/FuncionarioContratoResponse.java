package com.apifuncionarios.api_funcionarios.dto;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Contrato del listado; no modifica la respuesta individual del funcionario. */
public record FuncionarioContratoResponse(Integer rut, String vrut, String paterno, String materno, String email,
        String codDeptoExt, String nombres, String foto, String departamento, Integer ident,
        String tipoContrato, String escalafon, Integer grado, LocalDate fechainicioContrato,
        LocalDate fechafinContrato, Boolean vigente, LocalDate fechaNacimiento, String jefe,
        Long codDepto, Long codDeptoJefe,
        @JsonProperty("numresolcontr") String numResolContr,
        @JsonProperty("obs_adicional_cont") String obsAdicionalCont,
        @JsonProperty("lincontrato") Integer linContrato,
        @JsonProperty("fecharesolcontr") LocalDate fechaResolContr,
        @JsonProperty("denominacion_dominio") String denominacionDominio) {

    public FuncionarioContratoResponse conDepartamento(Long codDepto, String jefe, Long codDeptoJefe) {
        return new FuncionarioContratoResponse(rut, vrut, paterno, materno, email, codDeptoExt, nombres,
                foto, departamento, ident, tipoContrato, escalafon, grado, fechainicioContrato,
                fechafinContrato, vigente, fechaNacimiento, jefe, codDepto, codDeptoJefe,
                numResolContr, obsAdicionalCont, linContrato, fechaResolContr, denominacionDominio);
    }
}
