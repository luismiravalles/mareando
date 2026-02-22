package miravalles.tumareapro;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import java.io.File;

public class TuMareaApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Modelo.crearModelo(this, getDirectorioImagenes());
        cargarPreferencias();
    }

    public File getDirectorioImagenes() {
        File directorio= Environment.getExternalStorageDirectory();
        if(directorio!=null) {
            directorio=new File(directorio, "tumarea");
            directorio.mkdir();
        }  else {
            directorio=getCacheDir();
        }
        return directorio;
    }

    public void cargarPreferencias() {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        boolean pies=prefs.getBoolean("pies", false);
        MareaInfo.pies=pies;

        AvisadorAemet.setAvisosAemet(prefs.getBoolean("avisosAemet", true));

    }
}
