package com.apifuncionarios.api_funcionarios.config;
import org.springframework.stereotype.Component;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Component
@ConfigurationProperties(prefix = "api")
public class ApiProperties {


    private String funcionarioUrl;

    private String departamentoUrl;

    

    public String getFuncionarioUrl() {
        return funcionarioUrl;
    }

    public void setFuncionarioUrl(String funcionarioUrl) {
        this.funcionarioUrl = funcionarioUrl;
    }

    public String getDepartamentoUrl() {
        return departamentoUrl;
    }

    public void setDepartamentoUrl(String departamentoUrl) {
        this.departamentoUrl = departamentoUrl;
    }

    


    

}