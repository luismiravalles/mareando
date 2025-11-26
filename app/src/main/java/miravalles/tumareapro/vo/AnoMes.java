package miravalles.tumareapro.vo;

/**
 * Guarda Año y mes y permite obtener facilmente el siguiente.
 * Es una clase inmutable.
 */
public class AnoMes {
    private int mes;
    private int ano;

    public AnoMes(int ano, int mes) {
        this.ano=ano;
        this.mes=mes;
    }

    public int getMes() { return mes;}
    public int getAno() { return ano;}

    public int getMesBaseUno() { return mes+1; }



    public AnoMes siguiente() {
        int mesSiguiente=mes+1;
        int anoSiguiente=ano;
        if(mesSiguiente>=12) {
            mesSiguiente=0;
            anoSiguiente++;
        }
        return new AnoMes(anoSiguiente, mesSiguiente);
    }

    public String toString() {
        return "" + ano + "-" + getMesBaseUno();
    }

}
