package miravalles.tumareapro;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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
import miravalles.tumareapro.domain.Foto;
import miravalles.tumareapro.domain.Sitio;
import miravalles.tumareapro.domain.Spain;
import miravalles.tumareapro.vo.AnoMes;
import miravalles.tumareapro.vo.GeoLocalizacion;
import miravalles.tumareapro.vo.Marea;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;

import org.shredzone.commons.suncalc.MoonIllumination;

public class Modelo {

	public static final int MINUTOS_DIA = 1440;
	private static Modelo modelo;
	private Context contexto;

	public static final int SIN_DATOS=-20000;
												// indicador de que un sitio no tiene			
											    // datos con desfase
	private Sitio []sitios=null;

	private File directorioImagenes;

	/**
	 * Guardamos los coeficientes del año actual y del año siguiente:
	 */
	private int [][][]coeficientes;
	// Para saber si los tengo que recargar...

	public static Modelo crearModelo(Context contexto, File directorioImagenes) {
		modelo=new Modelo(contexto);
		modelo.directorioImagenes = directorioImagenes;
		return modelo;
	}

	public Modelo(Context contexto) {
		sitios=new Spain().getSitios(contexto);
		this.contexto=contexto;
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
		    File photo = new File(directorioImagenes,
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

	/**
	 * Retorna el indice más alto de la matriz que esté cargado con algún valor.
	 * @param array La matriz
	 * @return El valor
	 */
	private int ultimoDato(int array[]) {
		for(int i=array.length-1; i>=0; i--) {
			if(array[i]!=0) {
				return i;
			}
		}
		return 0;
	}

	private int mesAnterior(int mes) {
		return mes==0?11:mes-1;
	}

	private void getAnterior(
			int [][]marea, int [][]altura,	Sitio sitio,  int mes, int i, MareaInfo info) {
		int iAnt=i-1;
		int mesAnt=mes;
		if(iAnt<0) {
			mesAnt=mesAnterior(mesAnt);
			iAnt=ultimoDato(marea[mesAnt]);
		}
		GregorianCalendar gc=utcCalendar(anoDelMes(mesAnt), mesAnt, 1);
		boolean esPleamar=esPleamar(altura[mesAnt], iAnt);
		gc.add(gc.MINUTE, getEventoAjustado(marea[mesAnt][iAnt], esPleamar, sitio));
		info.anterior=gc.getTime();
		info.alturaAnterior=altura[mesAnt][iAnt];
	}

	public Date getHoraDeMinutosDesdeInicioMes(int mes, int desdeInicioMes) {
		GregorianCalendar gc=utcCalendar(anoDelMes(mes), mes, 1);
		gc.add(gc.MINUTE, desdeInicioMes);
		return gc.getTime();
	}

	/**
	 * En las matrices de marea y alturas tengo cargados de cada mes siendo
	 * del año siguiente en el caso de que el mes sea menor al actual
	 */
	public int anoDelMes(int mes) {
		if(mes < Util.thisMonth()) {
			return Util.thisYear()+1;
		} else {
			return Util.thisYear();
		}
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
		// 1. Obtener la iluminación de la luna para hoy

		MoonIllumination moon = MoonIllumination.compute()
				.on(date) // Usa la fecha actual del dispositivo
				.execute();
		double faseGrados = moon.getPhase();

		double edadLuna = ((faseGrados + 180) / 360) * 29.53;
		int result= (int)Math.round(edadLuna);
		if(result>=29) {
			result=0;
		}
		return result;
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

	/**
	 * Devuelve los datos básicos de las mareas de un dia determinado.
	 *
	 * @param sitio El indice del sitio actual
	 * @param mes El mes en base 0
	 * @param dia El dia en base 0
	 * @return Tabla de Mareas
	 */
	public List<Marea> getTablaMareas(int sitio, int mes, int dia) {
		int [][]minutoMareas=getMarea(sitio);
		int [][]alturaMareas=getAltura(sitio);

		final int minutoInicioDia=dia * MINUTOS_DIA;
		int indiceInicioDia=buscarIndicePorMinuto(minutoMareas[mes], minutoInicioDia);


		List<Marea> mareas=new ArrayList<>();
		int ultimoIndice=ultimoDato(minutoMareas[mes]);
		// Recorremos hasta cambiar de día o topar con el fin del mes
		for(int i=indiceInicioDia;  i<=ultimoIndice; i++) {

			int diaActual=minutoMareas[mes][i] / MINUTOS_DIA;
			if(diaActual!=dia) {
				break;
			}

			GregorianCalendar gc=utcCalendar(anoDelMes(mes), mes, 1);
			gc.add(gc.MINUTE, minutoMareas[mes][i]);

			Marea marea=new Marea(gc.get(Calendar.HOUR),
								  gc.get(Calendar.MINUTE),
								alturaMareas[mes][i],
								getCoeficiente(gc.getTime()),
								getEdadLuna(gc.getTime())
								);
			mareas.add(marea);
		}
		// Establecer cuales son pleamares
		for(int i=0; i<mareas.size();  i++) {
			Marea compararCon= i==0?mareas.get(i+1):mareas.get(i-1);
			mareas.get(i).setPleamar(mareas.get(i).getAltura() > compararCon.getAltura());
		}
		Log.i("M", "getTableMareas retorna: " + mareas.size());
		return mareas;
	}

	public int buscarIndicePorMinuto(int []minutoMareas, int minuto) {
		int ultimo=ultimoDato(minutoMareas);
		for(int i=0; i<=ultimo; i++ ) {
			if(minutoMareas[i]>=minuto) {
				return i;
			}
		}
		return -1;
	}
	
	public MareaInfo getMareaInfo(int sitio, Date momento) {
		MareaInfo info=new MareaInfo(momento, sitios[sitio].getGeo());
		info.setSitio(sitio);

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

		int minutoMesBuscado = (dia * MINUTOS_DIA) + (hora * 60) + min;
		for (int i = 0; i < marea[mes].length; i++) {
			boolean esPleamar=esPleamar(altura[mes], i);
			if (minutoMesBuscado < getEventoAjustado(marea[mes][i], esPleamar, sitios[sitio])) {
				result.add(result.MINUTE, 
						getEventoAjustado(marea[mes][i], esPleamar, sitios[sitio]) - minutoMesBuscado);
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
		if (minutoMesBuscado >= getEventoAjustado(eventoUltimoDia, esPleamarUltimoDia, sitios[sitio])) {
			GregorianCalendar gcAnterior=utcCalendar(anoDelMes(mes), mes, 1);
			int ultimoI=ultimo(marea,  mes);
			int minutos=marea[mes][ultimoI];
			gcAnterior.add(gc.MINUTE, minutos);
			info.anterior=gcAnterior.getTime();
			info.alturaAnterior=altura[mes][ultimoI];
			// Ahora a por la siguiente
			mes++;
			if(mes>11) {
				mes=0;
			}
			GregorianCalendar gcSiguiente=utcCalendar(anoDelMes(mes),mes,1);
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

	/**
	 * Retorna -1 si no hay coeficiente cargado para esta hora.
	 *
	 * @param hora
	 * @return
	 */
	public int getCoeficiente(Date hora) {
		GregorianCalendar gc = utcCalendar();
		gc.setTime(hora);
		int dia = gc.get(gc.DAY_OF_MONTH) - 1;
		int mes = gc.get(gc.MONTH);
		int anoDelta = gc.get(GregorianCalendar.YEAR) - Util.thisYear();
		if(anoDelta<0) {
			return -1 ;
		}
		if(coeficientes!=null && coeficientes[anoDelta]!=null) { // Podría no estar cargado aun.
			return coeficientes[anoDelta][mes][dia];
		} else {
			return -1;
		}
	}
	
	public void calcularCoeficiente(MareaInfo info) {
		int coeficiente=getCoeficiente(info.hora);
		if(coeficiente>0) {
			info.coeficiente=coeficiente;
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
	
	public GregorianCalendar utcCalendar(int ano, int mes , int dia) {
		GregorianCalendar cal=new GregorianCalendar(ano, mes, dia);
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
	
	public void cargarCoeficientes(Context contexto, int ano, DatosListener listener)  {
		int anoDelta=ano-Util.thisYear();
		if(coeficientes!=null) {
			listener.datosCargados();
			return;
		}
		Executor executor= Executors.newSingleThreadExecutor();
		executor.execute(() -> {
			CoeficientesDao coeficientesDao=new  CoeficientesDao(contexto);
			coeficientes=new int[2][][];
			coeficientes[0]=coeficientesDao.cargarCoeficientes(ano);
			coeficientes[1]=coeficientesDao.cargarCoeficientes(ano+1);
			new Handler(Looper.getMainLooper()).post(() ->{
				listener.datosCargados();
			});
		});
	}


	public List<Sitio> getSitios() {
		return Arrays.asList(sitios);
	}
	
}

	