package miravalles.tumareapro;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

public class TablaMareas extends Activity {
	
	private static final long MILIS_DIA=1000*60*60*24;
	
	private static SimpleDateFormat sdf=new SimpleDateFormat("EEE, dd MMM");

	Sizer sizer=new Sizer();

	Typeface fontAwesome;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tabla_mareas);
		fontAwesome=ResourcesCompat.getFont(this, R.font.fontawesome);
		cargar();
	}


	
	private void cargar() {
		Date fechaVista=new Date(getIntent().getExtras().getLong("fecha"));
		int posicion=getIntent().getExtras().getInt("posicion",0);
		
		TableLayout tabla=(TableLayout)findViewById(R.id.tabla);
		GregorianCalendar gc=Modelo.get().utcCalendar();
		gc.setTime(fechaVista);
		gc.set(gc.HOUR_OF_DAY,0);
		gc.set(gc.MINUTE,0);
		gc.set(gc.SECOND,0);

		Date desde=gc.getTime();
		
		boolean par=false;
		for(long i=0; i<30 * MILIS_DIA; i+=MILIS_DIA) {
			long time=desde.getTime()+i;
			Date fecha=new Date(time);
			if(!Modelo.get().existeFecha(fecha)) {
				break;
			}			
			TableRow fila=new TableRow(this);
			fila.setWeightSum(100f);
			if(par) {
				fila.setBackgroundColor(0xFF002244);
			} else {
				fila.setBackgroundColor(0xFF003355);
			}
			tabla.addView(fila);
			
			TableRow.LayoutParams tlp=new TableRow.LayoutParams();
			tlp.width=0;
			tlp.weight=20;
			
			TextView tvFecha=new TextView(this);
			tvFecha.setText(sdf.format(fecha));
			tvFecha.setTextColor(0xFFFFFFFF);
			tvFecha.setPadding(10,0,20,0);
			tvFecha.setTextSize(16);
			tvFecha.setGravity(Gravity.CENTER);
			fila.addView(tvFecha);
			par=!par;
			tvFecha.setLayoutParams(tlp);

			TableRow.LayoutParams tll=new TableRow.LayoutParams();
			tll.width=0;
			tll.weight=80;		
			LinearLayout ll=new LinearLayout(this);
			ll.setOrientation(LinearLayout.VERTICAL);			
			ll.setLayoutParams(tll);
			fila.addView(ll);
			MareaInfo info=Modelo.get().getMareaInfo(posicion, fecha);
			while(info.siguiente.getTime() < time + MILIS_DIA) {
				imprimirDatosMarea(ll, info);
				Date sig=new Date(info.siguiente.getTime()+1L);
				if(!Modelo.get().existeFecha(sig)) {
					return;
				}
				info=Modelo.get().getMareaInfo(posicion, sig);
				if(info.siguiente.getTime() <= sig.getTime()) {
					break;
				}
			}
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

	private int imprimirNombre(LinearLayout ll, MareaInfo info) {
		int peso=10;
		TextView campo=newColumn(ll, peso);
		campo.setTypeface(fontAwesome);
		if(info.alturaSiguiente>info.alturaAnterior) {
			campo.setTextColor(0xFFAA2222);
			campo.setText("\uf062");
		} else {
			campo.setTextColor(0xFF22AA22);
			campo.setText("\uf063");
		}
		return peso;
	}

	private int imprimirHora(LinearLayout ll, MareaInfo info) {
		int peso=25;
		TextView campo=newColumn(ll,peso);
		campo.setText(info.getHoraSiguiente());
		return peso;
	}

	private int imprimirAltura(LinearLayout ll, MareaInfo info) {
		int peso=15;
		TextView campo=newColumn(ll, peso);
		campo.setText(info.getAlturaSiguiente());
		return peso;
	}

	private int imprimirCoeficiente(LinearLayout ll, MareaInfo info) {
		int peso=15;
		TextView campo=newColumn(ll, 15);
		campo.setTextAlignment(TextView.TEXT_ALIGNMENT_TEXT_END);
		campo.setText(Integer.toString(info.coeficiente));
		return peso;
	}

	private int getBackgroundColor(MareaInfo info) {
		int backgroundColor;
		if(info.alturaAnterior>info.alturaSiguiente) {
			backgroundColor=Estilo.FONDO_BAJAMAR_TABLA;
		} else {
			backgroundColor=Estilo.FONDO_PLEAMAR_TABLA;
		}
		return backgroundColor;
	}

	private void imprimirDatosMarea(LinearLayout ll, MareaInfo info) {
		LinearLayout cols=new LinearLayout(this);
		cols.setOrientation(LinearLayout.HORIZONTAL);
		cols.setWeightSum(100);
		cols.setBackgroundColor(getBackgroundColor(info));
		ll.addView(cols);

		int peso=0;
		peso+=imprimirNombre(cols, info);
		peso+=imprimirHora(cols, info);
		peso+=imprimirAltura(cols, info);
		peso+=imprimirCoeficiente(cols, info);

		LayoutParams aguaLp=new LayoutParams(0, LayoutParams.MATCH_PARENT);
		aguaLp.weight=100 - peso;

		AguaTabla agua=new AguaTabla(this);
		if(info.alturaAnterior>info.alturaSiguiente) {
			agua.setBajamar(true);
		}
		agua.setColor(0xFF0044AA);
		agua.setBackgroundColor(0xFF000044);
		agua.setLayoutParams(aguaLp);
		agua.setMax(Config.maxAltura());
		agua.setMin(0);
		agua.setAltura(info.alturaSiguiente);
		agua.setPadding(1,1,1,1);
		cols.addView(agua);
	}
	
	
}
