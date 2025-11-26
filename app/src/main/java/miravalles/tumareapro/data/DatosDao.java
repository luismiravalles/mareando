package miravalles.tumareapro.data;

import miravalles.tumareapro.domain.Sitio;
import miravalles.tumareapro.vo.AnoMes;

/**
 * Generalización de la obtención de datos externa, sin comprometerse a saber
 * si viene de un lugar externo, o de un fichero o de una base de datos.
 */
public interface DatosDao {

    /**
     * Obtener los datos de marea y alturas de un sitio para un mes determinado.
     * @param sitio
     * @param anoMes El año y el mes
     */
    void obtenerDatosMes(Sitio sitio, AnoMes anoMes);
}