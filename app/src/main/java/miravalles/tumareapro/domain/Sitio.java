package miravalles.tumareapro.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
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
	public boolean		cargado;
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

	public void cargarDatos(final Context contexto, final DatosListener datosListener) {
		if(cargado) {
			datosListener.datosCargados(this);
			return;
		}
		AsyncTask<String, Void, String> tarea=new AsyncTask<String, Void, String>() {
			@Override
			protected String doInBackground(String... params) {
				cargarDatosDosMeses(contexto);
				return "";
			}
			@Override
			protected void onPostExecute(String result) {
				datosListener.datosCargados(Sitio.this);
			}
		};
		tarea.execute();
	}

	private void cargarDatosDosMeses(final Context contexto) {
		InstitutoMarinaDatosDao dao=new InstitutoMarinaDatosDao(contexto);
		int mes=Util.thisMonth();
		dao.obtenerDatosMes(this, mes);
		mes++;
		if(mes>=12) {
			mes=0;
		}
		dao.obtenerDatosMes(this, mes);
	}

	private void cargarDatosAleatorios() {
		Random r=new Random();
		for(int mes=0; mes<12; mes++) {
			int n=0;
			for(int dia=0; dia<31; dia++) {
				marea[mes][dia]=n + 300 + r.nextInt(100);
				altura[mes][dia]=r.nextInt(500);
			}
		}
		Log.i("X", "Datos Cargados");
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

}
