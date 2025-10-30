package miravalles.tumareapro.data;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import miravalles.tumareapro.Util;
import miravalles.tumareapro.domain.Sitio;

/**
 * Clase que se encarga de descargar y persistir localmente la información de las
 * mareas.
 * La información de un mes y una estación(sitio) se representa en un fichero que
 * puede estar persistido previamente y si no lo está se lo pediremos al Instituto
 * Hidrográfico.
 *
 */
public class InstitutoMarinaDatosDao implements DatosDao {

    public static final String URL_BASE="https://ideihm.covam.es/api-ihm/getmarea?request=gettide";

    Context context;


    public InstitutoMarinaDatosDao(Context context) {
        this.context=context;
    }

    public void obtenerDatosMes(Sitio sitio, int ano, int mes) {
        Log.i("X", "Obtener datos de " + sitio.nombre + " mes " + mes);
        if(hayDatosLocales(sitio, ano, mes)) {
            cargarDatosLocales(sitio, ano, mes);
            return;
        }
        sitio.setErrorInstitutoMarina(mes, descargarDatosRemotos(sitio, ano, mes));
        cargarDatosLocales(sitio, ano, mes);
    }

    private void cargarDatosLocales(Sitio sitio, int ano, int mes) {
        Log.i("X", "Datos Locales de " + sitio.nombre + " mes " + mes);
        File fichero=getFileLocal(sitio, ano, mes);
        try(InputStream in=new FileInputStream(fichero)) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(in);
            doc.getDocumentElement().normalize();
            Element raiz=doc.getDocumentElement();
            Element datos=(Element)raiz.getElementsByTagName("datos").item(0);
            NodeList mareas=datos.getElementsByTagName("marea");
            for(int i=0; i<mareas.getLength(); i++) {
                Element marea=(Element)mareas.item(i);
                String fecha=marea.getAttribute("fecha");
                String hora=marea.getAttribute("hora");
                String altura=marea.getAttribute("altura");
                guardarEnSitio(sitio, mes, fecha, hora, altura);
            }
        } catch(Exception e) {
            // Cualquier error parseando el fichero debe implicar que lo
            // borremos para forzar la carga posteriormente.
            Log.e("X", e.getMessage());
            fichero.delete();
        }
    }

    /**
     * La estructura de las mareas del Sitio es muy peculiar. Tenemos una matriz
     * de mareas lineal en la que cada dato es la cantidad de minutos desplazados
     * respecto del mes en cuestión.
     *
     * La fecha nos llega en el formato de IHM: 2026-01-31
     * La hora nos llega en formato "HH:MM"
     * La altura es un dato en metros con decimales "3.656".
     *
     *
     * @param sitio
     * @param fecha
     * @param hora
     * @param altura
     */
    private void guardarEnSitio(Sitio sitio, int mes, String fecha, String hora, String altura) {
        int dias=getDia(fecha) - 1;
        int minutosDia=24 * 60;
        int horas=Integer.parseInt(hora.substring(0,2));
        int minutos=Integer.parseInt(hora.substring(3,5));
        int valorMarea= dias * minutosDia + horas *60 + minutos;
        int valorAltura = Integer.parseInt(altura.replace(".", "")) / 10;

        int indice=sitio.primerEspacioVacio(mes);

        sitio.marea[mes][indice]=valorMarea;
        sitio.altura[mes][indice]=valorAltura;

        //Log.i("X" , "Cargado " + sitio.nombre + " dia " + fecha + " " + hora
        //               + "indice " + indice + " marea" + valorMarea);
    }



    private int getDia(String fecha) {
        return Integer.parseInt(fecha.substring(8));
    }

    private File getFileLocal(Sitio sitio, int ano, int mes) {
        String nombreLocal=componerNombreLocal(sitio, ano, mes);
        return new File(context.getFilesDir(), nombreLocal);
    }

    private boolean hayDatosLocales(Sitio sitio, int ano, int mes) {
        File fichero=getFileLocal(sitio, ano, mes);
        return fichero.exists() && fichero.length()>0;
    }

    private String descargarDatosRemotos(Sitio sitio, int ano, int mes) {
        Log.i("INTERNET", "Descargando datos remotos de " + sitio.nombre + " mes " + mes);
        ConnectivityManager check = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo=check.getAllNetworkInfo();
        boolean conectado=false;
        Log.i("X", "Verificando red...");
        for(int i=0; i<netInfo.length; i++) {
            if(netInfo[i].getState()==NetworkInfo.State.CONNECTED) {
                conectado=true;
                break;
            }
        }
        if(conectado==false) {
            return "Problemas de conexión de red. No hay red.";
        }
        Log.i("X", "Verificada");
        try {
            URL url = new URL(componerUrl(sitio, ano,  mes));
            Log.i("X", "Descargando de " + url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            File ficheroLocal=getFileLocal(sitio, ano, mes);
            try(
                InputStream in = conn.getInputStream();
                OutputStream out= new FileOutputStream(ficheroLocal);
                ) {
                byte[] buffer = new byte[4096];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
            }
        } catch(Exception e) {
            Log.e("X", e.getMessage());
            return "No se pudo conectar con Instituto Hidrográfico de la Marina " +
                    " para obtener los datos de " + sitio.nombre + " " + ano + "-" + (mes+1);

        }
        return null;
    }


    private String componerNombreLocal(Sitio sitio, int ano, int mes) {
        return "datos_IHM_" + sitio.getIdIHM()
                + "_"
                +  ano + String.format("%02d", mes);
    }

    /**
     * Obtenemos siempre los datos a futuro. por tanto si me piden
     * datos de un mes menor al actual es que se refiere al mes
     * siguiente.
     */
    public int year(int mes) {
        int year= Util.thisYear();
        if(mes < Util.thisMonth()) {
            year++;
        }
        return year;
    }
    private String componerUrl(Sitio sitio, int ano, int mes) {
        return URL_BASE
                + "&id=" + sitio.getIdIHM()
                + "&format=xml"
                + "&month=" + ano + String.format("%02d", (mes+1));
    }

}
