package com.apifuncionarios.api_funcionarios.dto;

import java.time.LocalDate;

public class DetalleAdm {

    private Integer numero;
    private String resolucion;
    private LocalDate fechaResolucion;
    private LocalDate fechaInicio;
    private LocalDate fechaTermino;
    private double periodo;

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public String getResolucion() {
        return resolucion;
    }

    public void setResolucion(String resolucion) {
        this.resolucion = resolucion;
    }

    public LocalDate getFechaResolucion() {
        return fechaResolucion;
    }

    public void setFechaResolucion(LocalDate fechaResolucion) {
        this.fechaResolucion = fechaResolucion;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaTermino() {
        return fechaTermino;
    }

    public void setFechaTermino(LocalDate fechaTermino) {
        this.fechaTermino = fechaTermino;
    }

    public double getPeriodo() {
        return periodo;
    }

    public void setPeriodo(double periodo) {
        this.periodo = periodo;
    }

}
