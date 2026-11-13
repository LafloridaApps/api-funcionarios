package com.apifuncionarios.api_funcionarios.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.apifuncionarios.api_funcionarios.dto.ApiFuncionarioResponse;
import com.apifuncionarios.api_funcionarios.dto.DepartamentoResponse;
import com.apifuncionarios.api_funcionarios.dto.FuncionarioResponse;
import com.apifuncionarios.api_funcionarios.dto.ResumenAdm;
import com.apifuncionarios.api_funcionarios.dto.ResumenFeriadoLegal;
import com.apifuncionarios.api_funcionarios.entities.Funcionario;
import com.apifuncionarios.api_funcionarios.exceptions.FuncionarioException;
import com.apifuncionarios.api_funcionarios.exceptions.ExternalServiceException;
import com.apifuncionarios.api_funcionarios.exceptions.NotFounException;
import com.apifuncionarios.api_funcionarios.mappers.FuncionarioMapper;
import com.apifuncionarios.api_funcionarios.repositories.FuncionarioRepository;
import com.apifuncionarios.api_funcionarios.services.interfaces.ApiDepartamentoService;
import com.apifuncionarios.api_funcionarios.services.interfaces.ApiFuncionarioService;
import com.apifuncionarios.api_funcionarios.services.interfaces.FuncionarioService;
import com.apifuncionarios.api_funcionarios.services.sync.ISincronizacionService;
import com.apifuncionarios.api_funcionarios.utils.RepositoryUtils;

import java.util.Optional;

@Service
public class FuncionarioServiceImpl implements FuncionarioService {

    private final ApiFuncionarioService apiFuncionarioService;
    private final FuncionarioRepository funcionarioRepository;
    private final ApiDepartamentoService apiDepartamentoService;
    private final ISincronizacionService sincronizacionService;
    private final FuncionarioMapper funcionarioMapper;

    private static final Logger logger = LoggerFactory.getLogger(FuncionarioServiceImpl.class);

    public FuncionarioServiceImpl(ApiFuncionarioService apiFuncionarioService,
            FuncionarioRepository funcionarioRepository,
            ApiDepartamentoService apiDepartamentoService,
            ISincronizacionService sincronizacionService,
            FuncionarioMapper funcionarioMapper) {
        this.apiFuncionarioService = apiFuncionarioService;
        this.funcionarioRepository = funcionarioRepository;
        this.apiDepartamentoService = apiDepartamentoService;
        this.sincronizacionService = sincronizacionService;
        this.funcionarioMapper = funcionarioMapper;
    }

    @Override
    public FuncionarioResponse getFuncionarioInfo(Integer rut) {
        if (rut == null || rut <= 0) {
            throw new FuncionarioException("El RUT del funcionario debe ser un número positivo");
        }
        RuntimeException failure = null;
        String phase = "external_fetch";
        ApiFuncionarioResponse origenValidado = null;
        try {
            java.time.Instant inicioConsulta = java.time.Instant.now();
            ApiFuncionarioResponse apiResponse = apiFuncionarioService.obtenerDetalleColaborador(rut);
            if (apiResponse != null) {
                phase = "identity_validation";
                validarIdentidadExterna(rut, apiResponse);
                origenValidado = apiResponse;
                phase = "synchronization";
                Funcionario funcionario = sincronizacionService.sincronizarYActualizarFuncionario(apiResponse, inicioConsulta);
                phase = "response_build";
                return validarDepartamentoRespuesta(buildFuncionarioResponse(funcionario, apiResponse.getFoto()), apiResponse);
            }
        } catch (RuntimeException error) {
            failure = error;
            logger.warn("event=local_fallback_requested phase={} error_type={}", phase, error.getClass().getSimpleName());
        }

        Optional<Funcionario> local = funcionarioRepository.findByRut(rut);
        if (local.isEmpty() && failure != null) {
            logger.warn("event=local_fallback_unavailable phase={}", phase);
            throw failure;
        }
        Funcionario funcionario = RepositoryUtils.findOrThrow(local, "Funcionario no encontrado");
        if (failure != null && !(failure instanceof FuncionarioException)) {
            logger.error("event=funcionario_processing_failed phase={} recovered=true", phase, failure);
        }
        logger.info("event=local_fallback_used reason={}", failure == null ? "external_empty" : phase);
        FuncionarioResponse respaldo = buildFuncionarioResponse(funcionario, null);
        if (respaldo != null) {
            respaldo.setConfiguracionDepartamento(
                    new com.apifuncionarios.api_funcionarios.dto.ConfiguracionDepartamento(
                            "NO_VERIFICADO",
                            origenValidado == null ? null : origenValidado.getCodDeptoExt(),
                            origenValidado == null ? null : origenValidado.getDepartamento(),
                            respaldo.getCodDepto(), null));
            ocultarDepartamentoNoValidado(respaldo);
        }
        return respaldo;
    }

    private FuncionarioResponse validarDepartamentoRespuesta(FuncionarioResponse respuesta, ApiFuncionarioResponse origen) {
        if (respuesta == null) return null;
        String codigo = origen.getCodDeptoExt();
        String estado = "SIN_CODIGO_ORIGEN";
        Long idInterno = respuesta.getCodDepto();
        Long idAsociado = null;
        if (codigo != null && !codigo.isBlank()) {
            try {
                DepartamentoResponse departamento = apiDepartamentoService.obtenerDetalleDepartamentoByCodigoEx(codigo);
                idAsociado = departamento == null ? null : departamento.getId();
                estado = estadoAsociacion(departamento, idInterno);
            } catch (RuntimeException error) {
                estado = "SERVICIO_NO_DISPONIBLE";
                logger.warn("event=department_validation_unavailable error_type={}", error.getClass().getSimpleName());
            }
        }
        respuesta.setConfiguracionDepartamento(new com.apifuncionarios.api_funcionarios.dto.ConfiguracionDepartamento(
                estado, codigo, origen.getDepartamento(), idInterno, idAsociado));
        if (!"VALIDADO".equals(estado)) {
            ocultarDepartamentoNoValidado(respuesta);
        }
        return respuesta;
    }

    private String estadoAsociacion(DepartamentoResponse departamento, Long idInterno) {
        if (departamento == null) return "SIN_ASOCIACION";
        Long idAsociado = departamento.getId();
        if (idAsociado == null || idAsociado <= 0) return "ASOCIACION_INVALIDA";
        if (idInterno == null) return "SIN_ID_INTERNO";
        return idAsociado.equals(idInterno) ? "VALIDADO" : "ASOCIACION_INCONSISTENTE";
    }

    private void ocultarDepartamentoNoValidado(FuncionarioResponse respuesta) {
        respuesta.setCodDepto(null);
        respuesta.setDepartamento("");
        respuesta.setNombreJefe("");
        respuesta.setCodDeptoJefe(null);
    }

    private void validarIdentidadExterna(Integer rut, ApiFuncionarioResponse response) {
        if (!rut.equals(response.getRut())) {
            logger.warn("event=external_identity_rejected reason=rut_missing_invalid_or_mismatched");
            throw new ExternalServiceException(200, null);
        }
        if (response.getVrut() == null || !response.getVrut().trim().matches("[0-9kK]")) {
            logger.warn("event=external_identity_rejected reason=invalid_verification_digit");
            throw new ExternalServiceException(200, null);
        }
    }

    private FuncionarioResponse buildFuncionarioResponse(Funcionario funcionario, String foto) {
        if (funcionario == null) {
            throw new NotFounException("Funcionario nulo al construir la respuesta");
        }

        DepartamentoResponse depto = null;
        Long idDepto = funcionario.getIdDepto();
        if (idDepto != null) {
            try {
                depto = apiDepartamentoService.obtenerDetalleDepartamentoById(idDepto);
            } catch (RuntimeException error) {
                logger.warn("event=department_enrichment_skipped error_type={}", error.getClass().getSimpleName());
            }
        }

        Funcionario jefeDepto = resolveJefeDepto(depto, funcionario.getRut());

        return funcionarioMapper.toResponseDto(funcionario, depto, jefeDepto, foto);
    }

    // Extrae la lógica de resolución del jefe de departamento para facilitar tests
    // y legibilidad
    Funcionario resolveJefeDepto(DepartamentoResponse depto, Integer rutFuncionario) {
        if (depto == null)
            return null;

        DepartamentoResponse current = depto;
        final int maxHops = 20; // seguridad para evitar loops infinitos

        for (int hops = 0; hops < maxHops; hops++) {

            // Intentar resolver en el departamento actual
            Optional<Funcionario> resolved = attemptResolveInDept(current, rutFuncionario);
            if (resolved.isPresent())
                return resolved.get();

            // Ascender
            Long parentId = current.getIdDeptoSuperior();
            if (parentId == null)
                return null;

            DepartamentoResponse parent = safeObtenerDepto(parentId);
            if (parent == null)
                return null;

            // Intento final si es nivel de parada
            if (isStopLevel(parent)) {
                Optional<Funcionario> finalResolved = attemptResolveInDept(parent, rutFuncionario);
                return finalResolved.orElse(null);
            }

            current = parent;
        }

        return null;
    }

    private Optional<Integer> getCandidateRut(DepartamentoResponse depto, Integer rutFuncionario) {
        if (depto == null)
            return Optional.empty();
        Integer rutJefe = depto.getRutJefe();
        Integer rutJefeSup = depto.getRutJefeSuperior();

        if (rutJefe != null && !java.util.Objects.equals(rutJefe, rutFuncionario)) {
            return Optional.of(rutJefe);
        }
        if (rutJefe == null && rutJefeSup != null && !java.util.Objects.equals(rutJefeSup, rutFuncionario)) {
            return Optional.of(rutJefeSup);
        }
        return Optional.empty();
    }

    private Optional<Funcionario> findFuncionarioByRut(Integer rut) {
        if (rut == null)
            return Optional.empty();
        try {
            return funcionarioRepository.findByRut(rut);
        } catch (Exception e) {
            logger.error("event=manager_lookup_failed", e);
            return Optional.empty();
        }
    }

    private Optional<Funcionario> attemptResolveInDept(DepartamentoResponse depto, Integer rutFuncionario) {
        Optional<Integer> candidateRutOpt = getCandidateRut(depto, rutFuncionario);
        if (candidateRutOpt.isEmpty())
            return Optional.empty();
        return findFuncionarioByRut(candidateRutOpt.get());
    }

    private DepartamentoResponse safeObtenerDepto(Long id) {
        try {
            return apiDepartamentoService.obtenerDetalleDepartamentoById(id);
        } catch (Exception e) {
            logger.warn("event=manager_department_unavailable error_type={}", e.getClass().getSimpleName());
            return null;
        }
    }

    private boolean isStopLevel(DepartamentoResponse depto) {
        if (depto == null || depto.getNombre() == null)
            return false;
        String nombre = depto.getNombre().toUpperCase();
        return nombre.contains("SUBDIRECCION") || nombre.contains("DIRECCION");
    }

    @Override
    public ResumenFeriadoLegal resumenFeriadosLegales(Integer rut, Integer ident) {
        ResumenFeriadoLegal resumen = apiFuncionarioService.obtenerFeriadosLegales(rut, ident);
        if (resumen == null) {
            return null;
        }
        return resumen;
    }

    @Override
    public ResumenAdm resumenAdm(Integer rut, Integer ident) {
        ResumenAdm resumen = apiFuncionarioService.obtenerAdministrativos(rut, ident);
        if (resumen == null) {
            logger.debug("event=administrative_summary_empty");
            throw new NotFounException("No se encontraron dias administrativos");
        }
        return resumen;
    }
}
