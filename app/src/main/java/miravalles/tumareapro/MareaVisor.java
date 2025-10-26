package miravalles.tumareapro;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import miravalles.tumareapro.domain.AemetInfo;
import miravalles.tumareapro.domain.Foto;
import miravalles.tumareapro.domain.Sitio;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;

import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * Visor de Marea. Antes era un Paginador, ahora en vez de Páginas
 * mostramos en un único visor.
 */
public class MareaVisor  implements AemetListener {
	
	Modelo modelo;
	private TuMareaActivity contexto;


	private LinearLayout viewRaiz;


	private boolean sinFoto=true;

	/**
	 * Indice del sitio que estamos mostrando...
	 */
	private int indiceSitio;


	private Sizer sizer=new Sizer();


	private EventosView eventosView;


	public MareaVisor(TuMareaActivity contexto, Modelo modelo) {
		this.contexto=contexto;
		this.modelo=modelo;
		
		restaurarEstado();
	}

	/**
	 * Crear el contenido del
	 * @param context El contexto actual
	 * @return
	 */
	public View crearView(Context context) {
		viewRaiz = new LinearLayout(context);
		viewRaiz.setOrientation(LinearLayout.VERTICAL);

		sizer.set(viewRaiz).fillWidth().fillHeight();
		viewRaiz.setBackgroundColor(Color.CYAN);

		viewRaiz.addView(crearZonaInfoSuperior(context));
		viewRaiz.addView(crearZonaInfo(context));
		// Esta zona inferior podría ser donde se arrastrara el dedo...
		viewRaiz.addView(crearZonaInferior(context)); // Para dejar un espacio vacio...

		return viewRaiz;
	}


	public void setIndiceSitio(int indiceSitio) {
		this.indiceSitio=indiceSitio;
		cargarDatos();
	}

	public void actualizarEnUiThread() {
		Activity activity=(Activity)viewRaiz.getContext();
		new Handler(Looper.getMainLooper()).post(() -> {
			actualizarDatos(viewRaiz, indiceSitio);
		});
	}

	private void cargarDatos() {
		GregorianCalendar gc = Modelo.utcCalendar();
		gc.setTime(contexto.getFechaVista());
		int mes=gc.get(Calendar.MONTH);
		int ano=gc.get(Calendar.YEAR);

		MareaInfo info=modelo.getMareaInfo(indiceSitio, contexto.getFechaVista());
		Sitio sitio=modelo.getSitio(indiceSitio);
		sitio.cargarDatos(viewRaiz.getContext(), ano, mes, this::actualizarEnUiThread);
		modelo.cargarCoeficientes(viewRaiz.getContext(), this::actualizarEnUiThread);
		AemetInfo aemetInfo=Aemet.getCache(sitio.getCodigoAemet());
		if(aemetInfo==null) {
			Aemet.cargar(this.contexto, sitio.getCodigoAemet(), this);
		}

	}


	
	/**
	 * Establece el tama�o de la zona info y la zona de foto y 
	 * se encarga de gestionarlas.
	 * 
	 * @param zonaInfo
	 */
	private void gestionarGestos(LinearLayout zonaInfo) {
		final GestureDetector detector=new GestureDetector(contexto,
								new OnGestureListener() {
									
									public boolean onSingleTapUp(MotionEvent e) {
										// TODO Auto-generated method stub
										return false;
									}
									
									public void onShowPress(MotionEvent e) {
										// TODO Auto-generated method stub
									}
									
									public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
											float distanceY) {
										Log.i("GESTO", "ACTION= " + e1.getAction());
										if(distanceX != 0) {
											cambiarHora((int)distanceX, zonaInfo);
										} 
										return true;
									}
									
									public void onLongPress(MotionEvent e) {
										// TODO Auto-generated method stub
										
									}
									
									public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
											float velocityY) {
										// TODO Auto-generated method stub
										return false;
									}
									
									public boolean onDown(MotionEvent e) {
										// TODO Auto-generated method stub
										return false;
									}
								
											
						});
		
		zonaInfo.setOnTouchListener(new OnTouchListener() {			
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_UP
				|| event.getAction()==MotionEvent.ACTION_CANCEL) {
					Log.i("x","setRespetar=false porque " + event.getAction());
					contexto.setRespetarTouchEvent(false);
				}
				detector.onTouchEvent(event);										
				return true;
			}
		});
	}
	
	
	private void setSize(View zonaInfo) {
		sizer.set(zonaInfo).fillWidth().fillHeight();	// Ojo que es respecto al 90%
	}

	private View crearZonaInfoSuperior(Context context) {
		eventosView=new EventosView(context);
		eventosView.setTag("eventosView");
		sizer.set(eventosView).fillWidth().weightY(1);
		return eventosView;
	}

	private View crearZonaInferior(Context context) {
		LinearLayout zona=new LinearLayout(context);
		zona.setBackgroundColor(Estilo.COLOR_FONDO_INFO);
		sizer.set(zona).fillWidth().weightY(1);
		return zona;
	}


	private ViewGroup crearZonaInfo(Context context) {
		LinearLayout zonaInfo=new LinearLayout(context);

		zonaInfo.setOrientation(LinearLayout.HORIZONTAL);
		zonaInfo.setTag("ZonaInfo" );

		GraficoActual grafico=new GraficoActual(context);
		grafico.setTag("grafico");
		sizer.set(grafico).fillWidth().fillHeight();
		zonaInfo.addView(grafico);
		sizer.set(zonaInfo).fillWidth().weightY(4);

		gestionarGestos(zonaInfo);
		return zonaInfo;
	}
	

	public void verFotoAmpliada(final Foto foto, int pos) {
	    final File photo = new File(contexto.getDirectorioImagenes(),
	    		foto.getNombreExterna(modelo.getSitio(pos))
	    		);
		
		
		Intent it=new Intent(Intent.ACTION_VIEW);
		it.setDataAndType(Uri.fromFile(photo), "image/*");
		contexto.startActivity(it);
	}

	public void setSinFoto(boolean modo) {
		sinFoto=modo;
		guardarEstado();
	}
	
    public void guardarEstado() {
    	SharedPreferences pref=PreferenceManager.getDefaultSharedPreferences(contexto);
    	Editor edit=pref.edit();
    	edit.putBoolean("sinFoto", sinFoto);
    	edit.commit();    	
    }
    
    public void restaurarEstado() {
    	SharedPreferences pref=PreferenceManager.getDefaultSharedPreferences(contexto);
		sinFoto=true;
		// De momento no queremos mostrar foto.
    	// sinFoto=pref.getBoolean("sinFoto", false);
    }

    public void cambiarHora(int x, View zonaInfo) {
		Log.i("GESTO", "Cambiar hora");
    	Date fechaActual=contexto.getFechaVista();
    
    	fechaActual =new Date(fechaActual.getTime() - x * 60 * 1000);
    	contexto.setFechaVista(fechaActual);
		Log.i("GESTO", "Nueva hora " + fechaActual);
    	
    	GraficoActual grafico=(GraficoActual)zonaInfo.findViewWithTag("grafico");
    	if(grafico!=null) {
    		MareaInfo info=modelo.getMareaInfo(indiceSitio, contexto.getFechaVista());
    		grafico.setInfo(info, indiceSitio);
    		grafico.invalidate();

			eventosView.setInfo(info);
			eventosView.invalidate();
    	}
    }

	public void actualizarDatos(View raiz, int pagina) {
		Log.i("X", "actualizando Datos de " + pagina);
		GraficoActual grafico=(GraficoActual)getZonaInfo().findViewWithTag("grafico");
		MareaInfo info=modelo.getMareaInfo(pagina, contexto.getFechaVista());
		grafico.setInfo(info, indiceSitio);
		grafico.invalidate();
		eventosView.setInfo(info);
		eventosView.invalidate();
	}

	private View getZonaInfo() {
		return viewRaiz.findViewWithTag("ZonaInfo");
	}

    public void cargado(AemetInfo info) {
    	GraficoActual grafico=(GraficoActual)getZonaInfo().findViewWithTag("grafico");
    	grafico.invalidate();
    }


	public void refresh() {
		// TODO: Invalidar datos... etc...
		cargarDatos();
	}

	public int getIndiceSitio() {
		return indiceSitio;
	}
}
