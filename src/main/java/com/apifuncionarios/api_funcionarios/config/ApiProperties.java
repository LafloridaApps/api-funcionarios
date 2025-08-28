package com.apifuncionarios.api_funcionarios.config;

import org.springframework.stereotype.Component;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Component
@ConfigurationProperties(prefix = "api")
public class ApiProperties {

    private String funcionarioSmcUrl;

    private String departamentoUrl;

    public String getDepartamentoUrl() {
        return departamentoUrl;
    }

    public void setDepartamentoUrl(String departamentoUrl) {
        this.departamentoUrl = departamentoUrl;
    }

    public String getFuncionarioSmcUrl() {
        return funcionarioSmcUrl;
    }

    public void setFuncionarioSmcUrl(String funcionarioSmcUrl) {
        this.funcionarioSmcUrl = funcionarioSmcUrl;
    }

}