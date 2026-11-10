# Logs y diagnóstico

Cada petición genera un `request_id` nuevo en el contexto de logs y lo devuelve
como header `X-Request-ID`. El ID recibido del cliente no se reutiliza. El contexto
se limpia al finalizar para no mezclar peticiones del mismo hilo. Las llamadas
externas registran el resultado tras su ejecución bloqueante, en el hilo de la
petición, para conservar la correlación.

Ejemplo de formato en ejecución con Spring Boot:

```text
WARN [request_id=...] event=external_failed service=funcionarios operation=detalle status=503 error_type=ExternalServiceException duration_ms=30
WARN [request_id=...] event=local_fallback_requested phase=external_fetch error_type=ExternalServiceException
INFO [request_id=...] event=local_fallback_used reason=external_fetch
INFO [request_id=...] event=request_completed method=GET route=/api/funcionario status=200 duration_ms=35
```

## Eventos

| Evento | Nivel | Significado |
| --- | --- | --- |
| `request_completed` | INFO | Método, plantilla de ruta, estado final y duración de la petición. No incluye query string. |
| `external_completed` | DEBUG | Servicio, operación, estado externo, resultado `ok`/`empty` y duración. |
| `external_failed` | WARN | Fallo HTTP, transporte, timeout o deserialización. `status=0` indica que no se recibió estado HTTP; `200` con error suele indicar un problema al interpretar la respuesta. |
| `local_fallback_requested` | WARN | Fallo recuperable; `phase` indica dónde ocurrió. Todavía no confirma recuperación. |
| `local_fallback_used` | INFO | Se encontró la fila local y se usa para construir la respuesta. |
| `local_fallback_unavailable` | WARN | No hay fila local; se propaga el error original. |
| `sync_department_preserved` | WARN | Se conservó el departamento local ante un fallo o respuesta sin ID válido. |
| `sync_retry` | WARN | Segundo intento por integridad o bloqueo. No confirma éxito. |
| `sync_stale_ignored` | INFO | Se descartó una consulta anterior a la última aplicada. |
| `sync_ident_update_prepared` | DEBUG | Se prepara asignar el `ident` recibido al mismo ID local. No confirma commit. |
| `sync_completed` | INFO | Terminó la transacción; incluye ID local y duración de persistencia. Puede acompañar a `sync_stale_ignored` si no hubo actualización. |
| `funcionario_processing_failed` | ERROR | Fallo inesperado recuperado con datos locales; incluye traza. |
| `request_failed` | ERROR | Fallo inesperado sin recuperación; incluye traza. |

Los logs operativos de llamadas externas no incluyen URLs completas, RUT,
query strings, fotos ni cuerpos de respuesta. Las trazas de errores inesperados
pueden contener detalles de infraestructura y deben tener acceso restringido.
Los logs de éxito de llamadas externas y preparación de `ident` se habilitan con:

```properties
logging.level.com.apifuncionarios.api_funcionarios=DEBUG
```

## Respuestas de error

- Un 404/204 externo se trata como ausencia de datos y su cuerpo no se deserializa.
- Otros estados externos, errores de transporte y JSON inválido generan un error
  de dependencia. Para el detalle del funcionario se intenta usar la fila local.
- Si no es posible recuperar, el fallo externo responde 502 con mensaje genérico.
- Los parámetros inválidos responden 400; los errores HTTP de Spring conservan su
  estado y sus headers (por ejemplo 405 y `Allow`).
- Los fallos inesperados responden 500 sin SQL, mensajes internos ni trazas en el
  cuerpo. El cliente debe informar `X-Request-ID` para investigar.
- Las llamadas externas tienen un límite total de 15 segundos, además de los
  límites de conexión y respuesta de cada cliente.

La duración de `sync_completed` mide persistencia y reintentos; la duración total,
incluyendo llamadas externas y enriquecimiento, está en `request_completed`.

Estas pruebas usan clientes HTTP simulados y peticiones servlet de prueba; no se
ha validado tráfico de producción ni conectado las APIs reales.
