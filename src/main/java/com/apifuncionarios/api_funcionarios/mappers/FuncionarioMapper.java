
package com.apifuncionarios.api_funcionarios.mappers;

import org.springframework.stereotype.Component;

import com.apifuncionarios.api_funcionarios.dto.ApiFuncionarioResponse;
import com.apifuncionarios.api_funcionarios.dto.DepartamentoResponse;
import com.apifuncionarios.api_funcionarios.dto.FuncionarioResponse;
import com.apifuncionarios.api_funcionarios.entities.Funcionario;

@Component
public class FuncionarioMapper {

    public Funcionario toEntity(ApiFuncionarioResponse dto) {
        if (dto == null) {
            return null;
        }
        Funcionario funcionario = new Funcionario();
        funcionario.setRut(dto.getRut());
        funcionario.setVrut(dto.getVrut().trim().toUpperCase(java.util.Locale.ROOT).charAt(0));
        funcionario.setNombres(dto.getNombres());
        funcionario.setApellidoPaterno(dto.getPaterno());
        funcionario.setApellidoMaterno(dto.getMaterno());
        funcionario.setEmail(dto.getEmail());
        funcionario.setTipoContrato(trim(dto.getTipoContrato()));
        funcionario.setIdent(dto.getIdent());
        funcionario.setEscalafon(trim(dto.getEscalafon()));
        funcionario.setGrado(dto.getGrado());
        funcionario.setVigente(dto.isVigente());
        funcionario.setFechaNacimiento(dto.getFechaNacimiento());
        funcionario.setFechafinContrato(dto.getFechafinContrato());
        funcionario.setFechaInicioContrato(dto.getFechainicioContrato());
        return funcionario;
    }

    public void updateEntityFromDto(Funcionario entity, ApiFuncionarioResponse dto, DepartamentoResponse depto) {
        if (dto == null || entity == null) {
            return;
        }
        entity.setVrut(dto.getVrut().trim().toUpperCase(java.util.Locale.ROOT).charAt(0));
        if (dto.contieneCampo("nombres")) {
            entity.setNombres(dto.getNombres());
        }
        if (dto.contieneCampo("paterno")) {
            entity.setApellidoPaterno(dto.getPaterno());
        }
        if (dto.contieneCampo("materno")) {
            entity.setApellidoMaterno(dto.getMaterno());
        }
        if (dto.contieneCampo("email")) {
            entity.setEmail(dto.getEmail());
        }
        if (dto.contieneCampo("tipoContrato")) {
            entity.setTipoContrato(trim(dto.getTipoContrato()));
        }
        if (dto.contieneCampo("ident")) {
            entity.setIdent(dto.getIdent());
        }
        actualizarDepartamento(entity, depto);
        if (dto.contieneCampo("escalafon")) {
            entity.setEscalafon(trim(dto.getEscalafon()));
        }
        if (dto.contieneCampo("grado")) {
            entity.setGrado(dto.getGrado());
        }
        if (dto.contieneCampo("fechaNacimiento")) {
            entity.setFechaNacimiento(dto.getFechaNacimiento());
        }
        if (dto.contieneCampo("vigente")) {
            entity.setVigente(dto.isVigente());
        }
        if (dto.contieneCampo("fechafinContrato")) {
            entity.setFechafinContrato(dto.getFechafinContrato());
        }
        if (dto.contieneCampo("fechainicioContrato")) {
            entity.setFechaInicioContrato(dto.getFechainicioContrato());
        }
    }

    private void actualizarDepartamento(Funcionario entity, DepartamentoResponse depto) {
        if (depto != null && depto.getId() != null && depto.getId() > 0) {
            entity.setIdDepto(depto.getId());
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    public FuncionarioResponse toResponseDto(Funcionario entity, DepartamentoResponse depto, Funcionario jefe,
            String foto) {
        if (entity == null) {
            return null;
        }

        String nombreJefe = (jefe != null && jefe.getRut() != null) ? jefe.getNombreCompleto() : "";
        Long codDeptoJefe = (jefe != null) ? jefe.getIdDepto() : null;
        String nombreDepto = (depto != null) ? depto.getNombre() : "";

        return new FuncionarioResponse.Builder()
                .nombre(entity.getNombres())
                .apellidoPaterno(entity.getApellidoPaterno())
                .apellidoMaterno(entity.getApellidoMaterno())
                .rut(entity.getRut())
                .vrut(Character.toString(entity.getVrut()))
                .codDepto(entity.getIdDepto())
                .departamento(nombreDepto)
                .nombreJefe(nombreJefe)
                .codDeptoJefe(codDeptoJefe)
                .email(entity.getEmail())
                .tipoContrato(entity.getTipoContrato().trim())
                .foto(foto)
                .ident(entity.getIdent())
                .escalafon(entity.getEscalafon().trim())
                .grado(entity.getGrado())
                .fechaNacimiento(entity.getFechaNacimiento())
                .vigente(entity.isVigente())
                .fechaInicioContrato(entity.getFechaInicioContrato())
                .fechaFinContrato(entity.getFechafinContrato())
                .build();
    }
}
