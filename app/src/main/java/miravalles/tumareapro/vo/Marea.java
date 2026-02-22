package miravalles.tumareapro.vo;

import java.util.Date;

import miravalles.tumareapro.Modelo;

/**
 * Representa los datos mínimos de una marea que vienen a ser
 * la fecha/hora y la altura.
 *
 */
public class Marea {

    int hora;

    int minuto;

    int altura;

    boolean pleamar;

    int coeficiente;

    int edadLunar;

    public Marea(int hora, int minuto, int altura, int coeficiente, int edadLunar) {
        this.hora=hora;
        this.minuto=minuto;
        this.altura=altura;
        this.coeficiente=coeficiente;
        this.edadLunar = edadLunar;
    }

    public int getHora() {
        return hora;
    }

    public int getMinuto() {
        return minuto;
    }

    public int getAltura() {
        return altura;
    }

    public void setPleamar(boolean pleamar) {
        this.pleamar=pleamar;
    }

    public boolean isPleamar() {
        return pleamar;
    }

    public int getCoeficiente() {
        return this.coeficiente;
    }

    public int getEdadLunar() {
        return edadLunar;
    }

    public String getAlturaFormateada() {
        return String.format("%1d.%02dm", altura/100, altura%100);
    }

    public String getHoraFormateada() {
        return String.format("%02d:%02d", hora, minuto);
    }

    @Override
    public String toString() {
        return "Marea{" +
                "hora=" + hora +
                ", minuto=" + minuto +
                ", altura=" + altura +
                ", pleamar=" + pleamar +
                ", coeficiente=" + coeficiente +
                ", edadLunar=" + edadLunar +
                '}';
    }
}
