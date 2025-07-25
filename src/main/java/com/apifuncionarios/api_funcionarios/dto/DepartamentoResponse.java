package com.apifuncionarios.api_funcionarios.dto;

public class DepartamentoResponse {

    private Long id;
    private String nombre;
    private Integer rutJefe;

    public DepartamentoResponse(Long id, String nombre, Integer rutJefe) {
        this.id = id;
        this.nombre = nombre;
        this.rutJefe = rutJefe;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getRutJefe() {
        return rutJefe;
    }

    public void setRutJefe(Integer rutJefe) {
        this.rutJefe = rutJefe;
    }

}
