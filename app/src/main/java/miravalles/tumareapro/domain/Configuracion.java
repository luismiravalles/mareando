package miravalles.tumareapro.domain;

import android.app.Activity;
import android.util.Log;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.HashMap;
import java.util.Map;

public class Configuracion {

    private static final String URL_MACETERO = "https://tumarea.macetero.org/";

    private static final String URL_INSTITUTO = "https://ideihm.covam.es/api-ihm/getmarea?request=gettide&id=${id}&format=${format}&month=${month}";

    private static final String PROP_URL_MACETERO ="url_macetero";
    private static final String PROP_URL_INSTITUTO="url_instituto";

    private static FirebaseRemoteConfig mFirebaseRemoteConfig;

    public static String getUrlMacetero(String path)  {
        return mFirebaseRemoteConfig.getString(PROP_URL_MACETERO) + path;
    }

    public static String getUrlInstituto() {
        return mFirebaseRemoteConfig.getString(PROP_URL_INSTITUTO);
        // return "https://patatin.macetero.org/datos/patatan";
        // return "https://tumarea.macetero.org/datos/${id}/${month}.xml";
    }

    public static void setupRemoteConfig() {
        // 1. Obtener la instancia
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        // 2. Configurar ajustes (solo para desarrollo/pruebas)
        // En producción, el intervalo de caché es más largo (ej. 12 horas).
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                // Permite múltiples peticiones por hora para pruebas rápidas
                .setMinimumFetchIntervalInSeconds(3600) // 1 hora
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);

        // 3. Definir valores predeterminados locales
        // Si Firebase no está disponible, este valor será usado.
        Map<String, Object> defaultMap = new HashMap<>();
        defaultMap.put(PROP_URL_MACETERO, URL_MACETERO);
        defaultMap.put(PROP_URL_INSTITUTO, URL_INSTITUTO);
        mFirebaseRemoteConfig.setDefaultsAsync(defaultMap);
    }

    public static void fetchConfig(Activity activity) {
        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        boolean updated = task.getResult();
                        if (updated) {
                            Log.i("Conf", "Conf remota actualizada y activada");
                        } else {
                            Log.i("Conf", "Conf remota obtenida del caché.");
                        }
                    } else {
                        Log.i("Conf", "Error al obtener conf remota, se usan valores x defecto.");
                    }
                });
    }
}
