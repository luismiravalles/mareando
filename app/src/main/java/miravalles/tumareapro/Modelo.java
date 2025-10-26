package miravalles.tumareapro;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import miravalles.tumareapro.data.CoeficientesDao;
import miravalles.tumareapro.domain.DatosListener;
import miravalles.tumareapro.domain.England;
import miravalles.tumareapro.domain.Foto;
import miravalles.tumareapro.domain.Sitio;
import miravalles.tumareapro.domain.Spain;
import miravalles.tumareapro.vo.GeoLocalizacion;
import miravalles.tumareapro.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class Modelo {
	
	private static Modelo modelo;
	private TuMareaActivity contexto;

	
	public static final int SIN_DATOS=-20000;
												// indicador de que un sitio no tiene			
											    // datos con desfase

	private Sitio []sitios=null;
	
	private int [][]coeficientes;
			

	
	
	
	
	public static Modelo crearModelo(TuMareaActivity contexto) {
		modelo=new Modelo(contexto);		
		return modelo;
	}
			
	
	public Modelo(TuMareaActivity contexto) {
		
		if(Config.isEngland()) {
			sitios=new England().getSitios();
		} else {
			sitios=new Spain().getSitios(contexto);
		}		
				
		this.contexto=contexto;
		
		cargarSitiosDeUsuario(contexto);
		cargarFotos();		
	}
	
	public boolean existeFecha(Date fecha) {
		GregorianCalendar gc=new GregorianCalendar();
		GregorianCalendar hoy=new GregorianCalendar();
		hoy.setTime(new Date());
		gc.setTime(fecha);
		return gc.get(gc.YEAR)==hoy.get(gc.YEAR);
	}

	public int getNumSitios() {
		return sitios.length;
	}

	public String getNombreSitio(int i) {
		return sitios[i].nombre;
	}

	public GeoLocalizacion getGeo(int i) {
		return sitios[i].getGeo();
	}
	
	public void removeFoto(int lugar, int metros) {
		Foto foto=getFoto(lugar, metros);
		if(foto!=null && foto.isExterna()) {
		    File photo = new File(contexto.getDirectorioImagenes(),
		    		foto.getNombreExterna(modelo.getSitio(lugar))
		    		);
		    photo.delete();
		}
	}

	public Foto getFoto(int lugar, int metros) {
		int min=1000;
		int cercano=0;
		boolean alguno=false;
		List<Foto> fotos = sitios[lugar].fotos;
		// Primero que coincida del sitio-
		for(int i=0; i<fotos.size(); i++) {
			alguno=true;
			Foto foto=fotos.get(i);
			if(Math.abs(foto.centimetros-metros) < min) {
				min=Math.abs(foto.centimetros-metros);
				cercano=i;
			}
		}
		// Si no tengo del sitio me vale con que se acerque en centimetros.
		if(!alguno) {
			return new Foto(R.drawable.imgnodisponible, 0);
		}		
		return fotos.get(cercano);
	}

	private void getAnterior(
			int [][]marea, int [][]altura,	Sitio sitio,  int mes, int i, MareaInfo info) {
		int iAnt=i-1;
		int mesAnt=mes;
		if(iAnt<0) {
			mesAnt--;
			if(mesAnt<0) {
				mesAnt=0;
			}
			iAnt=marea[mesAnt].length-1;
		}
		GregorianCalendar gc=utcCalendar(mesAnt, 1);
		boolean esPleamar=esPleamar(altura[mesAnt], iAnt);
		gc.add(gc.MINUTE, getEventoAjustado(marea[mesAnt][iAnt], esPleamar, sitio));
		info.anterior=gc.getTime();
		info.alturaAnterior=altura[mesAnt][iAnt];
	}
	
	private int [][]getMarea(int sitio) {
		return sitios[sitio].getMarea();
	}
	
	private int [][]getAltura(int sitio) {
		return sitios[sitio].getAltura();
	}
	
	
	/**
	 * Retorna el �ltimo �ndice de una matriz que tenga datos, se considera
	 * que tiene datos si su valor es diferente de cero.
	 */
	private int ultimo(int[][]datos,  int mes) {
		for(int i=datos[mes].length-1; i>=0; i--) {
			if(datos[mes][i]!=0) {
				return i;
			}
		}
		return 0;
	}
	
	public static int getEdadLuna(Date date) {
		GregorianCalendar gc = utcCalendar();
		gc.setTime(date);
		int dia = gc.get(gc.DAY_OF_MONTH) - 1;
		int mes = gc.get(gc.MONTH);
		int ano = gc.get(gc.YEAR);

		int edad=getEpacta(ano)+getDiasDesde1Enero(mes, dia);
		return ((edad * 100 ) % 2953) / 100;
	}	
	
	public static int getEdadLuna(int ano, int mes, int dia) {
//		int edad=getEpacta(ano)+getDiasDesde1Enero(mes, dia);
//		return edad % 29;
		// Seg�n el m�todo con correcci�n mensual publicado en http://jms32.eresmas.net/web2008/documentos/divulgacion/astronomia/2010_08_14_FasesLuna.html#Refh5_Fases_De_La_Luna_2010
		final int []CORRECCION_MENSUAL=new int[] {
				0,1,0,1,2,3,4,5,6,7,8,9
		};
		int edad= getEpacta(ano)+CORRECCION_MENSUAL[mes]+dia;
		return edad % 29;		
	}
	
	public static int getDiasDesde1Enero(int mes, int dia) {
		int []diasmes=new int[]{31,28,31,30,31,30,31,31,30,31,30,31};
		int dias=0;
		for(int i=0; i<mes; i++) {
			dias+=diasmes[i];
		}
		return dias+dia;
	}
	
	public static int getEpacta(int year) {
		int A = year % 19;                       // Año dentro del ciclo metónico
		int B = year / 100;                      // Corrección solar
		int C = (8 * (B + 1)) / 25;              // Corrección lunar
		int D = (B - C + 1) / 3;                 // Ajuste gregoriano

		int epacta = (8 + 11 * A - B + C + D) % 30;

		if (epacta <= 0) {
			epacta += 30;                        // La epacta 0 se considera 30
		}

		return epacta;
	}
	

	
	private static int getEventoAjustado(int evento, boolean esPleamar, Sitio sitio) {
		int result;
		if(esPleamar) {
			result= evento + sitio.desfase + sitio.desfasePleamar;
		} else {
			result= evento + sitio.desfase + sitio.desfaseBajamar;
		}
		return result;
	}
	
	public static boolean esPleamar(int []altura, int i) {
		int alt=altura[i];
		int adyacente=altura[ i==0? i+1:i-1];
		return alt > adyacente;
	}
	
	public MareaInfo getMareaInfo(int sitio, Date momento) {
		MareaInfo info=new MareaInfo(momento, sitios[sitio].getGeo());

		GregorianCalendar gc = utcCalendar();
		gc.setTime(momento);
		int dia = gc.get(gc.DAY_OF_MONTH) - 1;
		int mes = gc.get(gc.MONTH);
		int hora = gc.get(gc.HOUR_OF_DAY);
		int min = gc.get(gc.MINUTE);

		int ajustePleamar=sitios[sitio].ajustePleamar;
		int ajusteBajamar=sitios[sitio].ajusteBajamar;
		int escalaPleamar=sitios[sitio].escalaPleamar;
		int escalaBajamar=sitios[sitio].escalaBajamar;

		GregorianCalendar result = utcCalendar();
		result.setTime(momento);

		int altProx=0;
		
		int [][]marea=getMarea(sitio);
		int [][]altura=getAltura(sitio);
			
		if(marea[mes].length<1 || marea[mes][1]==0) {
			return info;
			//throw new IllegalStateException("Error mostrando datos para " 
			//		+ sitios[sitio].nombre);
		}

		int minmes = (dia * 1440) + (hora * 60) + min;
		for (int i = 0; i < marea[mes].length; i++) {
			boolean esPleamar=esPleamar(altura[mes], i);
			if (minmes < getEventoAjustado(marea[mes][i], esPleamar, sitios[sitio])) {
				result.add(result.MINUTE, 
						getEventoAjustado(marea[mes][i], esPleamar, sitios[sitio]) - minmes);
				altProx = altura[mes][i];
				info.siguiente=result.getTime();
				info.alturaSiguiente=altura[mes][i] ;
				getAnterior(marea, altura, sitios[sitio], mes,i,info);
				calcularCoeficiente(info);				
				ajustarAlturas(info, ajustePleamar, ajusteBajamar);
				ajustarEscalas(info, escalaPleamar, escalaBajamar);
				return info;
			}
		}
		boolean esPleamarUltimoDia=esPleamar(altura[mes], altura[mes].length-1);
		int eventoUltimoDia=marea[mes][marea[mes].length - 1];
		if (minmes >= getEventoAjustado(eventoUltimoDia, esPleamarUltimoDia, sitios[sitio])) {
			GregorianCalendar gcAnterior=utcCalendar( mes, 1);
			int ultimoI=ultimo(marea,  mes);
			int minutos=marea[mes][ultimoI];
			gcAnterior.add(gc.MINUTE, minutos);
			info.anterior=gcAnterior.getTime();
			info.alturaAnterior=altura[mes][ultimoI];
			// Ahora a por la siguiente
			mes++;
			if(mes>11) {
				mes=11;
			}
			GregorianCalendar gcSiguiente=utcCalendar(mes,1);
			gcSiguiente.add(gc.MINUTE, marea[mes][0]);
			info.siguiente=gcSiguiente.getTime();
			info.alturaSiguiente=altura[mes][0];
		}
		ajustarAlturas(info, ajustePleamar, ajusteBajamar);
		ajustarEscalas(info, escalaPleamar, escalaBajamar);

		calcularCoeficiente(info);
		return info;
	}
	
	private void ajustarAlturas(MareaInfo info, int ajustePleamar, int ajusteBajamar) {
		if(info.alturaSiguiente>info.alturaAnterior) {
			info.alturaSiguiente += ajustePleamar;
			info.alturaAnterior  += ajusteBajamar;
		} else {
			info.alturaSiguiente += ajusteBajamar;
			info.alturaAnterior +=  ajustePleamar;
		}
	}
	
	private void ajustarEscalas(MareaInfo info, int escalaPleamar, int escalaBajamar) {
		if(info.alturaSiguiente>info.alturaAnterior) {
			info.alturaSiguiente = info.alturaSiguiente * escalaPleamar / 100;
			info.alturaAnterior  = info.alturaAnterior  * escalaBajamar / 100;
		} else {
			info.alturaSiguiente = info.alturaSiguiente * escalaBajamar / 100;
			info.alturaAnterior  = info.alturaAnterior *  escalaPleamar / 100;
		}
	}
	
	
	public void calcularCoeficiente(MareaInfo info) {
		GregorianCalendar gc = utcCalendar();
		gc.setTime(info.hora);
		int dia = gc.get(gc.DAY_OF_MONTH) - 1;
		int mes = gc.get(gc.MONTH);
		if(coeficientes!=null) { // Podría no estar cargado aun.
			info.coeficiente=coeficientes[mes][dia];
		}
	}
	
	

	
	public String getSituacion(int i, Date hora) {
		MareaInfo info=getMareaInfo(i, hora);
		SimpleDateFormat sd=new SimpleDateFormat("HH:mm");
		return  
				"<small>" + 
				info.getEstado() + 
				" <small>desde </small>" + "</small><b>" + 
				info.getHoraAnterior() + "</b>";
	}
	
	private static final TimeZone utc=new SimpleTimeZone(0,"UTC");
	
	public static GregorianCalendar utcCalendar() {
		GregorianCalendar cal=new GregorianCalendar(utc);
		return cal;
	}
	
	public static TimeZone getTimeZone() {
		return utc;
	}
	
	public GregorianCalendar utcCalendar(int mes , int dia) {
		int anoActual=new Date().getYear()+1900;
		GregorianCalendar cal=new GregorianCalendar(anoActual, mes, dia);
		cal.setTimeZone(utc);
		return cal;
	}

	public static Modelo get() {
		return modelo;
	}
	

	
	public int buscarSitioPorNombre(String nombre) {
		for(int i=0; i<sitios.length; i++) {
			if(sitios[i].nombre.equals(nombre)) {
				return i;
			}
		}
		return -1;
	}
	
	
	public int buscarSitioPorPosicion(GeoLocalizacion geo) {
		double min=10000;
		int result=0;
		for(int i=0; i<sitios.length; i++) {
			double dLatitud=geo.y()-sitios[i].getGeo().y();
			double dLongitud=geo.x()-sitios[i].getGeo().x();		
			double distancia=Math.sqrt(dLatitud*dLatitud + dLongitud*dLongitud);
			if(distancia < min) {
				min=distancia;
				result=i;
			}
		}
		return result;
	}
	
	private Sitio buscarReferenciaPorPosicion(GeoLocalizacion geo) {
		double min=10000;
		Sitio result=null;
		for(int i=0; i<sitios.length; i++) {
			double dLatitud=geo.y()-sitios[i].getGeo().y();
			double dLongitud=geo.x()-sitios[i].getGeo().x();
			if(Math.abs(sitios[i].getGeo().y())>0.01 && Math.abs(sitios[i].getGeo().x())>0.01 && !sitios[i].deUsuario) {
				double distancia=Math.sqrt(dLatitud*dLatitud + dLongitud*dLongitud);
				if(distancia < min) {
					min=distancia;
					result=sitios[i];
				}
			}
		}
		return result;
	}
	
	
	
	public List<Sitio> getListaSitiosAlfa() {
	
		List<Sitio> sitiosAlfa=new ArrayList<Sitio>(
				Arrays.asList(sitios));
		Collections.sort(sitiosAlfa, new Comparator<Sitio>() {
			public int compare(Sitio lhs, Sitio rhs) {
				return lhs.nombre.compareTo(rhs.nombre);
			}
		});
		return sitiosAlfa;
	}
	
	public Sitio getSitio(int pos) {
		return sitios[pos];
	}
	
	public int getIndiceSitio(Sitio sitio) {
		for(int i=0; i<sitios.length; i++) {
			if(sitio==sitios[i]) {
				return i;
			}		
		}
		return 0;
	}

	public void addFoto(int sitio, int altura) {
		sitios[sitio].fotos.add(new Foto(altura));		
	}
	

	public void cargarFotos() {
		File dir=contexto.getDirectorioImagenes();
		File[] ficheros=dir.listFiles();
		if(ficheros!=null) {
			for(File fichero:ficheros) {
				String p[]=fichero.getName().split("[-\\.]");
				if(p.length>=2) {
					int sitio=getSitio(p[0]);
					try {
						int altura=Integer.parseInt(p[1]);
						if(sitio>=0) {
							addFoto(sitio, altura);
						}
					} catch(NumberFormatException e) {
						// No pasa nada, era que la foto se llamar�a 
						// por ejemplo cudillero-rar y no nos interesa.
					}
				}
			}
		}
	}
	
	public int getSitio(String nombreNormalizado) {
		for(int i=0; i<sitios.length; i++) {
			if(nombreNormalizado.equals(sitios[i].getNombreNormalizado())) {
				return i;
			}
		}
		return -1;
	}
	
	public void crearSitio(String nombre, String coordenadas)  {
		String c[]=coordenadas.split("[;:]");
		double latitud=Double.parseDouble(c[0]);
		double longitud=Double.parseDouble(c[1]);
		//double longitud=43.58067064886307;
		//double latitud=-6.192469596862793;
		
		GeoLocalizacion geo=new GeoLocalizacion(latitud, longitud);
		Sitio referencia=			
				buscarReferenciaPorPosicion(geo);
		
		String equivalente=referencia.getNombreNormalizado();
		if(referencia.getEquivalente()!=null) {
			equivalente=referencia.getEquivalente();
		}
		int desfase=referencia.desfase;
		int ajustePleamar=referencia.ajustePleamar;
		int ajusteBajamar=referencia.ajusteBajamar;
		
		Sitio nuevoSitio=new Sitio(nombre, equivalente, 
							 referencia.getCodigoAemet(),  geo ,desfase ,ajustePleamar, ajusteBajamar);
		nuevoSitio.deUsuario=true;
		addSitio(nuevoSitio);
	}
	
	private void addSitio(Sitio nuevo) {
		Sitio []nuevaMatriz=new Sitio[sitios.length+1];
		for(int i=0; i<sitios.length; i++) {
			nuevaMatriz[i]=sitios[i];
		}
		nuevaMatriz[sitios.length]=nuevo;
		sitios=nuevaMatriz;
	}	
	
	public void cargarSitiosDeUsuario(Context contexto) {
		SharedPreferences pref=PreferenceManager.getDefaultSharedPreferences(contexto);
		int n=pref.getInt("numSitios", 0);
		Sitio []nuevaMatriz=new Sitio[sitios.length+n];
		for(int i=0; i<sitios.length; i++) {
			nuevaMatriz[i]=sitios[i];
		}
		for(int i=0; i<n; i++) {
			String saved=pref.getString("sitio" + i, "");
			String campos[]=saved.split("[;:]");
			
			GeoLocalizacion geo=new GeoLocalizacion(
					   Double.parseDouble(campos[2]), 
					   Double.parseDouble(campos[3]));
			Sitio referencia=			
					buscarReferenciaPorPosicion(geo);
			
			String equivalente=referencia.getNombreNormalizado();
			if(referencia.getEquivalente()!=null) {
				equivalente=referencia.getEquivalente();
			}
			int desfase=referencia.desfase;
			int ajustePleamar=referencia.ajustePleamar;
			int ajusteBajamar=referencia.ajusteBajamar;
						
			Sitio nuevo=new Sitio(
					campos[0], equivalente, referencia.getCodigoAemet(),  geo , desfase, ajustePleamar, ajusteBajamar);
			nuevo.deUsuario=true;
			nuevaMatriz[sitios.length+i]=nuevo;
			Log.i("X", "Cargando sitio de usuario :" + saved);
		}
		sitios=nuevaMatriz;
	}
	
	public void guardarSitiosDeUsuario(Context contexto) {
    	SharedPreferences pref=PreferenceManager.getDefaultSharedPreferences(contexto);
    	Editor edit=pref.edit();
    	int n=0;
    	for(int i=0; i<sitios.length; i++) {
    		if(sitios[i].deUsuario) {
    			String save=sitios[i].nombre + ";" + sitios[i].getEquivalente() + ";" + 
    					sitios[i].getGeo().y() + ";" + sitios[i].getGeo().x();
    			edit.putString("sitio" + n , save);
    			n++;
    		}
    	}    	
    	edit.putInt("numSitios", n);
    	edit.commit();    			
	}
	
	public void borrarSitio(int sitio) {
		Sitio[] nuevaMatriz=new Sitio[sitios.length-1];
		int n=0;
		for(int i=0; i<sitios.length; i++) {
			if(sitio!=i) {
				nuevaMatriz[n]=sitios[i];
				n++;
			}
		}
		sitios=nuevaMatriz;
	}
	
	public void cargarCoeficientes(Context contexto, DatosListener listener)  {
		if(coeficientes!=null) {
			listener.datosCargados();
			return;
		}
		Executor executor= Executors.newSingleThreadExecutor();
		executor.execute(() -> {
			CoeficientesDao coeficientesDao=new  CoeficientesDao(contexto);
			coeficientes=coeficientesDao.cargarCoeficientes(Util.thisYear());
			new Handler(Looper.getMainLooper()).post(() ->{
				listener.datosCargados();
			});
		});
	}
	
}

	