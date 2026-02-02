package com.example;

import org.junit.Test;
import org.shredzone.commons.suncalc.MoonIllumination;

import java.util.Calendar;
import java.util.Date;

import miravalles.tumareapro.Modelo;

public class TestLuna {

    @Test
    public void testCalcularEdadLuna() {

        for(int mes=0; mes<2; mes++) {
            for(int dia=1; dia<=28; dia++) {
                Calendar cal = Calendar.getInstance();
                cal.set(2026, mes, dia, 00, 00);

                int edad = Modelo.getEdadLuna(cal.getTime());
                int edadAstro = getEdadLuna(cal.getTime());
                double fase =getPhase100(cal.getTime());
                System.out.printf(
                        "%2d/%2d Edad de la luna %2d %2d %3.2f\n " ,
                        mes, dia, edad , edadAstro, fase);
            }

        }
    }


    public static int getEdadLuna(Date date) {
        // 1. Obtener la iluminación de la luna para hoy

        MoonIllumination moon = MoonIllumination.compute()
                .on(date) // Usa la fecha actual del dispositivo
                .execute();
        double faseGrados = moon.getPhase();

        double edadLuna = ((faseGrados + 180) / 360) * 29.53;
        int result= (int)Math.round(edadLuna);
        if(result>=29) {
            result=0;
        }
        return result;
    }

    public static double getPhase100(Date date) {
        // 1. Obtener la iluminación de la luna para hoy

        MoonIllumination moon = MoonIllumination.compute()
                .on(date) // Usa la fecha actual del dispositivo
                .execute();
        double faseGrados = moon.getPhase();
        return moon.getPhase() * 180 / 360;
    }



}
