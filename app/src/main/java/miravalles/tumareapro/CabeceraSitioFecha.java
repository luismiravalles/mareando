package miravalles.tumareapro;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatButton;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CabeceraSitioFecha {

    AppCompatButton botonSitio;
    AppCompatButton botonFecha;

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
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        botonSitio.setBackgroundResource(outValue.resourceId);

        // Auto size
        //botonSitio.setAutoSizeTextTypeUniformWithConfiguration(
        //       10,20, 1,
        //        TypedValue.COMPLEX_UNIT_SP );


        botonFecha=crearBoton(context);
        ll.addView(botonFecha);
        botonFecha.setOnClickListener( v -> elegirFecha.run());
        botonFecha.setTextAlignment(Button.TEXT_ALIGNMENT_TEXT_START);
        outValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        botonFecha.setBackgroundResource(outValue.resourceId);


        return ll;
    }

    private AppCompatButton crearBoton(Context context) {
        AppCompatButton b=new AppCompatButton(context);
        b.setTextColor(Color.WHITE);
        b.setTextSize(16);
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
