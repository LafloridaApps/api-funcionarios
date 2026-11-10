package com.apifuncionarios.api_funcionarios.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.apifuncionarios.api_funcionarios.dto.SearchnNombreDto;
import com.apifuncionarios.api_funcionarios.entities.Funcionario;

public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {

    @Query(value = "SELECT v.nombre_completo, v.rut, v.vrut, f.id_depto AS codDepto FROM vfuncionarios v "
            + "JOIN funcionarios f ON f.rut = v.rut "
            + "WHERE LOWER(v.nombre_completo) LIKE LOWER(CONCAT('%', :nombres, '%')) AND f.id_depto IN (:deptos)",
            countQuery = "SELECT COUNT(*) FROM vfuncionarios v JOIN funcionarios f ON f.rut = v.rut "
            + "WHERE LOWER(v.nombre_completo) LIKE LOWER(CONCAT('%', :nombres, '%')) AND f.id_depto IN (:deptos)", nativeQuery = true)
    Page<SearchnNombreDto> findByNombreEnDepartamentos(@org.springframework.data.repository.query.Param("nombres") String nombres,
            @org.springframework.data.repository.query.Param("deptos") java.util.List<Long> deptos, Pageable pageable);

    Optional<Funcionario> findByRut(Integer rut);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM Funcionario f WHERE f.rut = :rut")
    Optional<Funcionario> findByRutForUpdate(@org.springframework.data.repository.query.Param("rut") Integer rut);

    Page<Funcionario> findByNombresContainingIgnoreCaseOrApellidoPaternoContainingIgnoreCaseOrApellidoMaternoContainingIgnoreCase(
            String nombres, String paterno, String materno, Pageable pageable);

    @Query(value = "SELECT v.nombre_completo, v.rut, v.vrut, f.id_depto AS codDepto FROM vfuncionarios v LEFT JOIN funcionarios f ON f.rut = v.rut WHERE v.nombre_completo LIKE CONCAT('%', :nombres, '%')", nativeQuery = true)
    Page<SearchnNombreDto> findByNombre(String nombres, Pageable pageable);

}
