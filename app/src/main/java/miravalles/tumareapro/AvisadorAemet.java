package miravalles.tumareapro;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import miravalles.tumareapro.domain.Sitio;

public class AvisadorAemet {

    private static boolean avisosAemet=true;

    private Set<String> avisoVisto= new HashSet<>();

    private static final String API_KEY="eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJsdWlzbWlyYXZhbGxlc0BnbWFpbC5jb20iLCJqdGkiOiJhZjA3YWIzZC1jZDZmLTQzODktOWE1Yy0wMTEwY2I1OWQ1NTQiLCJpc3MiOiJBRU1FVCIsImlhdCI6MTc2NDY2NDA0MywidXNlcklkIjoiYWYwN2FiM2QtY2Q2Zi00Mzg5LTlhNWMtMDExMGNiNTlkNTU0Iiwicm9sZSI6IiJ9.QpJk_7fPeMXSWtwV79cH0KGZPPDYCgKJTlTngqO9MQk";

    private static final String URL_BASE="https://opendata.aemet.es/opendata/api/prediccion/maritima/costera/costa/";

    public void verificarAviso(Context context, Sitio sitio, Consumer<String> consumidor) {
        if(!avisosAemet) {
            return;
        }
        if(sitio==null) {
            return;
        }
        String area=sitio.getIdAreaAemet();
        if(area==null) {
            return;
        }


        if(avisoVisto.contains(area)) {
            return;
        }

        avisoVisto.add(area);

        ExecutorService executor = Executors.newSingleThreadExecutor();


        executor.execute(() -> {
            try {
                URL url = new URL(URL_BASE + area);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("api_key", API_KEY);
                connection.setRequestMethod("GET");
                int code=connection.getResponseCode();
                String result=null;
                if(code== HttpURLConnection.HTTP_OK) {
                    result = readStream(connection.getInputStream());
                }
                connection.disconnect();
                if(result!=null) {
                    JSONObject json=new JSONObject(result);
                    if(!"200".equals(json.getString("estado"))) {
                        return;
                    }
                    String urlConsulta=json.getString("datos");
                    consultar(context,urlConsulta, consumidor);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }


    /**
     * La consulta a la AEMET tiene dos partes, primero con la API nos devuelve una URL
     * que tenemos que consultar para luego obtener los datos:
     * @param context
     * @param urlConsulta
     */
    private void consultar(final Context context, String urlConsulta, Consumer<String> consumidor) {

        try {
            URL url = new URL(urlConsulta);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestMethod("GET");
            int code=connection.getResponseCode();
            String result=null;
            String texto=null;
            String titulo=null;
            if(code== HttpURLConnection.HTTP_OK) {
                result = readStream(connection.getInputStream());
                JSONArray array=new JSONArray(result);
                JSONObject json=array.getJSONObject(0);

                JSONObject jsonAviso=json.getJSONObject("aviso");
                if(jsonAviso!=null) {
                    texto=jsonAviso.getString("texto");
                    titulo=jsonAviso.getString("nombre");
                    if(texto!=null) {
                        if(consumidor!=null) {
                            consumidor.accept(texto);
                        } else {
                            mostrarAviso(context, titulo, texto);
                        }
                    }
                }
            }

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void mostrarAviso(Context context, String titulo, String texto) {
        if(texto!=null && texto.contains("No hay aviso")) {
            return;
        }
        Handler handler = new Handler(Looper.getMainLooper());
        if(texto!=null) {
            handler.post(() -> {
                new AlertDialog.Builder(context)
                        .setTitle(titulo)
                        .setMessage(texto)
                        .setPositiveButton("Entendido", null )
                        .show();
            });
        }

    }

    private String readStream(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        // Usamos try-with-resources para asegurar que el lector se cierre automáticamente
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, "ISO-8859-1"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        return builder.toString();
    }

    public static void setAvisosAemet(boolean valor) {
        avisosAemet=valor;
    }
}
