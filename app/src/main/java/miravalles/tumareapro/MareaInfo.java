package miravalles.tumareapro;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import miravalles.tumareapro.vo.GeoLocalizacion;
import miravalles.tumareapro.R;

import android.util.Log;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;

public class MareaInfo {	
	private static final double CON_PIES_A_METROS = 3.2808;
	public	Date siguiente;	// Si son nulos es que no hay datos para esta fecha
	public  Date anterior;
	public  int  alturaSiguiente;
	public  int  alturaAnterior;
	public 	Date hora;
	public  int  coeficiente;
	
	public Calendar orto;
	public Calendar ocaso;
	
	

	private static SimpleDateFormat sd=new SimpleDateFormat("HH:mm");
	
	public static boolean pies=false;
	
	public MareaInfo(Date hora, GeoLocalizacion geo) {
		this.hora=hora;
		Calendar cal=Calendar.getInstance();
		cal.setTime(hora);
		Log.i("X", "Latitud=" + geo.y());
		Log.i("X", "Longitud=" + geo.x());
		this.orto=SunriseSunsetCalculator.getSunrise(geo.y(), geo.x(), 
				TimeZone.getDefault(), cal, 0.0);
		this.ocaso=SunriseSunsetCalculator.getSunset(geo.y(), geo.x(), 
				TimeZone.getDefault(), cal, 0.0);
	}

	public int getMes() {
		Calendar cal=Calendar.getInstance();
		cal.setTime(hora);
		return cal.get(Calendar.MONTH);
	}
	
	public int getNombreProximo() {
		if(alturaSiguiente>alturaAnterior) {
			return R.string.pleamar;
		} else {
			return R.string.bajamar;
		}
		
	}
	
	public int getNombreAnterior() {
		if(alturaSiguiente>alturaAnterior) {
			return R.string.bajamar;
		} else {
			return R.string.pleamar;
		}	
	}	
	
	public String getHoraSiguiente() {
		if(siguiente==null) {
			return "";
		} else {
			return sd.format(siguiente);
		}		
	}
	
	public String getHoraAnterior() {
		if(anterior==null) {
			return ""; 
		} else {
			return sd.format(anterior);
		}
	}
	
	public String getHora() {
		return sd.format(hora);
	}
	
	private static DecimalFormat df=new DecimalFormat("0,00");

	public String getMinimo() {
		int min=Math.min(alturaSiguiente, alturaAnterior);
		return getStringAltura(min);
	}
	
	public int getIntMaximo() {
		return Math.max(alturaSiguiente, alturaAnterior);
	}
	
	public int getIntMinimo() {
		return Math.min(alturaSiguiente, alturaAnterior);
	}

	
	public String getMaximo() {
		int max=Math.max(alturaSiguiente, alturaAnterior);
		return getStringAltura(max);
	}	
	
	public int getIconoEstado() {
		if(alturaSiguiente>alturaAnterior) {
			return R.drawable.subiendo;
		} else {
			return R.drawable.bajando;
		}
	}
	
	public boolean estaSubiendo() {
		 return alturaSiguiente>alturaAnterior;
	}
	
	public int getEstado() {
		if(alturaSiguiente>alturaAnterior) {
			return R.string.subiendo;
		} else {
			return R.string.bajando;
		}
	}
	
	public String getVerboEstado() {
		if(alturaSiguiente>alturaAnterior) {
			return "subir";
		} else {
			return "bajar";
		}
	}	
	
	public int getPct() {
		int max=Math.max(alturaSiguiente, alturaAnterior);
		int min=Math.min(alturaSiguiente, alturaAnterior);
		int act=getAltura();
		if(max-min==0) {
			return 0;
			// throw new IllegalStateException("Algo falla en los datos: max=" + max + " min=" + min );
		}
		return 100 * (act-min) / (max-min);
	}
	
	public int getAltura() {
		if(siguiente==null) {
			return 0;
		}
		double sig=siguiente.getTime();
		double ant=anterior.getTime();
		double act=hora.getTime();
		double ratio = (act - ant) / (sig - ant);  //Cociente de 0 a 1 de tiempo que lleva subiendo o bajando

		double incr = (((double)alturaSiguiente - (double)alturaAnterior)/2)
							*(1-Math.cos(ratio*Math.PI));

		/*
		Log.d("M", "Calculando Altura actual "
								+ " Sig = " + siguiente
								+ " Ant = " + anterior
								+ " act = " + hora.getTime()
								+ " Ratio = " + ratio
								+ " Inc = " + incr);
		*/
		return alturaAnterior + (int)incr;
	}
	
	public String getAlturaPendiente() {
		return getStringAltura(
				Math.abs(
				alturaSiguiente - getAltura()));
	}
	
	public String getStringAltura() {
		return getStringAltura(getAltura());		
	}
	
	public String getAlturaSiguiente() {
		return getStringAltura(alturaSiguiente);		
	}
	
	public int getIntAlturaSiguiente() {
		return alturaSiguiente;
	}
	
	public int getIntAlturaAnterior() {
		return alturaAnterior;
	}
	
	
	public String getStringAltura(int altura) {
		String sufijo="m";
		if(pies) {
			sufijo="ft";
			altura=(int)(altura*CON_PIES_A_METROS);
		}
		return df.format(altura) + sufijo;
	}
	
	public int getEdadLunar() {
		return Modelo.getEdadLuna(hora);
	}
	
	// Luz en porcentaje de 0 a 100.
	public int getLuz() {
		int minuto=hora.getHours() * 60 + hora.getMinutes();
		
		int minutoOrto=orto.get(Calendar.HOUR_OF_DAY)*60 + orto.get(Calendar.MINUTE);
		int minutoOcaso=ocaso.get(Calendar.HOUR_OF_DAY)*60 + ocaso.get(Calendar.MINUTE);
		
//		Log.i("X", "minuto=" + minuto);
//		Log.i("X", "minutoOcaso=" + minutoOcaso);
		
		if(minuto < minutoOrto-50) {
			return 0;
		} else if(minuto < minutoOrto+50) {
			return minuto - (minutoOrto -50);
		} else if(minuto < minutoOcaso-50) {
			return 100;
		} else if(minuto < minutoOcaso+50) {
			return  minutoOcaso+50 - minuto;
		} else {
			return 0;
		}
		
	}

	/**
	 * Retorna cierto si esta instancia realmente contiene datos.
	 * No continene datos si las alturas anterior y siguiente son cero.
	 * @return
	 */
	public boolean hayDatos() {
		 return this.alturaAnterior!=0 && this.alturaSiguiente!=0;
	}
	
}
