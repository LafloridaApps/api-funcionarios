
package com.apifuncionarios.api_funcionarios.services.sync;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import com.apifuncionarios.api_funcionarios.dto.ApiFuncionarioResponse;
import com.apifuncionarios.api_funcionarios.dto.DepartamentoResponse;
import com.apifuncionarios.api_funcionarios.entities.Funcionario;
import com.apifuncionarios.api_funcionarios.exceptions.FuncionarioException;
import com.apifuncionarios.api_funcionarios.mappers.FuncionarioMapper;
import com.apifuncionarios.api_funcionarios.repositories.FuncionarioRepository;
import com.apifuncionarios.api_funcionarios.services.interfaces.ApiDepartamentoService;

@Service
public class SincronizacionServiceImpl implements ISincronizacionService {

    private static final Logger logger = LoggerFactory.getLogger(SincronizacionServiceImpl.class);

    private final FuncionarioRepository funcionarioRepository;
    private final FuncionarioMapper funcionarioMapper;
    private final ApiDepartamentoService apiDepartamentoService;
    private final TransactionTemplate transactionTemplate;

    public SincronizacionServiceImpl(FuncionarioRepository funcionarioRepository, FuncionarioMapper funcionarioMapper, ApiDepartamentoService apiDepartamentoService, PlatformTransactionManager transactionManager) {
        this.funcionarioRepository = funcionarioRepository;
        this.funcionarioMapper = funcionarioMapper;
        this.apiDepartamentoService = apiDepartamentoService;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public Funcionario sincronizarYActualizarFuncionario(ApiFuncionarioResponse response) {
        return sincronizarYActualizarFuncionario(response, Instant.now());
    }

    @Override
    public Funcionario sincronizarYActualizarFuncionario(ApiFuncionarioResponse response, Instant inicioConsulta) {
        if (inicioConsulta == null) {
            throw new FuncionarioException("La fecha de inicio de consulta es obligatoria");
        }
        if (response == null || response.getRut() == null || response.getRut() <= 0) {
            throw new FuncionarioException("El RUT del funcionario debe ser un número positivo");
        }
        if (response.getVrut() == null || !response.getVrut().trim().matches("[0-9kK]")) {
            throw new FuncionarioException("El dígito verificador del RUT es obligatorio y debe ser 0-9 o K");
        }
        DepartamentoResponse depto = null;
        try {
            if (response.getCodDeptoExt() != null && !response.getCodDeptoExt().isBlank()) {
                depto = apiDepartamentoService.obtenerDetalleDepartamentoByCodigoEx(response.getCodDeptoExt());
                if (depto == null || depto.getId() == null || depto.getId() <= 0) {
                    logger.warn("event=sync_department_preserved reason=missing_or_invalid_department");
                }
            }
        } catch (FuncionarioException error) {
            logger.warn("event=sync_department_preserved reason=external_failure error_type={}", error.getClass().getSimpleName());
        }

        // Igualar la precisión de la columna MySQL para ordenar también lecturas posteriores.
        final Instant inicio = inicioConsulta.truncatedTo(java.time.temporal.ChronoUnit.MICROS);
        final DepartamentoResponse departamento = depto;
        long start = System.nanoTime();
        Funcionario result = persistir(response, departamento, inicio);
        logger.info("event=sync_completed entity_id={} duration_ms={}", result.getId(), (System.nanoTime() - start) / 1_000_000);
        return result;
    }

    private Funcionario persistir(ApiFuncionarioResponse response, DepartamentoResponse departamento, Instant inicio) {
        try {
            return transactionTemplate.execute(status -> guardar(response, departamento, inicio));
        } catch (DataIntegrityViolationException conflict) {
            logger.warn("event=sync_retry reason=integrity_conflict attempt=2");
            // La transacción fallida debe terminar antes de consultar el registro concurrente.
            return transactionTemplate.execute(status -> {
                Funcionario existente = funcionarioRepository.findByRutForUpdate(response.getRut()).orElse(null);
                if (existente == null) {
                    throw conflict;
                }
                return actualizar(existente, response, departamento, inicio);
            });
        } catch (PessimisticLockingFailureException conflict) {
            logger.warn("event=sync_retry reason=lock_conflict attempt=2");
            // InnoDB puede abortar una de dos altas que bloquean el mismo hueco del índice.
            return transactionTemplate.execute(status -> guardar(response, departamento, inicio));
        }
    }

    private Funcionario guardar(ApiFuncionarioResponse response, DepartamentoResponse depto, Instant inicioConsulta) {
        Funcionario funcionario = funcionarioRepository.findByRutForUpdate(response.getRut())
                .orElseGet(() -> {
                    if (response.getNombres() == null || response.getNombres().isBlank()
                            || !response.contieneCampo("vigente") || !response.contieneCampo("ident")) {
                        throw new FuncionarioException("Respuesta incompleta para crear un funcionario: faltan nombres, vigente o ident");
                    }
                    return funcionarioMapper.toEntity(response);
                });
        return actualizar(funcionario, response, depto, inicioConsulta);
    }

    private Funcionario actualizar(Funcionario funcionario, ApiFuncionarioResponse response,
            DepartamentoResponse depto, Instant inicioConsulta) {
        if (funcionario.getUltimaSincronizacion() != null
                && !inicioConsulta.isAfter(funcionario.getUltimaSincronizacion())) {
            logger.info("event=sync_stale_ignored entity_id={}", funcionario.getId());
            return funcionario;
        }
        if (response.contieneCampo("ident") && funcionario.getId() != null) {
            logger.debug("event=sync_ident_update_prepared entity_id={} new_ident={}",
                    funcionario.getId(), response.getIdent());
        }
        funcionarioMapper.updateEntityFromDto(funcionario, response, depto);
        funcionario.setUltimaSincronizacion(inicioConsulta);
        return funcionarioRepository.saveAndFlush(funcionario);
    }
}
