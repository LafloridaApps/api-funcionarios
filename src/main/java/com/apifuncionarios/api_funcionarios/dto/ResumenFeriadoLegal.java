package com.apifuncionarios.api_funcionarios.dto;

import java.util.List;

public class ResumenFeriadoLegal {

    private Integer anio;
    private int diasCorresponden;
    private int diasAcumulados;
    private int total;
    private int diasTomados;
    private int diasPerdidos;
    private int diasPendientes;
    private List<DetalleFeriadoLegal> detalle;

    public Integer getAnio() {
        return anio;
    }

    public void setAnio(Integer anio) {
        this.anio = anio;
    }

    public int getDiasCorresponden() {
        return diasCorresponden;
    }

    public void setDiasCorresponden(int diasCorresponden) {
        this.diasCorresponden = diasCorresponden;
    }

    public int getDiasAcumulados() {
        return diasAcumulados;
    }

    public void setDiasAcumulados(int diasAcumulados) {
        this.diasAcumulados = diasAcumulados;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getDiasTomados() {
        return diasTomados;
    }

    public void setDiasTomados(int diasTomados) {
        this.diasTomados = diasTomados;
    }

    public int getDiasPerdidos() {
        return diasPerdidos;
    }

    public void setDiasPerdidos(int diasPerdidos) {
        this.diasPerdidos = diasPerdidos;
    }

    public int getDiasPendientes() {
        return diasPendientes;
    }

    public void setDiasPendientes(int diasPendientes) {
        this.diasPendientes = diasPendientes;
    }

    public List<DetalleFeriadoLegal> getDetalle() {
        return detalle;
    }

    public void setDetalle(List<DetalleFeriadoLegal> detalle) {
        this.detalle = detalle;
    }

}
