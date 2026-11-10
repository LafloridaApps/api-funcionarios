# Unicidad y sincronización por RUT

La sincronización identifica al funcionario por su RUT numérico, conserva su ID y actualiza sus datos, incluido el dígito verificador y las fechas de contrato. Si otra solicitud inserta el mismo RUT simultáneamente, se revierte el intento de inserción y se actualiza el registro existente en una nueva transacción.

La tabla debe tener la restricción única `uk_funcionarios_rut` y la columna `rut` no debe admitir NULL. El mapeo JPA declara ambas condiciones. Para bases existentes, verificar antes del despliegue:

```sql
SELECT rut, COUNT(*) AS cantidad
FROM funcionarios
GROUP BY rut
HAVING COUNT(*) > 1 OR rut IS NULL OR rut <= 0;
```

Si devuelve filas, revisar y consolidar esos registros y sus referencias antes de agregar la restricción. No se eliminan automáticamente registros históricos.

Si la restricción todavía no existe, aplicar una vez después de consolidar:

```sql
ALTER TABLE funcionarios MODIFY COLUMN rut INT NOT NULL;
ALTER TABLE funcionarios ADD CONSTRAINT uk_funcionarios_rut UNIQUE (rut);
```

Confirmar la instalación con `SHOW CREATE TABLE funcionarios`. Hibernate solamente
valida el esquema al iniciar; no crea columnas ni índices. Si existen datos
duplicados, MySQL rechazará la creación del índice.

## Arranque del contenedor y protección de datos

El contenedor ejecuta `java -jar app.jar`. La configuración del repositorio usa
`spring.jpa.hibernate.ddl-auto=validate` y `spring.sql.init.mode=never`: al iniciar,
Hibernate comprueba las tablas y columnas sin modificarlas y Spring no ejecuta
scripts `schema.sql` o `data.sql`. Si falta una columna o su tipo es incompatible,
el inicio falla. Después se comprueban, mediante consultas de lectura a
`information_schema`, el índice único del RUT y su condición `NOT NULL`.
No se corrigen duplicados ni se borran datos al iniciar. No hay tareas de
sincronización registradas para el arranque; la sincronización durante las
peticiones sí puede insertar o actualizar funcionarios.

Antes de desplegar sobre una base existente:

1. Obtener un respaldo consistente de datos y esquema con las herramientas del
   administrador de MySQL y comprobar que se puede restaurar en otra base.
2. Revisar `SHOW CREATE TABLE funcionarios`, los duplicados y los RUT nulos con
   las consultas de esta guía. Aplicar los cambios pendientes en una ventana
   controlada, después de revisar las referencias a los registros afectados.
3. Usar una cuenta de ejecución con los permisos de lectura e inserción y
   actualización que necesita la aplicación, sin permisos `CREATE`, `ALTER`,
   `DROP` ni `DELETE`. Usar otra cuenta para migraciones. La cuenta de ejecución
   debe poder consultar los metadatos de su tabla en `information_schema`.
4. Revisar las variables y opciones del despliegue: no sobrescribir estas
   protecciones con `SPRING_JPA_HIBERNATE_DDL_AUTO=update`, `create` o
   `create-drop`, `SPRING_SQL_INIT_MODE=always`, propiedades de generación DDL
   de JPA/Hibernate ni argumentos equivalentes en `JAVA_TOOL_OPTIONS` o la línea
   de comandos. Las propiedades externas pueden reemplazar las del JAR.
5. Comprobar los logs y `/actuator/health` antes de dar el despliegue por válido.
   Si falla la validación, revisar el esquema con la cuenta de migraciones;
   no habilitar `ddl-auto=update` para intentar reparar el arranque.

Estas comprobaciones de código no verifican el estado de una base desplegada ni
sustituyen el respaldo. Las migraciones siguientes son manuales y no se ejecutan
desde el contenedor.

## Actualizaciones parciales y orden de sincronización

El DTO registra los campos presentes: un campo ausente conserva el valor local; un
`null` explícito borra los campos que admiten nulos. `false` y `0` explícitos se
aplican. Para crear un funcionario se exigen nombres no vacíos, `ident` y `vigente`,
además del RUT y DV. Una respuesta de departamento sin ID positivo conserva la
asociación local. El RUT recibido debe coincidir con el solicitado.

Antes de desplegar esta versión sobre una tabla existente, agregar una vez la
columna de control (comprobar primero con `SHOW COLUMNS FROM funcionarios`):

```sql
ALTER TABLE funcionarios
    ADD COLUMN ultima_sincronizacion TIMESTAMP(6) NULL DEFAULT NULL;
```

La consulta externa captura su instante de inicio. Al guardar, se bloquea la fila
por RUT dentro de la transacción y se descartan respuestas de consultas iniciadas
antes de la última aplicada. El control también se aplica al recuperar una
inserción concurrente; los conflictos de bloqueo se reintentan una vez en una
transacción nueva. Los registros existentes con marca NULL aceptan la primera
sincronización.

Esto ordena consultas de esta aplicación, no versiones de origen: la API externa
no proporciona una versión o fecha de modificación que permita detectar datos ya
obsoletos en su respuesta. Las instancias deben mantener sus relojes sincronizados.
Los consumidores directos del servicio deben usar la sobrecarga con el instante
capturado antes de consultar la API; la sobrecarga de un argumento usa el instante
de invocación. Las pruebas unitarias no sustituyen una prueba de concurrencia
contra MySQL ni verifican el esquema desplegado.

## Cambio de dominio (`ident`)

El RUT identifica a la persona. `ident` es un atributo mutable: si el RUT ya
existe, se conserva el mismo `id` local y se actualiza `ident` con el valor recibido,
incluso si cambia de dominio. Si `ident` no viene en la respuesta, se conserva el
actual. No se busca ni se crea un registro por la combinación `(rut, ident)`.

Al iniciar, la aplicación comprueba en MySQL que exista un índice único sobre
`rut` solamente. Un índice único compuesto `(rut, ident)` no satisface esta regla.
Si falta el índice, el arranque falla con una explicación; no se eliminan ni se
consolidan datos automáticamente. Aplicar el SQL de esta guía después de resolver
los duplicados existentes. El índice rechaza también las inserciones duplicadas
realizadas fuera del servicio de sincronización.
