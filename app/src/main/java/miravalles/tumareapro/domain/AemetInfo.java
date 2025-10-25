package miravalles.tumareapro.domain;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import android.util.Log;

public class AemetInfo {
	
	
	private SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHH");
	private Map<String,Datos> datos=new HashMap<String,Datos>();

	public String getOleaje() {
		return capitalize(oleaje);
	}
	public void setOleaje(String oleaje) {
		this.oleaje = oleaje;
	}
	private String nombre;
	private String fecha;
	
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public String getFecha() {
		return fecha;
	}
	public void setFecha(String fecha) {
		if(fecha!=null && fecha.length()>=8) {
			this.fecha = fecha.substring(6,8) + "/" + fecha.substring(4,6) + "/" + fecha.substring(0,4);
		} else {
			this.fecha = fecha;
		}
	}
	private String temperaturaAgua;
	private String viento;
	private String cielo;
	private String oleaje;
	
	
	public String getTemperaturaAgua() {
		return temperaturaAgua ;
	}
	public void setTemperaturaAgua(String temperaturaAgua) {
		if(temperaturaAgua != null) {
			this.temperaturaAgua = temperaturaAgua + "º";
		} else {
			this.temperaturaAgua = temperaturaAgua;
		}
	}
	public String getViento() {
		return capitalize(viento);
	}
	public void setViento(String viento) {
		this.viento = viento;
	}
	public String getCielo() {
		return capitalize(cielo);
	}
	public void setCielo(String cielo) {
		this.cielo = cielo;
	}
	
	
	String capitalize(String origen) {
		if(origen==null) {
			return null;
		} else {
			return origen.substring(0,1).toUpperCase() + origen.substring(1);
		}
	}
	
	public static class Datos {
		private  String direccionViento;
		private  String velocidadViento;
		private  String cielo;
		
		public String getDireccionViento() {
			return direccionViento;
		}
		public void setDireccionViento(String direccionViento) {
			this.direccionViento = direccionViento;
		}
		public String getVelocidadViento() {
			return velocidadViento;
		}
		public void setVelocidadViento(String velocidadViento) {
			this.velocidadViento = velocidadViento;
		}
		
		public String getImagenViento() {
			int v=getIntVelocidadViento();
			String n="";
			if(v<20) {
				n="1";
			} else if(v<30) {
				n="2";
			} else if(v<40) {
				n="3";
			} else {
				n="4";
			}
			return "viento/viento_n_" + n;
		}
		
		public float getAngulo() {
			if("S".equals(direccionViento)) {
				return 180;
			} else if ("SO".equals(direccionViento)) {
				return 225;
			} else if ("O".equals(direccionViento)) {
				return 270;
			} else if("NO".equals(direccionViento)) {
				return 315;
			} else if("N".equals(direccionViento)) {
				return 0;
			} else if("NE".equals(direccionViento)) {
				return 45;
			} else if("E".equals(direccionViento)) {
				return 90;
			} else if("SE".equals(direccionViento)) {
				return 135;
			}
			return 0;
		}
		
		public int getIntVelocidadViento() {
			try {
				return Integer.parseInt(velocidadViento);
			} catch(Exception e) {
				return 0;
			}
		}
		public void setCielo(String attributeValue) {
			this.cielo=attributeValue;
		}
		public String getCielo() {
			return this.cielo;
		}
		
		public String getNombreBitmapCielo() {
			return "cielo/" + this.cielo.replaceAll(" ", "-").toLowerCase();
		}
	}
	
	public void createDatos(GregorianCalendar periodo) {
		String formateado=sdf.format(periodo.getTime());
		if(datos.get(formateado)==null) {
			Log.i("D", "Creado datos para " + formateado);
			datos.put(formateado, new Datos());
		}
	}
	
	public Datos getDatos(GregorianCalendar periodo) {
		String formateado=sdf.format(periodo.getTime());
		return datos.get(formateado);
	}
	
	public Datos getDatos(Date date) {
		GregorianCalendar cal=new GregorianCalendar();
		cal.setTime(date);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		int hora=cal.get(Calendar.HOUR_OF_DAY);
		int []redondeos=new int[]{6,12,24};
		Datos result=null;
		for(int redondeo:redondeos) {
			int horaRedondeada= hora / redondeo;
			horaRedondeada*=redondeo;
			
			cal.set(Calendar.HOUR_OF_DAY, horaRedondeada);
			String formateado=sdf.format(cal.getTime());
			Log.i("D", "Buscando datos para " + formateado);
			result= datos.get(formateado);
			if(result!=null) {
				return result; 
			}

		}
		return result;
	}
	
}
