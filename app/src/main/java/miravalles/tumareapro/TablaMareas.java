package miravalles.tumareapro;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import android.app.Activity;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.WindowCompat;

import miravalles.tumareapro.vo.Marea;

public class TablaMareas extends AppCompatActivity {

	private static SimpleDateFormat sdf=new SimpleDateFormat("EEE, dd MMM");

	Typeface fontAwesome;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Según ChatGpt esto evita que machaquemos la barra de estado de arriba.
		WindowCompat.setDecorFitsSystemWindows(getWindow(), true);

		setContentView(R.layout.tabla_mareas);
		fontAwesome=ResourcesCompat.getFont(this, R.font.fontawesome);


		cargarTabla();
	}

	private void cargarTabla() {
		Log.i("TM", "Cargando tabla...");
		Date fechaVista=new Date(getIntent().getExtras().getLong("fecha"));
		int posicion=getIntent().getExtras().getInt("posicion",0);

		TableLayout tabla=(TableLayout)findViewById(R.id.tabla);
		GregorianCalendar gc=Modelo.get().utcCalendar();
		gc.setTime(fechaVista);
		gc.set(gc.HOUR_OF_DAY,0);
		gc.set(gc.MINUTE,0);
		gc.set(gc.SECOND,0);
		gc.set(GregorianCalendar.MILLISECOND, 0);

		Date desde=gc.getTime();

		boolean par=false;

		for(int i=0; i<30; i++) {
			cargarDia(tabla, posicion, gc, par);
			gc.add(Calendar.DAY_OF_MONTH, 1);
			par=!par;
		}
	}


	private void cargarDia(TableLayout tabla,int posicion, GregorianCalendar dia, boolean par) {
		Log.i("TM", "Cargando día " + dia.getTime());
		TableRow fila=new TableRow(this);
		fila.setWeightSum(100f);
		tabla.addView(fila);
		if(par) {
			fila.setBackgroundColor(0xFF002244);
		} else {
			fila.setBackgroundColor(0xFF003355);
		}

			TableRow.LayoutParams tlp=new TableRow.LayoutParams();
		tlp.width=0;
		tlp.weight=20;


		TextView tvFecha=new TextView(this);
		tvFecha.setText(sdf.format(dia.getTime()));
		tvFecha.setTextColor(0xFFFFFFFF);
		tvFecha.setPadding(10,0,20,0);
		tvFecha.setTextSize(16);
		tvFecha.setGravity(Gravity.CENTER);
		fila.addView(tvFecha);
		tvFecha.setLayoutParams(tlp);

		TableRow.LayoutParams tll=new TableRow.LayoutParams();
		tll.width=0;
		tll.weight=80;
		LinearLayout ll=new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.setLayoutParams(tll);
		fila.addView(ll);

		int mes=dia.get(Calendar.MONTH);
		int diaBaseCero=dia.get(Calendar.DAY_OF_MONTH)-1;

		List< Marea> mareas=Modelo.get().getTablaMareas(posicion, mes, diaBaseCero);
		for(Marea marea:mareas) {
			imprimirDatosMarea(ll, marea);

		}
	}


	private TextView newColumn(LinearLayout ll, int peso) {
		TextView campo=new TextView(this);
		campo.setTypeface(Typeface.DEFAULT_BOLD);
		campo.setPadding(8, 12, 8, 12);
		LayoutParams campoLp=new LayoutParams(0, LayoutParams.WRAP_CONTENT);
		campoLp.weight=peso;
		campo.setLayoutParams(campoLp);
		ll.addView(campo);
		return campo;
	}

	private ImageView newImageColumn(LinearLayout ll, int peso) {
		ImageView campo=new ImageView(this);
		campo.setPadding(8, 12, 8, 12);
		LayoutParams campoLp=new LayoutParams(0, LayoutParams.WRAP_CONTENT);
		campoLp.weight=peso;
		campo.setLayoutParams(campoLp);
		ll.addView(campo);
		return campo;
	}

	private int imprimirNombre(LinearLayout ll, Marea marea) {
		int peso=10;
		TextView campo=newColumn(ll, peso);
		campo.setTypeface(fontAwesome);
		if(marea.isPleamar()) {
			campo.setTextColor(0xFFAA2222);
			campo.setText("\uf062");
		} else {
			campo.setTextColor(0xFF22AA22);
			campo.setText("\uf063");
		}
		return peso;
	}

	private int imprimirLuna(LinearLayout ll, Marea marea) {
		int peso=20;
		ImageView imageView=newImageColumn(ll, peso);
		try {
			InputStream is = this.getAssets().open("luna/luna-" +
					marea.getEdadLunar() + ".png");
			Drawable d = Drawable.createFromStream(is, null);
			imageView.setImageDrawable(d);
		} catch(IOException e) {
			Log.e("TablaMareas", "Error leyendo Luna " + e);
		}
		return peso;

	}

	private int imprimirHora(LinearLayout ll, Marea marea) {
		int peso=20;
		TextView campo=newColumn(ll,peso);
		campo.setText(marea.getHoraFormateada());
		return peso;
	}

	private int imprimirAltura(LinearLayout ll, Marea marea) {
		int peso=20;
		TextView campo=newColumn(ll, peso);
		campo.setText(marea.getAlturaFormateada());
		return peso;
	}

	private int imprimirCoeficiente(LinearLayout ll, Marea marea) {
		int peso=10;
		TextView campo=newColumn(ll, 15);
		campo.setTextAlignment(TextView.TEXT_ALIGNMENT_TEXT_END);
		if(marea.getCoeficiente()>0) {
			campo.setText(Integer.toString(marea.getCoeficiente()));
		}
		return peso;
	}

	private int getBackgroundColor(Marea marea) {
		return marea.isPleamar()?Estilo.FONDO_PLEAMAR_TABLA:Estilo.FONDO_BAJAMAR_TABLA;
	}

	private void imprimirDatosMarea(LinearLayout ll, Marea marea) {
		Log.i("TM", "Imprimir Datos Marea: " + marea);
		LinearLayout cols=new LinearLayout(this);
		cols.setOrientation(LinearLayout.HORIZONTAL);
		cols.setWeightSum(100);
		cols.setBackgroundColor(getBackgroundColor(marea));
		ll.addView(cols);

		int peso=0;
		peso+=imprimirNombre(cols, marea);
		peso+=imprimirHora(cols, marea);
		peso+=imprimirAltura(cols, marea);
		peso+=imprimirCoeficiente(cols, marea);
		peso+=imprimirLuna(cols, marea);

		LayoutParams aguaLp=new LayoutParams(0, LayoutParams.MATCH_PARENT);
		aguaLp.weight=100 - peso;

		AguaTabla agua=new AguaTabla(this);
			agua.setBajamar(!marea.isPleamar());
		agua.setColor(0xFF0044AA);
		agua.setBackgroundColor(0xFF000044);
		agua.setLayoutParams(aguaLp);
		agua.setMax(Config.maxAltura());
		agua.setMin(0);
		agua.setAltura(marea.getAltura());
		agua.setPadding(1,1,1,1);
		cols.addView(agua);
	}
	
	
}
