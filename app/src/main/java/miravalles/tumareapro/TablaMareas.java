package miravalles.tumareapro;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class TablaMareas extends Activity {
	
	private static final long MILIS_DIA=1000*60*60*24;
	
	private static SimpleDateFormat sdf=new SimpleDateFormat("EEE, dd MMM");
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tabla_mareas);
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
				LinearLayout cols=new LinearLayout(this);
				cols.setOrientation(LinearLayout.HORIZONTAL);
				cols.setWeightSum(100);
				ll.addView(cols);
				
				LayoutParams campoLp=
						new LayoutParams(
								0, LayoutParams.WRAP_CONTENT
								);
				campoLp.weight=80;
				TextView campo=new TextView(this);
				campo.setTextColor(0xFFFFFFFF);
				campo.setText(
						getString(info.getNombreProximo()) + "    "
						+ info.getHoraSiguiente() + "    "
						+ info.coeficiente + "  "
						+ info.getAlturaSiguiente() );
				int backgroundColor;
				if(info.alturaAnterior>info.alturaSiguiente) {
					backgroundColor=Estilo.FONDO_BAJAMAR_TABLA;
				} else {
					backgroundColor=Estilo.FONDO_PLEAMAR_TABLA;
				}
				cols.setBackgroundColor(backgroundColor);
				campo.setPadding(4, 2, 0, 2);
				campo.setLayoutParams(campoLp);
				cols.addView(campo);
				
				LayoutParams aguaLp=
						new LayoutParams(
								0, LayoutParams.FILL_PARENT
								);
				aguaLp.weight=20;
				
				AguaTabla agua=new AguaTabla(this);
				if(info.alturaAnterior>info.alturaSiguiente) {
					agua.setBajamar(true);
				}
				agua.setColor(0xFF0066FF);
				agua.setBackgroundColor(0xFF000000);
				agua.setLayoutParams(aguaLp);
				agua.setMax(Config.maxAltura());
				agua.setMin(0);
				agua.setAltura(info.alturaSiguiente);
				agua.setPadding(1,1,1,1);
				cols.addView(agua);
				
				
				Date sig=new Date(info.siguiente.getTime()+1L);
				if(!Modelo.get().existeFecha(sig)) {
					return;
				}
				info=Modelo.get().getMareaInfo(posicion, sig);
				if(info.siguiente.getTime() <= sig.getTime()) {
					break;
				}
				Log.i("T", "" + info.siguiente);
			}
		}
	}
	
	
}
