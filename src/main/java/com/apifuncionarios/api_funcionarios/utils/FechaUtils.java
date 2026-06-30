package com.apifuncionarios.api_funcionarios.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class FechaUtils {

    private static final ZoneId SANTIAGO_ZONE = ZoneId.of("America/Santiago");

    private FechaUtils() {

    }

    public static LocalDate fechaActual() {
        ZonedDateTime fechaActualSantiago = ZonedDateTime.now(SANTIAGO_ZONE);
        return fechaActualSantiago.toLocalDate();
    }

    public static LocalDateTime fechaActualHora(){
          ZonedDateTime fechaActualSantiago = ZonedDateTime.now(SANTIAGO_ZONE);
        return fechaActualSantiago.toLocalDateTime();

    }
}
