package miravalles.tumareapro;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import miravalles.BitmapUtil;
import miravalles.tumareapro.domain.AemetInfo;
import miravalles.tumareapro.domain.DatosListener;
import miravalles.tumareapro.domain.Foto;
import miravalles.tumareapro.domain.Sitio;
import miravalles.tumareapro.R;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.preference.PreferenceManager;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TableRow.LayoutParams;

public class Paginador extends PagerAdapter implements AemetListener {
	
	Modelo modelo;
	private TuMareaActivity contexto;

	private Map<Integer,Foto> fotosMostradas=new HashMap<Integer,Foto>();
	
	private Map<Integer,View> zonasInfo=new HashMap<Integer,View>();
	private Map<Integer,View> zonasFoto=new HashMap<Integer,View>();
	
	private boolean sinFoto=true;

	
	public Foto getFotoMostrada(int i) {
		return fotosMostradas.get(i);
	}

	public Paginador(TuMareaActivity contexto, Modelo modelo) {
		this.contexto=contexto;
		this.modelo=modelo;
		
		restaurarEstado();
	}

	@Override
	public int getCount() {
		return modelo.getNumSitios();
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		// TODO Auto-generated method stub
		return arg0==arg1;
	}
	
	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}

	public LinearLayout envolverRaya(ViewGroup padre, View hijo) {
		Sizer sizer=new Sizer();
		
		LinearLayout raya=new LinearLayout(padre.getContext());
		sizer.set(raya).fillWidth().setHeight(2);
		raya.setBackgroundColor(Color.WHITE);

		LinearLayout envoltorio=new LinearLayout(padre.getContext());
		envoltorio.setOrientation(LinearLayout.VERTICAL);
		
		padre.addView(envoltorio);
		envoltorio.addView(raya);
		envoltorio.addView(hijo);
		return envoltorio;
	}
	
	

	@Override
	public Object instantiateItem(final ViewGroup container, final int position) {

		Log.i("x", "INSTANCIANDO PAGINA " + position);

		final LinearLayout raiz=new LinearLayout(container.getContext());

		instanciarConDatos(modelo.getSitio(position), container, position, raiz);
		modelo.getSitio(position).cargarDatos(container.getContext(),
			 sitio -> actualizarDatos(raiz, position)
			);
		modelo.cargarCoeficientes(container.getContext(),
				sitio -> actualizarDatos(raiz, position));
		return raiz;
	}

	public void instanciarConDatos(Sitio sitio, final ViewGroup container,
								   	final int position, LinearLayout raiz) {
		MareaInfo info=modelo.getMareaInfo(position, contexto.getFechaVista());

		Sizer sizer = new Sizer();
		raiz.setOrientation(LinearLayout.VERTICAL);
		sizer.set(raiz).fillWidth().fillHeight();
		raiz.setBackgroundColor(Color.BLACK);
		//raiz.setWeightSum(100);

		final LinearLayout zonaInfo=new LinearLayout(container.getContext());
		zonaInfo.setOrientation(LinearLayout.HORIZONTAL);
		zonaInfo.setGravity(Gravity.CENTER_HORIZONTAL);
		zonaInfo.setTag("ZonaInfo_" + position);

		LinearLayout envoltorio=envolverRaya(raiz,zonaInfo);
		envoltorio.setWeightSum(100f);

		instanciarZonaInfo(container, position, sizer, info, zonaInfo);
		Foto foto=modelo.getFoto(position, info.getAltura());
		fotosMostradas.put(position,foto);
		final View fotoView;
		if(foto.codFoto==R.drawable.imgnodisponible) {
			fotoView=instanciarSinFoto(container, raiz);
		} else {
			fotoView=instanciarFoto(container, raiz, foto, position);
		}

		zonasInfo.put(position,zonaInfo);
		zonasFoto.put(position,  fotoView);

		gestionarTamanos(zonaInfo, fotoView);
		setSize(zonaInfo, fotoView);
		container.addView(raiz);
	}

	
	/**
	 * Establece el tama�o de la zona info y la zona de foto y 
	 * se encarga de gestionarlas.
	 * 
	 * @param zonaInfo
	 * @param fotoView
	 */
	private void gestionarTamanos(final LinearLayout zonaInfo,
			final View fotoView) {
		setSize(zonaInfo, fotoView);		
		final GestureDetector detector=new GestureDetector(
										contexto,
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
										Log.i("x", "Paginador: ACTION= " + e1.getAction());
										if(distanceY > 20) {
											// De momento no queremos mostrar o qitar foto
											//setSinFoto(false);
											//setSize(zonaInfo, fotoView);
										} else if(distanceY < -20) {
											setSinFoto(true);
											setSize(zonaInfo, fotoView);
										} else if(distanceX != 0 && contexto.isRespetarTouchEvent()) {
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
	
	
	private void setSize(View zonaInfo, View zonaFoto) {

		Sizer sizer=new Sizer();		// Dimensionar las tres partes. Han de sumar 74
		// sizer.set(llSitio).fillWidth().pctHeight(10);
		if(sinFoto) {

			sizer.set(zonaInfo).fillWidth().pctHeight(65);	// Ojo que es respecto al 90%		
			sizer.set(zonaFoto).fillWidth().pctHeight(0);
			
		} else {		
			sizer.set(zonaInfo).fillWidth().pctHeight(45);	// Ojo que es respecto al 90%		
			sizer.set(zonaFoto).fillWidth().pctHeight(40);
		}

	}
	
	private View instanciarFoto(ViewGroup container, ViewGroup raiz, final Foto foto, final int pos) {
		ImageView fotoView=new ImageView(container.getContext());
		
		
		// fotoView.setBackgroundColor(0x002852);
		
		((Activity)container.getContext()).registerForContextMenu(fotoView);
		fotoView.setTag("Foto");
		fotoView.setAdjustViewBounds(true);
		fotoView.setScaleType(ScaleType.FIT_START);
		// foto.setMinimumWidth(raiz.getContext().getResources().getDisplayMetrics().widthPixels);
		// fotoView.setPadding(0, 10, 0, 0);
		
		LinearLayout envoltorioFoto=envolverRaya(raiz, fotoView);		
		new Sizer().set(envoltorioFoto).fillWidth().fillHeight();
		
		if(foto.isExterna()) {
			String nombreImagen=foto.getNombreExterna(modelo.getSitio(pos));
			Bitmap bitmap;
			
			Log.i("F", "Mostrando " + nombreImagen);
		    final File photo = new File(contexto.getDirectorioImagenes(),
		    		nombreImagen
		    		);
            bitmap=BitmapUtil.decodeFile(photo);
			if(bitmap!=null) {
        		fotoView.setImageBitmap(bitmap);
        	}
			fotoView.setOnClickListener(new OnClickListener() {			
				public void onClick(View arg0) {
					verFotoAmpliada(foto,pos);
				}
			});

		} else {		
			fotoView.setImageResource(foto.codFoto);
		}
		return fotoView;
	}
	

	
	private WebView getVistaSinDatos(LinearLayout zonaInfo) {
		WebView vistaSinDatos;
		vistaSinDatos=new WebView(zonaInfo.getContext());
		Util.setLayout(vistaSinDatos, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		vistaSinDatos.loadUrl("file:///android_asset/sindatos.html");
		vistaSinDatos.setPadding(0,0,0,0);
		vistaSinDatos.setVerticalScrollBarEnabled(false);
		return vistaSinDatos;
	}
	
	private void instanciarSinDatos(LinearLayout zonaInfo) {
		zonaInfo.addView(getVistaSinDatos(zonaInfo));		
	}
	
	private View instanciarSinFoto(ViewGroup container, ViewGroup raiz) {
		ImageView fotoView=new ImageView(container.getContext());
		((Activity)container.getContext()).registerForContextMenu(fotoView);
		fotoView.setTag("Foto");
		fotoView.setAdjustViewBounds(true);
		fotoView.setScaleType(ScaleType.FIT_START);
		LinearLayout envoltorioFoto=envolverRaya(raiz, fotoView);		
		new Sizer().set(envoltorioFoto).fillWidth().fillHeight();
		fotoView.setImageDrawable(null);
		return fotoView;
	}
	

	private void instanciarZonaInfo(ViewGroup container, final int position,
			Sizer sizer, MareaInfo info, LinearLayout zonaInfo) {
		GraficoActual grafico=new GraficoActual(
								container.getContext(), 
								position);
		grafico.setTag("grafico");
		grafico.setInfo(info);
		sizer.set(grafico).pctWidth(100).fillHeight();
		// agua.setBackgroundColor(0x052241);
		zonaInfo.addView(grafico);
		sizer.set(zonaInfo).fillHeight();
	}
	


	
	@Override
	public void destroyItem(ViewGroup container, int position, Object view) {
		zonasInfo.put(position,null);
		zonasFoto.put(position,null);
		 ((ViewPager) container).removeView((View) view); 
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
    
    public void paginaCambiada(int pagina) {
    	if(zonasInfo.get(pagina)!=null) {
    		setSize(zonasInfo.get(pagina), zonasFoto.get(pagina));
    	}
    	
    	Sitio sitio=Modelo.get().getSitio(pagina);
    	if(contexto.isMostrarAemet()&& Config.isVersionPro() && sitio.getCodigoAemet()!=null) {
    		AemetInfo aemetInfo=Aemet.getCache(sitio.getCodigoAemet());
    		if(aemetInfo==null) {
    			Aemet.cargar(this.contexto, sitio.getCodigoAemet(), this);
    		}
    	}
    	
    }
    
    public void cambiarHora(int x, View zonaInfo) {
    	Date fechaActual=contexto.getFechaVista();
    
    	fechaActual =new Date(fechaActual.getTime() - x * 60 * 1000);
    	contexto.setFechaVista(fechaActual);
    	
    	GraficoActual grafico=(GraficoActual)zonaInfo.findViewWithTag("grafico");
    	if(grafico!=null) {
    		MareaInfo info=modelo.getMareaInfo(
    					grafico.getPosition(), contexto.getFechaVista());
    		grafico.setInfo(info);
    		grafico.invalidate();
    	}
    }

	public void actualizarDatos(View raiz, int pagina) {
		Log.i("X", "actualizando Datos de " + pagina);
		View zonaInfo=raiz.findViewWithTag("ZonaInfo_" + pagina);
		GraficoActual grafico=(GraficoActual)zonaInfo.findViewWithTag("grafico");
		MareaInfo info=modelo.getMareaInfo(
				grafico.getPosition(), contexto.getFechaVista());
		grafico.setInfo(info);
		grafico.invalidate();
	}
    public void cargado(AemetInfo info) {
		// TODO: Tengo que resolver esto. Me llaman antes de componer la página
    	View zonaInfo=zonasInfo.get(contexto.getPagina());
    	GraficoActual grafico=(GraficoActual)zonaInfo.findViewWithTag("grafico");
    	grafico.invalidate();
    }
}
