package miravalles.tumareapro;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Map;

import miravalles.tumareapro.domain.AemetInfo;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Xml;

public class Aemet {
	
	
	public static final String BASE_URL="https://www.aemet.es/xml/playas/play_v2_";
	public static final String BASE_URL_MUNICIPIO="https://www.aemet.es/xml/municipios/localidad_";
	public static final String SUFIJO=".xml";
	
	public static Map<String, AemetInfo> cache=new Hashtable<String,AemetInfo>();
	
	
	public static AemetInfo getCache(String codigo) {
		return cache.get(codigo);
	}
	
	public static void cargar(final Context context, final String codigo, final AemetListener listener) {
		AsyncTask<String, Void, String> tarea=new AsyncTask<String, Void, String>() {

			AemetInfo info=null;
			
			@Override
			protected String doInBackground(String... params) {
				info=Aemet.obtenerDatos(context, codigo);
				return null;
			}
			
			@Override
			protected void onPostExecute(String result) {
				listener.cargado(info);
			}
			
		};
		tarea.execute("");
	}
	
	
	private static AemetInfo obtenerDatos(Context context, String codigo) {
		
		if(codigo==null || codigo.length()==0) {
			return null;
		}
		
		if(cache.containsKey(codigo)) {
			return cache.get(codigo);
		}
		
		ConnectivityManager check = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo[] netInfo=check.getAllNetworkInfo();
		boolean conectado=false;
		Log.i("X", "Verificando red...");
		for(int i=0; i<netInfo.length; i++) {
			if(netInfo[i].getState()==NetworkInfo.State.CONNECTED) {
				conectado=true;
				break;
			}
		}
		
		if(conectado==false) {
			return null;
		}
		Log.i("X", "Verificada");
		try {
			AemetInfo info=new AemetInfo();
			URL url=new URL(BASE_URL + codigo + SUFIJO);
			Log.i("AEMET", "Descargando datos Aemet desde " + url);
			HttpURLConnection conn=(HttpURLConnection)url.openConnection();
			conn.connect();
			InputStream is=conn.getInputStream();
			XmlPullParser parser=Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(is, null);
			parser.nextTag();
			parser.require(XmlPullParser.START_TAG, null, "playa");
			Log.i("A","Parseando...");
			int tipo;
			while(( tipo=parser.next()) != XmlPullParser.END_DOCUMENT) {
				String name=parser.getName();
				if(name==null || tipo!=XmlPullParser.START_TAG) {
					continue;
				}
				Log.i("A", "name=" + name);
				if(name.equals("nombre")) {					
					if(parser.next()==XmlPullParser.TEXT){
						Log.i("X", "TEXT de Nombre ");
						info.setNombre(parser.getText());
					}
				}
				if(name.equals("dia") && info.getFecha()==null) {
					info.setFecha(parser.getAttributeValue(null, "fecha"));
				}
				if(name.equals("t_agua") && info.getTemperaturaAgua()==null) {
					info.setTemperaturaAgua(parser.getAttributeValue(null, "valor1"));	
					Log.i("A","Encontrada temperatura agua " + info.getTemperaturaAgua());
				}
				if(name.equals("viento")&& info.getViento()==null) {
					info.setViento(parser.getAttributeValue(null, "descripcion1"));
				}
				if(name.equals("oleaje")&& info.getOleaje()==null) {
					info.setOleaje(parser.getAttributeValue(null, "descripcion1"));
				}
				if(name.equals("estado_cielo")&& info.getCielo()==null) {
					info.setCielo(parser.getAttributeValue(null, "descripcion1"));
				}				
			}
			Log.i("A","Fin parseando...");
			is.close();
			obtenerDatosMunicipio(info, codigo);
			cache.put(codigo, info);
			return info;
			
		} catch (Exception e) {

			Log.e("A",e.toString());
		}
		
		return null;
		
	}
	
	private static void obtenerDatosMunicipio(AemetInfo info, String codigo) throws Exception {
		codigo=codigo.substring(0,5);
		URL url=new URL(BASE_URL_MUNICIPIO + codigo + SUFIJO);
		HttpURLConnection conn=(HttpURLConnection)url.openConnection();
		conn.connect();
		InputStream is=conn.getInputStream();
		XmlPullParser parser=Xml.newPullParser();
		parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
		parser.setInput(is, null);
		parser.nextTag();
		parser.require(XmlPullParser.START_TAG, null, "root");
		Log.i("A","Parseando..." + url);
		int tipo;
		GregorianCalendar dia=new GregorianCalendar();
		GregorianCalendar periodo=null;
		while(( tipo=parser.next()) != XmlPullParser.END_DOCUMENT) {
			String name=parser.getName();
			if(name==null || tipo!=XmlPullParser.START_TAG) {
				continue;
			}


			if(name.equals("dia")) {
				String fecha=parser.getAttributeValue(null,"fecha");
				Log.i("A", "<dia fecha " + fecha);
				String []partes=fecha.split("-");
				dia.set(Calendar.YEAR, Integer.parseInt(partes[0]));
				dia.set(Calendar.MONTH, Integer.parseInt(partes[1]) - 1);
				dia.set(Calendar.DAY_OF_MONTH, Integer.parseInt(partes[2]));
			}
			if(name.equals("viento")) {
				periodo=getPeriodo(dia, parser);
				info.createDatos(periodo);
				Log.i("A", "<viento periodo " + periodo.getTime());
			}
			if(name.equals("direccion")) {
				if(parser.next()==XmlPullParser.TEXT){
					info.getDatos(periodo).setDireccionViento(parser.getText());
					Log.i("A", "<direccion  " + parser.getText());
				}				
			}
			if(name.equals("velocidad")) {
				if(parser.next()==XmlPullParser.TEXT){
					info.getDatos(periodo).setVelocidadViento(parser.getText());
					Log.i("A", "<velocidad  " + parser.getText());
				}				
			}
			if(name.equals("estado_cielo")) {
				periodo=getPeriodo(dia, parser);
				info.createDatos(periodo);
				info.getDatos(periodo).setCielo(parser.getAttributeValue(null,"descripcion"));
			}
		}
		Log.i("A","Fin parseando...");
		is.close();
	}	
	
	private static GregorianCalendar getPeriodo(GregorianCalendar dia, XmlPullParser parser) {
		String periodo=parser.getAttributeValue(null, "periodo");
		if(periodo==null) {
			periodo="00-24";
		}
		GregorianCalendar result=new GregorianCalendar();
		result.setTime(dia.getTime());
		int horaInicio=Integer.parseInt(periodo.split("-")[0]);
		result.set(Calendar.HOUR_OF_DAY, horaInicio);
		result.set(Calendar.MINUTE,0);
		result.set(Calendar.SECOND,0);		
		return result;
	}

}
