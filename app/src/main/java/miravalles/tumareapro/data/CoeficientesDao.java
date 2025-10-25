package miravalles.tumareapro.data;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Carga los coeficientes de una URL utilizando un archivo privado como caché.
 */
public class CoeficientesDao {

    CacheDao<Integer> cacheDao;

    public CoeficientesDao(Context context) {
        this.cacheDao=new CacheDao<>(context, "coeficientes",
                year -> {
                    return "https://macetero.duckdns.org/tumarea/coeficientes-"
                                + year + ".txt";
                });
    }

    public int[][] cargarCoeficientes(int ano) {
        int [][] coeficientes = new int[12][31];
        this.cacheDao.descargarSiEsNecesario(ano);
        File fichero=this.cacheDao.getFileLocal(ano);
        int mes=0;
        try(InputStream is=new FileInputStream(fichero)) {
            BufferedReader bf=new BufferedReader(new InputStreamReader(is), 8*1024);
            String linea=bf.readLine();

            while(linea!=null) {
                linea=linea.replace(",", "");
                String pals[]=linea.split(";");
                int dia=0;
                for(String pal:pals) {
                    int valor=Integer.parseInt(pal);
                    coeficientes[mes][dia]=Integer.parseInt(pal);
                    dia++;
                }
                linea=bf.readLine();
                mes++;
            }
        } catch(IOException e) {
            Log.e("X", "Error leyendo " + fichero+ ":" + e.getMessage());
        }
        Log.i("X", "Cargados coeficientes desde " + fichero);
        return coeficientes;
    }

}
