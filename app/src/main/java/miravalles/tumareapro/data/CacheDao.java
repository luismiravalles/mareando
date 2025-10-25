package miravalles.tumareapro.data;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.function.Function;

/**
 * Una clase que facilita la carga de datos a partir de ficheros o servicios remotos
 * que cachearemos sobre un fichero local.
 */
public class CacheDao<T> {

    Context context;

    String prefijoLocal;

    Function<T, String> compositorUrl;

    public CacheDao(Context context, String prefijoLocal, Function<T, String> compositorUrl) {
        this.context=context;
        this.prefijoLocal=prefijoLocal;
        this.compositorUrl=compositorUrl;
    }

    private boolean hayDatosLocales(T arg) {
        File fichero=getFileLocal(arg);
        return fichero.exists() && fichero.length()>0;
    }

    public File getFileLocal(T arg) {
        String nombreLocal=componerNombreLocal(arg);
        return new File(context.getFilesDir(), nombreLocal);
    }

    public void descargarSiEsNecesario(T arg) {
        if(!hayDatosLocales(arg)) {
            descargarDatosRemotos(arg);
        }
    }

    private String componerNombreLocal(T arg) {
        return prefijoLocal + "_" + arg;
    }

    private String componerUrl(T arg) {
        return compositorUrl.apply(arg);
    }

    private void descargarDatosRemotos(T arg) {

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
            return;
        }
        Log.i("X", "Verificada");
        try {
            URL url = new URL(componerUrl(arg));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            File ficheroLocal=getFileLocal(arg);
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
        }
    }

}
