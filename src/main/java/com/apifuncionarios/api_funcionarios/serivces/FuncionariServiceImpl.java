package com.apifuncionarios.api_funcionarios.serivces;

import org.springframework.stereotype.Service;

import com.apifuncionarios.api_funcionarios.dto.ApiFuncionarioResponse;
import com.apifuncionarios.api_funcionarios.dto.DepartamentoResponse;
import com.apifuncionarios.api_funcionarios.dto.FuncionarioResponse;
import com.apifuncionarios.api_funcionarios.entities.Funcionario;
import com.apifuncionarios.api_funcionarios.exceptions.FuncionarioException;
import com.apifuncionarios.api_funcionarios.repositories.FuncionarioRepository;
import com.apifuncionarios.api_funcionarios.serivces.interfaces.ApiDepartamentoService;
import com.apifuncionarios.api_funcionarios.serivces.interfaces.ApiFuncionarioService;
import com.apifuncionarios.api_funcionarios.serivces.interfaces.FuncionarioService;
import com.apifuncionarios.api_funcionarios.utils.RepositoryUtils;

@Service
public class FuncionariServiceImpl implements FuncionarioService {

    private final ApiFuncionarioService apiFuncionarioService;

    private final FuncionarioRepository funcionarioRepository;

    private final ApiDepartamentoService apiDepartamentoService;

    public FuncionariServiceImpl(ApiFuncionarioService apiFuncionarioService,
            FuncionarioRepository funcionarioRepository,ApiDepartamentoService apiDepartamentoService) {
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

        Funcionario funcionario = funcionarioRepository.findByRut(rut).orElseGet(() -> createFuncionario(response));

        DepartamentoResponse depto = apiDepartamentoService.obtenerDetalleDepartamentoByCodigoEx(response.getCodDeptoExt());

        if(funcionario.getIdDepto() == null && depto != null) {
            funcionario.setIdDepto(depto.getId());
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
        funcionario.setNombres(request.getNombres().concat(" ").concat(request.getPaterno()));
        funcionario.setEmail(request.getEmail());
        funcionario.setVrut(request.getVrut().charAt(0));

        return funcionarioRepository.save(funcionario);

    }

    

    private FuncionarioResponse getFuncionarioByRut(Integer rut) {

        Funcionario funcionario = RepositoryUtils.findOrThrow(funcionarioRepository.findByRut(rut),
                String.format("No se encontro el funcionario con el rut %d", rut));

        DepartamentoResponse depto = apiDepartamentoService.obtenerDetalleDepartamentoById(funcionario.getIdDepto());

        return new FuncionarioResponse.Builder()
                .nombre(funcionario.getNombres())
                .rut(funcionario.getRut())
                .vrut(Character.toString(funcionario.getVrut()))
                .codDepto(funcionario.getIdDepto())
                .departamento(depto.getNombre())
                .email(funcionario.getEmail())
                .build();

    }

}

