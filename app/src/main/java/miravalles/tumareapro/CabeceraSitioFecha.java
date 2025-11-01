package miravalles.tumareapro;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CabeceraSitioFecha {

    Button botonSitio;
    Button botonFecha;

    Sizer sizer=new Sizer();

    SimpleDateFormat formatoFecha=new SimpleDateFormat("dd-MMM-yyyy");


    public View crear(Context context, Runnable elegirSitio, Runnable elegirFecha) {
        LinearLayout ll=new LinearLayout(context);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        sizer.set(ll).fillHeight().fillWidth();

        botonSitio=crearBoton(context);
        ll.addView(botonSitio);
        botonSitio.setOnClickListener( v -> elegirSitio.run());
        botonSitio.setTextAlignment(Button.TEXT_ALIGNMENT_VIEW_START);

        botonFecha=crearBoton(context);
        ll.addView(botonFecha);
        botonFecha.setOnClickListener( v -> elegirFecha.run());
        botonFecha.setTextAlignment(Button.TEXT_ALIGNMENT_TEXT_START);

        return ll;
    }

    private Button crearBoton(Context context) {
        Button b=new Button(context);
        b.setTextColor(Color.WHITE);
        b.setTextSize(20);
        b.setPadding(30,20,30,20);
        b.setAllCaps(false);
        b.setBackgroundColor(Color.TRANSPARENT);
        sizer.set(b).pctWidth(50).fillHeight();
        return b;
    }

    public void setSitio(String sitio) {
        botonSitio.setText(sitio);
    }

    public void setFecha(Date fecha) {
        botonFecha.setText(formatoFecha.format(fecha));
    }

}
