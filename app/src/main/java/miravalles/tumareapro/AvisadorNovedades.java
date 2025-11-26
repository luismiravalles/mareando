package miravalles.tumareapro;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.WebView;

import org.osmdroid.library.BuildConfig;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import miravalles.tumareapro.domain.Configuracion;

public class AvisadorNovedades {


    private static final String PREFS_NAME = "AppConfig";
    private static final String KEY_LAST_MODIFIED = "last_modified_date";


    public String getUrl(Context context) {
        String appName=context.getString(R.string.flavor);
        return Configuracion.getUrlMacetero("aviso-" + appName +  ".html");
    }

    public void verificarAviso(Context context) {


        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                Log.i("avisador", "Conectando con " + getUrl(context));
                URL url = new URL(getUrl(context));
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // IMPORTANTE: Primero solo pedimos HEAD para no descargar todo si no es necesario
                // Aunque para archivos pequeños, un GET directo y verificar fecha es aceptable.
                // Aquí haremos GET para obtener la fecha y el contenido si hace falta.
                connection.setRequestMethod("GET");
                connection.connect();

                // 1. Obtenemos la fecha del archivo en el servidor (en milisegundos)
                long serverLastModified = connection.getLastModified();

                // 2. Obtenemos la fecha guardada en la app (0 si es la primera vez)
                SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                long localLastModified = prefs.getLong(KEY_LAST_MODIFIED, 0);
                Log.i("Avisador", "Comparando: " + serverLastModified + " <=> " + localLastModified);
                // 3. Comparamos
                if (serverLastModified > localLastModified) {

                    // 4. Volvemos al hilo principal para mostrar la alerta
                    handler.post(() -> {
                        mostrarDialogoHTML(context, getUrl(context));

                        // 5. Guardamos la nueva fecha para que no salga la próxima vez
                        prefs.edit().putLong(KEY_LAST_MODIFIED, serverLastModified).apply();
                    });
                } else {
                    // El archivo no ha cambiado, no hacemos nada
                }

            } catch (Exception e) {
                e.printStackTrace();
                // Manejar error de conexión silenciosamente o con log
            }
        });
    }

    private void mostrarDialogoHTML(Context context, String url) {
        // Usamos un WebView para renderizar el HTML correctamente
        WebView webView = new WebView(context);
        webView.clearCache(true);
        webView.loadUrl(url);

        new AlertDialog.Builder(context)
                .setTitle("Novedades")
                .setView(webView) // Insertamos el WebView en el diálogo
                .setPositiveButton("Entendido", (dialog, which) -> {
                    }
                )
                .setCancelable(false) // Obligar al usuario a leer y cerrar
                .show();
    }
}

