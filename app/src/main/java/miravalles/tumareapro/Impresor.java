package miravalles.tumareapro;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;

public class Impresor {

    Context context;

    public Impresor(Context context) {
        this.context=context;
    }

    private void abrirArchivoDesdeDownloads(Context context, String nombreFichero) {
        // 1. Obtener la referencia al archivo en la carpeta pública
        File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(downloadsFolder, nombreFichero);

        if (file.exists()) {
            // 2. Crear la URI segura
            // Asegúrate de que el authority coincida con el de tu AndroidManifest
            Uri uri = FileProvider.getUriForFile(context,
                    context.getPackageName() + ".fileprovider", file);

            // 3. Crear el Intent de visualización
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // Crucial
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            try {
                context.startActivity(Intent.createChooser(intent, "Selecciona un visor de PDF"));
            } catch (Exception e) {
                // Manejar caso donde el usuario no tenga NINGUNA app de PDF
                Toast.makeText(context, "No hay aplicaciones para abrir PDF", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public  void  imprimir(final String puerto, final String year, String url) {
        Log.i("IMP", "Descargando archivo");
        DownloadManager.Request request=new DownloadManager.Request(Uri.parse(url));
        request.setTitle("Descargando Calendario");
        request.setDescription("Calendario");
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        final String nombreFichero="Calendario-" + year + "-" + System.currentTimeMillis() + ".pdf";
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                nombreFichero);
        DownloadManager manager=(DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
        final long downloadId=manager.enqueue(request);

        BroadcastReceiver onComplete = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (id == downloadId) {
                    Log.i("IMP", "Abriendo archivo " + nombreFichero);

                    abrirArchivoDesdeDownloads(context, nombreFichero);
                    context.unregisterReceiver(this); // Importante para no dejar fugas de memoria
                }
            }
        };

        ContextCompat.registerReceiver(
                context,
                onComplete,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                ContextCompat.RECEIVER_EXPORTED);
    }

}
