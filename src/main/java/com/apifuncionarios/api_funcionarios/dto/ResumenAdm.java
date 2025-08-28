package com.apifuncionarios.api_funcionarios.dto;

import java.util.List;

public class ResumenAdm {

    private Double maximo;
    private Double usados;
    private Double saldo;
    private Integer anio;
    private List<DetalleAdm> detalle;

    public Double getMaximo() {
        return maximo;
    }

    public void setMaximo(Double maximo) {
        this.maximo = maximo;
    }

    public Double getUsados() {
        return usados;
    }

    public void setUsados(Double usados) {
        this.usados = usados;
    }

    public Double getSaldo() {
        return saldo;
    }

    public void setSaldo(Double saldo) {
        this.saldo = saldo;
    }

    public Integer getAnio() {
        return anio;
    }

    public void setAnio(Integer anio) {
        this.anio = anio;
    }

    public List<DetalleAdm> getDetalle() {
        return detalle;
    }

    public void setDetalle(List<DetalleAdm> detalle) {
        this.detalle = detalle;
    }

}
