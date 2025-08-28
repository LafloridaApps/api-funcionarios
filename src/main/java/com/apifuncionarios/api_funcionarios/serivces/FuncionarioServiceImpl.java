package com.apifuncionarios.api_funcionarios.serivces;

import org.springframework.stereotype.Service;

import com.apifuncionarios.api_funcionarios.dto.ApiFuncionarioResponse;
import com.apifuncionarios.api_funcionarios.dto.DepartamentoResponse;
import com.apifuncionarios.api_funcionarios.dto.FuncionarioResponse;
import com.apifuncionarios.api_funcionarios.dto.ResumenAdm;
import com.apifuncionarios.api_funcionarios.dto.ResumenFeriadoLegal;
import com.apifuncionarios.api_funcionarios.entities.Funcionario;
import com.apifuncionarios.api_funcionarios.exceptions.FuncionarioException;
import com.apifuncionarios.api_funcionarios.repositories.FuncionarioRepository;
import com.apifuncionarios.api_funcionarios.serivces.interfaces.ApiDepartamentoService;
import com.apifuncionarios.api_funcionarios.serivces.interfaces.ApiFuncionarioService;
import com.apifuncionarios.api_funcionarios.serivces.interfaces.FuncionarioService;
import com.apifuncionarios.api_funcionarios.utils.RepositoryUtils;

@Service
public class FuncionarioServiceImpl implements FuncionarioService {

    private final ApiFuncionarioService apiFuncionarioService;
    private final FuncionarioRepository funcionarioRepository;
    private final ApiDepartamentoService apiDepartamentoService;

    private static final String ERROR_MESSAGE = "No se encontro el funcionario en la base de datos municipalidad %d";

    public FuncionarioServiceImpl(ApiFuncionarioService apiFuncionarioService,
            FuncionarioRepository funcionarioRepository,
            ApiDepartamentoService apiDepartamentoService) {
        this.apiFuncionarioService = apiFuncionarioService;
        this.funcionarioRepository = funcionarioRepository;
        this.apiDepartamentoService = apiDepartamentoService;
    }

    @Override
    public FuncionarioResponse getFuncionarioInfo(Integer rut) {

        ApiFuncionarioResponse response = apiFuncionarioService.obtenerDetalleColaborador(rut);

        if (response == null) {
            throw new FuncionarioException("No se encontro el funcionario en la base de datos municipalidad");
        }

        Funcionario funcionario = funcionarioRepository.findByRut(rut)
                .orElseGet(() -> createFuncionario(response));

        DepartamentoResponse depto = apiDepartamentoService
                .obtenerDetalleDepartamentoByCodigoEx(response.getCodDeptoExt());

        if (depto == null) {
            throw new FuncionarioException("No se encontro el departamento en la base de datos municipalidad");
        }

        boolean updated = false;

        if (funcionario.getIdDepto() == null) {
            funcionario.setIdDepto(depto.getId());
            updated = true;
        }

        if (funcionario.getIdent() != response.getIdent() && response.getIdent() != 0) {
            funcionario.setIdent(response.getIdent());
            updated = true;
        }

        if (updated) {
            funcionarioRepository.save(funcionario);
        }

        FuncionarioResponse funcionarioResponse = getFuncionarioByRut(funcionario.getRut());
        funcionarioResponse.setFoto(response.getFoto());
        funcionarioResponse.setIdent(response.getIdent());

        return funcionarioResponse;
    }

    private Funcionario createFuncionario(ApiFuncionarioResponse request) {
        Funcionario funcionario = new Funcionario();

        funcionario.setRut(request.getRut());
        funcionario.setNombres(request.getNombres());
        funcionario.setApellidoPaterno(request.getPaterno());
        funcionario.setApellidoMaterno(request.getMaterno());
        funcionario.setEmail(request.getEmail());
        funcionario.setVrut(request.getVrut().charAt(0));

        return funcionarioRepository.save(funcionario);
    }

    private FuncionarioResponse getFuncionarioByRut(Integer rut) {
        Funcionario funcionario = RepositoryUtils.findOrThrow(
                funcionarioRepository.findByRut(rut),
                String.format(ERROR_MESSAGE, rut));

        DepartamentoResponse depto = apiDepartamentoService
                .obtenerDetalleDepartamentoById(funcionario.getIdDepto());

        Funcionario jefeDepto = new Funcionario();

        if (depto.getRutJefe() != null) {
            jefeDepto = RepositoryUtils.findOrThrow(
                    funcionarioRepository.findByRut(depto.getRutJefe()),
                    String.format(ERROR_MESSAGE, rut));
        }

        if (depto.getRutJefe().equals(funcionario.getRut())) {
            jefeDepto = RepositoryUtils.findOrThrow(
                    funcionarioRepository.findByRut(depto.getRutJefeSuperior()),
                    String.format(ERROR_MESSAGE, rut));
        }

        return new FuncionarioResponse.Builder()
                .nombre(funcionario.getNombres())
                .apellidoPaterno(funcionario.getApellidoPaterno())
                .apellidoMaterno(funcionario.getApellidoMaterno())
                .rut(funcionario.getRut())
                .vrut(Character.toString(funcionario.getVrut()))
                .codDepto(funcionario.getIdDepto())
                .departamento(depto.getNombre())
                .nombreJefe(jefeDepto.getRut() != null ? jefeDepto.getNombreCompleto() : "")
                .codDeptoJefe(jefeDepto.getIdDepto())
                .email(funcionario.getEmail())
                .build();
    }

    @Override
    public ResumenFeriadoLegal resumenFeriadosLegales(Integer rut, Integer ident) {
        return apiFuncionarioService.obtenerFeriadosLegales(rut, ident);
    }

    @Override
    public ResumenAdm resumenAdm(Integer rut, Integer ident) {
        return apiFuncionarioService.obtenerAdministrativos(rut, ident);
    }
}
