package com.apifuncionarios.api_funcionarios.dto;

public record SearchnNombreDto(String nombreCompleto, Integer rut, Character vrut, Long codDepto) {
    public SearchnNombreDto(String nombreCompleto, Integer rut, Character vrut) {
        this(nombreCompleto, rut, vrut, null);
    }
}
