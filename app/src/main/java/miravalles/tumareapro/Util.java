package miravalles.tumareapro;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;

public final class Util {
	

	/**
	 * Retorna Pixels equivalentes a DP. 
	 */
	public static int dp(int v, View raiz) {
		return (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_SP, v, 
				raiz.getContext().getResources().getDisplayMetrics());		
	} 
	
	/**
	 * Retorna en pixels un porcentaje respecto al ancho de la pantalla.
	 * 
	 * Por ejemplo, si v=100 quiere decir el ancho completo.
	 * 
	 */
	public static int widthPct(int v, View raiz) {
		return v *
		raiz.getContext().getResources().getDisplayMetrics().widthPixels
		/ 100;	
	}
	
	public static int heightPct(int v, View raiz) {
		return v *
		raiz.getContext().getResources().getDisplayMetrics().heightPixels
		/ 100;		
	}
	
	public static LinearLayout.LayoutParams setLayout(View v, int x, int y) {
		LinearLayout.LayoutParams ll=new LinearLayout.LayoutParams(x, y);
		v.setLayoutParams(ll);
		return ll;
	}
	
	/*
	public static <T extends LayoutParams> T xFill() {
		
	}
	*/

	public static void mostrarAviso(Activity raiz, String aviso) {
		Intent myIntent = new Intent(raiz,AvisoActivity.class);		
		myIntent.putExtra("aviso", aviso);
		raiz.startActivity(myIntent);    	
	}
	
	
	private static long anterior;
	public static long lapso() {
		long result=0;
		if(anterior!=0) {
			result=System.currentTimeMillis()-anterior;
		}
		anterior=System.currentTimeMillis();
		return result;
	}
	
	public static String capitalize(String origen) {
		return origen.substring(0,1).toUpperCase() + origen.substring(1);
	}
	
	
	public static boolean isManana(Date fecha) {
		Date ahora=new Date();
		GregorianCalendar gcFecha=new GregorianCalendar();
		gcFecha.setTime(fecha);
		GregorianCalendar gcAhora=new GregorianCalendar();
		ahora.setTime(ahora.getTime()+24*60*60*1000);
		gcAhora.setTime(ahora);
		
		if(gcAhora.get(Calendar.DAY_OF_MONTH)==gcFecha.get(Calendar.DAY_OF_MONTH) &&
			gcAhora.get(Calendar.MONTH)==gcFecha.get(Calendar.MONTH)&&
			gcAhora.get(Calendar.YEAR)==gcFecha.get(Calendar.YEAR)) {
		
			Log.i("D", "Ma�ana: " + fecha);
			return true;
		} else {
			return false;
		}
	}
	
	private static TimeZone spainTimezone=TimeZone.getTimeZone("Europe/Madrid");
	private static TimeZone canariasTimezone=TimeZone.getTimeZone("Atlantic/Canary");
	
//	public static TimeZone getTimeZone(GeoLocalizacion geo) {
//		if(geo.x() < -12.0 ) {
//			Log.i("T", "TimeZone de Canarias: " + canariasTimezone);
//			return canariasTimezone;
//		} else {
//			Log.i("T", "TimeZone de Madrid: " + canariasTimezone);
//			return spainTimezone;
//		}
//	}
//	
//	
//	public static GregorianCalendar calendar(GeoLocalizacion geo) {
//		return new GregorianCalendar(getTimeZone(geo));
//	}

	public static int thisYear() {
		return Calendar.getInstance().get(Calendar.YEAR);
	}

	/**
	 * Mes actual en base cero.
	 * @return
	 */
	public static int thisMonth() {
		return Calendar.getInstance().get(Calendar.MONTH);
	}
	
}

