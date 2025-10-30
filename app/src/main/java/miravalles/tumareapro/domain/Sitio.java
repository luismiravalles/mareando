package miravalles.tumareapro.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import miravalles.tumareapro.Config;
import miravalles.tumareapro.Util;
import miravalles.tumareapro.data.DatosSitioAno;
import miravalles.tumareapro.data.InstitutoMarinaDatosDao;
import miravalles.tumareapro.data.MapeoPuertos;
import miravalles.tumareapro.vo.GeoLocalizacion;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;


public class Sitio {

	private GeoLocalizacion geo;
	public String 		nombre;
	public String		datos;
	private String		equivalente;
	public int			desfase;
	public int			desfasePleamar;
	public int			desfaseBajamar;
	public int			ajustePleamar;
	public List<Foto>	fotos;
	public int			ajusteBajamar;
	public int			[][]marea=new int[12][MAX];		// A�o y Mes
	public int			[][]altura=new int[12][MAX];		// A�o y Mes

	private String		errorInstitutoMarina[]=new String [12];


	public boolean		deUsuario;
	public int			unidadAltura;
	private String		codigoAemet;


	private int			idIHM;
	
	public int			escalaPleamar=100;	// Se multiplicar� en porcentaje
	public int			escalaBajamar=100;  // Se multiplicar� en porcentaje
	
	
	private static final Pattern patron=Pattern.compile("[ =\\[\\],;]+");
	
	
	private static final int	MAX= 31*4+1;	// El numero maximo de mareas que puede haber en un mes.


	public Sitio(String nombre) {
		this.nombre=nombre;
		this.fotos=new ArrayList<Foto>();
	}

	public Sitio(String nombre, String equivalente,String codigoAemet, GeoLocalizacion geo, int desfase, int ajustePleamar, int ajusteBajamar, Foto... fotos) {
		this(nombre, codigoAemet, geo, fotos);
		this.desfase=desfase;
		this.equivalente=equivalente;
		this.ajustePleamar=ajustePleamar;
		this.ajusteBajamar=ajusteBajamar;
		this.datos=null;
		this.codigoAemet=codigoAemet;
	}

	public Sitio(String nombre, String codigoAemet, GeoLocalizacion geo, int ajustePleamar, int ajusteBajamar, Foto... fotos) {
		this.nombre=nombre;
		this.datos=null;
		this.geo=geo;
		this.desfase=0;
		this.ajustePleamar=ajustePleamar;
		this.ajusteBajamar=ajusteBajamar;
		this.fotos=new ArrayList<Foto>(
				Arrays.asList(fotos));
		this.codigoAemet=codigoAemet;
	}
	
	public Sitio(String nombre, double y, double x, Foto... fotos) {
		this.nombre=nombre;
		this.datos=null;
		this.geo=new GeoLocalizacion(y,  x);
		this.fotos=new ArrayList<Foto>(
				Arrays.asList(fotos));
		this.codigoAemet=null;
	}
	
	public Sitio escala(int pleamar, int bajamar) {
		this.escalaPleamar=pleamar;
		this.escalaBajamar=bajamar;
		return this;
	}
	
	public Sitio desfasar(int minutos) {
		this.desfase=minutos;
		return this;
	}
	
	public Sitio desfasar(int pleamar, int bajamar) {
		this.desfasePleamar=pleamar;
		this.desfaseBajamar=bajamar;
		return this;
	}
	
	public Sitio(String nombre, String codigoAemet, GeoLocalizacion geo, Foto... fotos) {
		this.nombre=nombre;
		this.datos=null;
		this.geo=geo;
		this.desfase=0;
		this.fotos=new ArrayList<Foto>(
				Arrays.asList(fotos));
		this.codigoAemet=codigoAemet;
	}
	
	public Sitio(String nombre, String datos, Foto... fotos) {
		this.nombre=nombre;
		this.datos=datos;
		this.geo=new GeoLocalizacion(0.0, 0.0);
		this.desfase=0;
		this.ajustePleamar=0;
		this.ajusteBajamar=0;
		this.fotos=new ArrayList<Foto>(
				Arrays.asList(fotos));
		
	}
	
	
	public String getNombreNormalizado() {
		String nom=nombre.toLowerCase()
				.replaceAll("�","e")
				.replaceAll("�","i")
				.replaceAll("�","o")
				.replaceAll("�","a")
				.replaceAll(" " , "");

		return nom;
	}

	public void cargarDatos(final Context contexto, final int ano, final int mes, final Runnable runnable) {
		if(yaCargado(mes)) {
			runnable.run();
			return;
		}

		Executor executor= Executors.newSingleThreadExecutor();
		executor.execute( () -> {
			cargarDatosDosMeses(contexto, ano, mes);
			runnable.run();
		});

	}

	private boolean yaCargado(int mes) {
		return altura[mes][0]!=0;
	}

	private void cargarDatosDosMeses(final Context contexto, int ano, int mes) {
		InstitutoMarinaDatosDao dao=new InstitutoMarinaDatosDao(contexto);
		dao.obtenerDatosMes(this, ano, mes);
		mes++;
		if(mes>=12) {
			mes=0;
			ano++;
		}
		dao.obtenerDatosMes(this, ano, mes);
	}

	public int [][]getMarea() {
		return marea;
	}
	
	public int [][]getAltura() {
		return altura;
	}

	
	
	public GeoLocalizacion getGeo() {
		return geo;
	}


	public String getCodigoAemet() {
		return codigoAemet;
	}
	
	public String getEquivalente() {
		return equivalente;
	}

	public int getIdIHM() {
		return idIHM;
	}

	public Sitio idIHM(int idIHM) {
		this.idIHM = idIHM;
		return this;
	}

	public int primerEspacioVacio(int mes) {
		for(int i=0; i<Sitio.MAX; i++) {
			if(marea[mes][i]==0) {
				return i;
			}
		}
		return -1;
	}

	public Sitio nombre(String nombre) {
		this.nombre=nombre;
		return this;
	}

	public Sitio geo(double x, double y) {
		this.geo=new GeoLocalizacion(x, y);
		return this;
	}

	public Sitio aemet(String codigo) {
		this.codigoAemet=codigo;
		return this;
	}

	public Sitio geo(GeoLocalizacion geo) {
		this.geo=geo;
		return this;
	}

    public String getErrorInstitutoMarina(int mes) {
        return errorInstitutoMarina[mes];
    }

    public void setErrorInstitutoMarina(int mes, String errorInstitutoMarina) {
        this.errorInstitutoMarina[mes] = errorInstitutoMarina;
    }
}
