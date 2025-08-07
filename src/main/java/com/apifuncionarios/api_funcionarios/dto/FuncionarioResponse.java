package com.apifuncionarios.api_funcionarios.dto;


public class FuncionarioResponse {

    private Integer rut;
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String vrut;
    private String email;
    private String departamento;
    private Long codDepto;
    private String nombreJefe;
    private Long codDeptoJefe;
    private String foto;
    private Integer ident;

    private FuncionarioResponse(Builder builder) {
        this.rut = builder.rut;
        this.nombre = builder.nombre;
        this.vrut = builder.vrut;
        this.email = builder.email;
        this.departamento = builder.departamento;
        this.codDepto = builder.codDepto;
        this.nombreJefe = builder.nombreJefe;
        this.codDeptoJefe = builder.codDeptoJefe;
        this.foto=builder.foto;
        this.ident = builder.ident;
        this.apellidoMaterno = builder.apellidoMaterno;
        this.apellidoPaterno = builder.apellidoPaterno;
    }

   

    public static class Builder {
        private Integer rut;
        private String nombre;
         private String apellidoPaterno;
    private String apellidoMaterno;
        private String vrut;
        private String email;
        private String departamento;
        private Long codDepto;
        private String nombreJefe;
        private Long codDeptoJefe;
        private String foto;
        private Integer ident;

        public Builder rut(Integer rut) {
            this.rut = rut;
            return this;
        }

        public Builder nombre(String nombre) {
            this.nombre = nombre;
            return this;
        }

        public Builder vrut(String vrut) {
            this.vrut = vrut;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder departamento(String departamento) {
            this.departamento = departamento;
            return this;
        }

        public Builder codDepto(Long codDepto) {
            this.codDepto = codDepto;
            return this;
        }

        public Builder nombreJefe(String nombreJefe) {
            this.nombreJefe = nombreJefe;
            return this;
        }

        public Builder codDeptoJefe(Long codDeptoJefe) {
            this.codDeptoJefe = codDeptoJefe;
            return this;
        }

        public Builder foto(String foto) {
            this.foto = foto;
            return this;
        }   
        public Builder ident(Integer ident) {
            this.ident = ident;
            return this;
        }

        public FuncionarioResponse build() {
            return new FuncionarioResponse(this);
        }

        public Builder apellidoPaterno(String apellidoPaterno) {
            this.apellidoPaterno = apellidoPaterno;
            return this;
        }

        public Builder apellidoMaterno(String apellidoMaterno) {
            this.apellidoMaterno = apellidoMaterno;
            return this;
        }
    }

    public Integer getRut() {
        return rut;
    }

    public void setRut(Integer rut) {
        this.rut = rut;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getVrut() {
        return vrut;
    }

    public void setVrut(String vrut) {
        this.vrut = vrut;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public Long getCodDepto() {
        return codDepto;
    }

    public void setCodDepto(Long codDepto) {
        this.codDepto = codDepto;
    }

    public String getNombreJefe() {
        return this.nombreJefe;
    }

    public void setNombreJefe(String nombreJefe) {
        this.nombreJefe = nombreJefe;
    }

    public Long getCodDeptoJefe() {
        return this.codDeptoJefe;
    }

    public void setCodDeptoJefe(Long codDeptoJefe) {
        this.codDeptoJefe = codDeptoJefe;
    }

    public String getFoto() {
        return this.foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public Integer getIdent() {
        return this.ident;
    }

    public void setIdent(Integer ident) {
        this.ident = ident;
 
    }
    public String getApellidoPaterno() {
        return this.apellidoPaterno;
    }

    public void setApellidoPaterno(String apellidoPaterno) {
        this.apellidoPaterno = apellidoPaterno;
    }

    public String getApellidoMaterno() {
        return this.apellidoMaterno;
    }

    public void setApellidoMaterno(String apellidoMaterno) {
        this.apellidoMaterno = apellidoMaterno;
    }
}
