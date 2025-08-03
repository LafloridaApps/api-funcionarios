package com.apifuncionarios.api_funcionarios.dto;

public class DepartamentoResponse {

    private Long id;
    private String nombre;
    private Integer rutJefe;
    private Integer rutJefeSuperior;
    private Long idDeptoSuperior;

    public Integer getRutJefeSuperior() {
        return rutJefeSuperior;
    }

    public void setRutJefeSuperior(Integer rutJefeSuperior) {
        this.rutJefeSuperior = rutJefeSuperior;
    }

    public Long getIdDeptoSuperior() {
        return idDeptoSuperior;
    }

    public void setIdDeptoSuperior(Long idDeptoSuperior) {
        this.idDeptoSuperior = idDeptoSuperior;
    }

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
