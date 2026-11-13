# Campos adicionales del listado de contratos

El endpoint existente `GET /api/funcionario/{rut}/list` conserva todos los contratos devueltos por smcapis, vigentes y no vigentes. Agrega `numresolcontr` y `obs_adicional_cont`, ambos de tipo string nullable, a cada elemento.

smcapis lee estas columnas desde `sql/funcionarioList.sql` mediante ResultSet.getString, conservando texto, ceros iniciales y valores nulos. api-funcionarios las deserializa y vuelve a exponer con los mismos nombres JSON.

## ADR

Se utiliza un record FuncionarioContratoResponse dedicado al listado en cada servicio. No se amplían los DTO usados por el endpoint individual ni por la sincronización local. No se requiere persistencia adicional ni migraciones. Esta separación evita agregar propiedades a las respuestas individuales y permite evolucionar el listado sin afectar ese contrato.

La publicación requiere actualizar primero smcapis y luego api-funcionarios. Un smcapis anterior producirá null en los campos nuevos.
