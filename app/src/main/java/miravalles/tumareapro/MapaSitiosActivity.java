package miravalles.tumareapro;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import org.osmdroid.config.Configuration;
import org.osmdroid.library.BuildConfig;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.MapTileIndex;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import miravalles.tumareapro.domain.Sitio;

public class MapaSitiosActivity extends Activity {

    Sizer sizer=new Sizer();

    MapView map;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().setUserAgentValue("MareandoApp (luismiravalles@gmail.com)");

        LinearLayout contenedor=new LinearLayout(this);
        sizer.set(contenedor).fillHeight().fillWidth();


        map=new MapView(this);
        //map.setTileSource(TileSourceFactory.USGS_TOPO);
        map.setMultiTouchControls(true);

        map.setTileSource(new OnlineTileSourceBase(
                "CartoLight",
                0, 19, 256, "",
                new String[] {
                        "https://a.basemaps.cartocdn.com/light_all/",
                        "https://b.basemaps.cartocdn.com/light_all/",
                        "https://c.basemaps.cartocdn.com/light_all/"
                }) {
            @Override
            public String getTileURLString(long pMapTileIndex) {
                return getBaseUrl()
                        + MapTileIndex.getZoom(pMapTileIndex) + "/"
                        + MapTileIndex.getX(pMapTileIndex) + "/"
                        + MapTileIndex.getY(pMapTileIndex) + ".png";
            }
        });

        sizer.set(map).pctHeight(90).fillWidth();

        int indiceSitio=(Integer)getIntent().getExtras().getInt("indiceSitio");


        for(int i=0; i<Modelo.get().getNumSitios(); i++) {
            Sitio sitio=Modelo.get().getSitio(i);
            Marker marker=new Marker(map);
            GeoPoint punto=new GeoPoint(sitio.getGeo().latitud(), sitio.getGeo().longitud());
            marker.setTitle(sitio.nombre);
            marker.setPosition(punto);
            marker.setId(Integer.toString(i));
            map.getOverlays().add(marker);
            marker.setOnMarkerClickListener((m,v) -> {
                mostrarDialogoPunto(m);
                return true;
            });

        }



        Sitio actual=Modelo.get().getSitio(indiceSitio);
        GeoPoint punto=new GeoPoint(actual.getGeo().latitud(),actual.getGeo().longitud());
        map.getController().setZoom(9f);
        map.getController().setCenter(punto);

        contenedor.addView(map);
        this.setContentView(contenedor);
    }


    private void mostrarDialogoPunto(Marker marker) {
        new AlertDialog.Builder(this)
                .setTitle(marker.getTitle())
                .setMessage(marker.getSnippet())
                .setPositiveButton("Seleccionar", (dialog,wich) -> {
                    devolverResultado(marker);
                })
                .setNegativeButton("Cerrar", null)
                .show();
    }

    private void devolverResultado(Marker marker) {
        Intent data = new Intent();
        data.putExtra("indiceSitio", Integer.parseInt(marker.getId()));
        // TODO: Poner el resultado.
        setResult(RESULT_OK, data);
        finish();
    }
}
