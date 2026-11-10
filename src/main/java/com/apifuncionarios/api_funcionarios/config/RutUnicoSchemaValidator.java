package com.apifuncionarios.api_funcionarios.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** Impide operar si MySQL no garantiza una sola fila por RUT. */
@Component
public class RutUnicoSchemaValidator implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public RutUnicoSchemaValidator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        // Un índice compuesto (rut, ident) permite duplicar el RUT al cambiar de dominio.
        Long indices = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM (
                    SELECT index_name
                    FROM information_schema.statistics
                    WHERE table_schema = DATABASE()
                      AND table_name = 'funcionarios'
                      AND non_unique = 0
                    GROUP BY index_name
                    HAVING COUNT(*) = 1 AND MAX(column_name) = 'rut'
                       AND MAX(sub_part) IS NULL
                ) AS indices_rut
                """, Long.class);
        if (indices == null || indices == 0) {
            throw new IllegalStateException("La tabla funcionarios requiere un índice UNIQUE sobre rut solamente. "
                    + "Consolidar duplicados y aplicar la migración de docs/unicidad-rut.md antes de iniciar. "
                    + "ident es mutable y no forma parte de la clave única.");
        }
        Long columnasRutNoNulas = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.columns
                WHERE table_schema = DATABASE()
                  AND table_name = 'funcionarios'
                  AND column_name = 'rut'
                  AND is_nullable = 'NO'
                """, Long.class);
        if (columnasRutNoNulas == null || columnasRutNoNulas == 0) {
            throw new IllegalStateException("La columna funcionarios.rut requiere NOT NULL. "
                    + "Revisar los datos y aplicar la migración de docs/unicidad-rut.md antes de iniciar. "
                    + "No se modifican datos ni el esquema automáticamente.");
        }
    }
}
