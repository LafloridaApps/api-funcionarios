
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
        funcionario.setVrut(dto.getVrut().charAt(0));
        funcionario.setNombres(dto.getNombres());
        funcionario.setApellidoPaterno(dto.getPaterno());
        funcionario.setApellidoMaterno(dto.getMaterno());
        funcionario.setEmail(dto.getEmail());
        funcionario.setTipoContrato(dto.getTipoContrato());
        funcionario.setIdent(dto.getIdent());
        return funcionario;
    }

    public void updateEntityFromDto(Funcionario entity, ApiFuncionarioResponse dto, DepartamentoResponse depto) {
        if (dto == null || entity == null || depto == null) {
            return;
        }
        entity.setTipoContrato(dto.getTipoContrato());
        entity.setIdent(dto.getIdent());
        entity.setIdDepto(depto.getId());
    }

    public FuncionarioResponse toResponseDto(Funcionario entity, DepartamentoResponse depto, Funcionario jefe, String foto) {
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
                .tipoContrato(entity.getTipoContrato())
                .foto(foto)
                .ident(entity.getIdent())
                .build();
    }
}
