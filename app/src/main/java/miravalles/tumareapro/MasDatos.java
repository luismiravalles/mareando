package miravalles.tumareapro;

import java.util.Date;

import miravalles.tumareapro.domain.AemetInfo;
import miravalles.tumareapro.R;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class MasDatos extends Activity implements AemetListener {
	

	private boolean par=false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.masdatos);
		int posicion=getIntent().getExtras().getInt("posicion",0);
		String codigoAemet= Modelo.get().getSitio(posicion).getCodigoAemet();
		
		if(getPackageName().equals("miravalles.tumareapro")) {
			Aemet.cargar(this, codigoAemet, this);
		} else {			
			comprar();
		}
		getActionBar().setTitle(Modelo.get().getSitio(posicion).nombre);
		getActionBar().setDisplayShowHomeEnabled(false);
	}
	
	public void comprar() {
		TableLayout tabla=(TableLayout)findViewById(R.id.masdatos);
		fila(tabla,"", 
			"Si quieres saber la temperatura del agua, fuerza del viento, oleaje, etc, "
		 +  " compra la versi�n \"Tu Marea Pro\"" );
	}
	
	public void cargado(AemetInfo info) {
		Log.i("A", "Cargado");
		Date fechaVista=new Date(getIntent().getExtras().getLong("fecha"));
		int posicion=getIntent().getExtras().getInt("posicion",0);
		
		TableLayout tabla=(TableLayout)findViewById(R.id.masdatos);

		
		if(info!=null) {
			fila(tabla, "Lugar", info.getNombre());
			fila(tabla, "Temperatura del agua", info.getTemperaturaAgua());						
			fila(tabla, "Estado del cielo", info.getCielo());
			fila(tabla, "Oleaje", info.getOleaje());
			fila(tabla, "Viento", info.getViento());
			fila(tabla, "Fuente", "Aemet");
			fila(tabla, "Fecha", info.getFecha());
		}
		

	}
	
	private TableRow fila(TableLayout tabla, String etiqueta, String valor) {
		TableRow fila=new TableRow(this);		
		if(par) {
			fila.setBackgroundColor(0xFF002244);
		} else {
			fila.setBackgroundColor(0xFF003355);
		}
		TableRow.LayoutParams tlf=new TableRow.LayoutParams();
		tlf.width=TableRow.LayoutParams.FILL_PARENT;
		tlf.height=TableRow.LayoutParams.WRAP_CONTENT;
		fila.setLayoutParams(tlf);
		
		tabla.addView(fila);
				
		LinearLayout ll=new LinearLayout(this);
		ll.setOrientation(LinearLayout.HORIZONTAL);
		fila.addView(ll);
		TableRow.LayoutParams llp=new TableRow.LayoutParams();
		llp.width=TableRow.LayoutParams.MATCH_PARENT;
		llp.height=TableRow.LayoutParams.WRAP_CONTENT;
		ll.setWeightSum(100f);
		ll.setLayoutParams(llp);		
		fila.setPadding(5, 10, 5, 10);
		
		LayoutParams tlp=new LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT,
				50f);

		
		TextView tvEtiqueta=new TextView(this);
		tvEtiqueta.setText(etiqueta);
		tvEtiqueta.setTextColor(0xFFAAAAAA);
		tvEtiqueta.setPadding(10,0,20,0);
		tvEtiqueta.setTextSize(20);
		tvEtiqueta.setLayoutParams(tlp);
		ll.addView(tvEtiqueta);
		
		
		TextView tvValor=new TextView(this);
		tvValor.setText(valor);
		tvValor.setTextColor(0xFFFFFFFF);
		tvValor.setPadding(10,0,20,0);
		tvValor.setTextSize(20);;
		tvValor.setLayoutParams(tlp);
		ll.addView(tvValor);
				
		
		par=!par;
		
		return fila;
	}


	
	
}
