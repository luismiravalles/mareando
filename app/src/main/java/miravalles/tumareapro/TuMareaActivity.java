package miravalles.tumareapro;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import miravalles.BitmapUtil;
import miravalles.tumareapro.domain.Foto;
import miravalles.tumareapro.vo.GeoLocalizacion;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.MediaStore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.WindowCompat;

import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

public class TuMareaActivity extends AppCompatActivity implements OnClickListener, LocationListener {

	public static final String MIRAVALLES_TUMAREAPRO = "miravalles.tumareapro";
	MareaVisor mareaVisor;
	ViewGroup raiz;

	Modelo modelo;
	private Date	 fechaVista;
	// private SitioView sitioView;

	private boolean pies;
	
	public Date getFechaVista() {
		return fechaVista;
	}

	public void setFechaVista(Date fechaVista) {
		this.fechaVista = fechaVista;
		refreshFecha();
	}

	ImageView apuntador;
	private ImageView mapaView;
	private ScrollView scrollMapa;
	RelativeLayout zonaApuntador;
//	ImageButton botonPapelera;
//	ImageButton botonBorrarSitio;

	private int altoMapa;
	
	final DateFormat fechaFormatoTitulo=new SimpleDateFormat("dd MMM");
	
	SimpleDateFormat sdf=new SimpleDateFormat("EEE, dd MMM yyyy HH:mm");
	
	private int anchoScreenMapa;    	

	
	private boolean respetarTouchEvent;
	private boolean mostrarAemet;
	

    public boolean isMostrarAemet() {
		return mostrarAemet;
	}

	public boolean isRespetarTouchEvent() {
		return respetarTouchEvent;
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {

		AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    	
		Config.init(this);
		
		//this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
    	cargarPreferencias();
    	verificarZonaHoraria();
    	
    	modelo=Modelo.crearModelo(this);
    	
    	initDimensiones();

    	Sizer sizer=new Sizer();
        super.onCreate(savedInstanceState);

		// Según ChatGpt esto evita que machaquemos la barra de estado de arriba.
		WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        setContentView(R.layout.main);
        
        scrollMapa=(ScrollView)findViewById(R.id.scrollMapa);
        scrollMapa.getLayoutParams().height=(int)Mapa.getBordeInferiorPx(this);

        /*
        LinearLayout parteFija=(LinearLayout)findViewById(R.id.partefija);
        parteFija.setPadding(
        		Util.widthPct(12, parteFija),
        		Util.heightPct(7, parteFija), 
        		0,0);
		*/
        
        Log.d("X","El mapa mide de alto " + 
        			(int)Mapa.getBordeInferiorPx(this));
        
        raiz=(ViewGroup)findViewById(R.id.raiz);
		/*
        raiz.setPadding(
        		0, // Util.widthPct(12, raiz),
        		(int)Mapa.getBordeInferiorPx(this), // Deber�a ser esto pero queda un hueco no se prque        		
        		// Util.heightPct(20, raiz), 
        		0,0);
        */
        zonaApuntador=(RelativeLayout)findViewById(R.id.zonaApuntador);

        fechaVista=new Date();        
        if(!modelo.existeFecha(fechaVista)) {
        	mostrarErrorFecha();
        	return;
        }

        mareaVisor =new MareaVisor(this, modelo);
		raiz.addView(mareaVisor.crearView(this));

        mapaView=(ImageView)findViewById(R.id.mapa);
        mapaView.setAdjustViewBounds(true);
        mapaView.setScaleType(ScaleType.FIT_START);
        mapaView.setOnTouchListener(new View.OnTouchListener() {
        	
			
			public boolean onTouch(View vista, MotionEvent ev) {
				if(ev.getAction()==MotionEvent.ACTION_UP) {					
					int x=(int)ev.getX();
					int y=(int)ev.getY();
					buscarPosicion(x,y);
				}
					return true;
			}
        });
        
        apuntador=new ImageView(this);
        
   
        apuntador.setImageResource(R.drawable.apuntador);
        
        
        RelativeLayout.LayoutParams lpApuntador=new RelativeLayout.LayoutParams(
        		LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);              
        lpApuntador.addRule(RelativeLayout.ALIGN_TOP, 1);
        lpApuntador.setMargins(20,0,0,0);
        apuntador.setLayoutParams(lpApuntador);
        zonaApuntador.addView(apuntador);
		new Handler(Looper.getMainLooper()).post(() -> {
        	if(!restaurarPosicion())  {
	        	elegirSitio();
	        }
    	    sitioCambiado(getIndiceSitio());
		});
    }

	private void verificarZonaHoraria() {
		TimeZone tz=TimeZone.getDefault();
    	int offset=tz.getOffset(new Date().getTime());
    	Log.d("X", "Offset=" + offset);
    	if(offset > 2 * 60 * 60 * 1000 || offset < 0) {
    		Toast.makeText(this,
    			" Aviso !! Su teléfono está configurado en una zona horaria que no es" +
    			"de la península ni de Canarias. Los horarios no serán correctos.",
    			Toast.LENGTH_LONG).show();
    	}
	}
    
    public void onClick(View v) {
    }
    
    @Override
    public void onResume() {
    	Log.d("T","On Resume");
    	super.onResume();
    }
    
    
    public void refresh() {
    	mareaVisor.refresh();
    	sitioCambiado(getIndiceSitio());
    }
    
    public void refreshFecha() {
    	mostrarPosicionYFecha(getIndiceSitio());
    }
    
    
    public void mostrarErrorFecha() {
    	Util.mostrarAviso(this, "avisoFecha");
    }
    
    
    @Override
    protected void onPause() {
    	Log.d("X","onPause");
    	guardarPosicion();    	
    	super.onPause();
    }
    
    public void guardarPosicion() {
    	SharedPreferences pref=PreferenceManager.getDefaultSharedPreferences(this);
    	Editor edit=pref.edit();
    	edit.putInt("posicion", getIndiceSitio());
    	
    	try {
			PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			edit.putInt("version", pInfo.versionCode);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
    	edit.commit();    	
    }
    
    public boolean restaurarPosicion() {
    	SharedPreferences pref=PreferenceManager.getDefaultSharedPreferences(this);    	
    	int pos=pref.getInt("posicion", -1);
    	boolean mostrarElegirSitio=false;
    	if(pos<0 || pos>=Modelo.get().getNumSitios()) {
			// mostrarElegirSitio=true;
			pos = modelo.buscarSitioPorNombre("Cudillero");
		}
    	if(pos>=0) {
			mareaVisor.setIndiceSitio(pos);
		}
    	return (!mostrarElegirSitio);
    }
    
    private void initDimensiones() {
    	Display display = getWindowManager().getDefaultDisplay();   	
    	anchoScreenMapa = display.getWidth();    	
    }
    


    public void sitioCambiado(int indiceSitio) {
    	double latitud=modelo.getGeo(indiceSitio).y();

    	double longitud=modelo.getGeo(indiceSitio).x();
    	Mapa mapa=Mapa.getMapa(latitud);
    	pintarMapa(latitud, longitud, mapa);

	    mostrarPosicionYFecha(indiceSitio);
    }

	private void pintarMapa(double latitud, double longitud, Mapa mapa) {
		if(mapa.getResourceId()==0) {
			mapaView.setImageResource(0);
			return;
		}
		
		Bitmap bitmap=BitmapUtil.decodeResource(this.getResources(), mapa.getResourceId());
		
    	
    	altoMapa=BitmapUtil.getAltoReal(bitmap, anchoScreenMapa); 
    	    	
    	double latitudRelativa = latitud - mapa.getMinLatitud();	
    	double rangoLatitud=mapa.getRangoLatitud();
    	double rangoLongitud=mapa.getRangoLongitud();
    	double longitudScreen = 
    			// Lo siguiente es la latitud en base 1.
    			((longitud - mapa.getMinLongitud()) / rangoLongitud)
    					*
    			anchoScreenMapa;
    	Log.i("M", "altoMapa=" + altoMapa);
    	Log.i("M", "latitudRelativa=" + latitudRelativa);
    	Log.i("M", "rangoLatitud=" + rangoLatitud);
    	double latitudScreen = (latitudRelativa / rangoLatitud ) * altoMapa;	    			
    	
    	// Hay que ajustar restando el ancho y alto de la imagen del punto gordo
    	latitudScreen += 4;
    	longitudScreen -= 4;
    	
    	// Mostramos el Apuntador (Redondelito rojo)
    	RelativeLayout.LayoutParams lp=(RelativeLayout.LayoutParams)apuntador.getLayoutParams();
    	int x=(int)longitudScreen;
    	int y=(int)altoMapa - (int)latitudScreen;
    	lp.setMargins(x,y, 0,0); 
    	Log.i("M", "apuntador: " + x+ " , " + y);   	
    	apuntador.setLayoutParams(lp);    	
    	
    	int yScroll=altoMapa - (int)latitudScreen - (scrollMapa.getLayoutParams().height / 2);
    	if(yScroll<0) {
    		yScroll=0;
    	}
    	if(yScroll+scrollMapa.getLayoutParams().height>altoMapa) {
    		yScroll=altoMapa-scrollMapa.getLayoutParams().height;
    	}
    	
    	final int finalScroll=yScroll;
    	
    	mapaView.setImageBitmap(bitmap);

		new Handler(Looper.getMainLooper()).postDelayed(() -> {
			scrollMapa.smoothScrollTo(0, finalScroll);
		}, 1000);
    	
//    	scrollMapa.smoothScrollTo(0, yScroll);
//    	scrollMapa.setScrollY(yScroll);
    	
    	Log.i("S", "Scroll: " + yScroll);
	}
    
    public void mostrarPosicionYFecha(int pagina) {
    	//getActionBar().setIcon(0);
    	getSupportActionBar().setDisplayShowHomeEnabled(false);
    	getSupportActionBar().setTitle(
    			Modelo.get().getNombreSitio(pagina) 
    			+ " - " + fechaFormatoTitulo.format(
						  getFechaVista()))
    			;
    	
    }
       
    
    public void buscarPosicion(int x, int y) {
    	
    	y = y + scrollMapa.getScrollY();
    	
    	Log.i("B", "Buscando posicion " + x + " _ " + y);
    	
    	Mapa mapa=Mapa.getMapa(modelo.getGeo(getIndiceSitio()).y());

    	double rangoLatitud=mapa.getRangoLatitud();
    	double rangoLongitud=mapa.getRangoLongitud();
    	
    	double longitud=(rangoLongitud * (double)x)/(double)anchoScreenMapa+
    				mapa.getMinLongitud();

    	double yInverso = altoMapa-(double)y;
    	
    	Log.i("B", "AltoMapa = " + altoMapa);
    	Log.i("A", "rangoLatitud " + rangoLatitud);
    	double latitud=
    			mapa.getMinLatitud() + 
    				yInverso * rangoLatitud / altoMapa;
    	
    	Log.i("B", "Buscando posicion lat/long " + latitud + " , " + longitud);

    	int indiceSitio=modelo.buscarSitioPorPosicion(new GeoLocalizacion(latitud, longitud));
		sitioCambiado(indiceSitio);
		mareaVisor.setIndiceSitio(indiceSitio);
    }
	
	private void masDatos() {
		if(getPackageName().equals(MIRAVALLES_TUMAREAPRO)) {
			Intent myIntent = new Intent(TuMareaActivity.this, 
					MasDatos.class);
			myIntent.putExtra("posicion", getIndiceSitio());
			myIntent.putExtra("fecha", fechaVista.getTime());
			TuMareaActivity.this.startActivity(myIntent);	
		} else {
			Util.mostrarAviso(TuMareaActivity.this, "masdatos" );
		}
	}

	private void mostrarTablaMareas() {
		Intent myIntent = new Intent(TuMareaActivity.this, 
				TablaMareas.class);
		myIntent.putExtra("posicion", getIndiceSitio());
		myIntent.putExtra("fecha", fechaVista.getTime());
		TuMareaActivity.this.startActivity(myIntent);
	}	
	
	public void crearSitio() {
		Intent myIntent = new Intent(this,CrearSitioActivity.class);
		startActivityForResult(myIntent, CREAR_SITIO);
	}
		
	private void camara() {
		takePhoto();
	}
		
	private final static int TAKE_PICTURE=1;
	private final static int CREAR_SITIO=2;
	private final static int PREFERENCIAS=3;
	
	public File getDirectorioImagenes() {		
    	File directorio=Environment.getExternalStorageDirectory();
    	if(directorio!=null) {
    		directorio=new File(directorio, "tumarea");
    		directorio.mkdir();
    	}  else {
    		directorio=getCacheDir();
    	}
    	return directorio;
	}
	
	

	public void takePhoto() {
	    Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
	    
	    int sitio= getIndiceSitio();
    
	    MareaInfo info=modelo.getMareaInfo(sitio, fechaVista);
	    Integer alturaImagen=info.getAltura();
	    
	    Foto foto=new Foto(alturaImagen);
	    
	    File photo = new File(getDirectorioImagenes(),
	    		foto.getNombreExterna(modelo.getSitio(sitio))
	    		);
	    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));	    		
	    startActivityForResult(intent, TAKE_PICTURE);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}

	public void onLocationChanged(Location location) {

	}

	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
	    super.onCreateContextMenu(menu, v, menuInfo);
	    if ("Sitio".equals(v.getTag())) {
	    	MenuInflater inflater = getMenuInflater();	    
		    inflater.inflate(R.menu.menusitio, menu);
		    int pagina= getIndiceSitio();
	    	if(!Modelo.get().getSitio(pagina).deUsuario) {	    	
	    		menu.findItem(R.id.menu_borrar_sitio).setEnabled(false);	
	    	}	    	
	    }
	}
	
	public boolean procesarItemSelected(MenuItem item) {
		if(item.getItemId()==R.id.menu_ajustes){
	            mostrarAjustes();
	            return true;
		}

		if(item.getItemId()==R.id.tabla){
	        	mostrarTablaMareas();
	        	return true;
		}
		if(item.getItemId()==R.id.cambiar_sitio){
	        	elegirSitio();
	        	return true;
		}
		if(item.getItemId()==R.id.cambiar_fecha){
			elegirFecha();
			return true;
		}
		if(item.getItemId()==R.id.masdatos) {
			masDatos();
			return true;
		}
		if(item.getItemId()==R.id.acercade) {
			Util.mostrarAviso(TuMareaActivity.this, "acercaDeMareando" );
			return true;
		}
		return super.onContextItemSelected(item);

	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
	    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	    return procesarItemSelected(item);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menugeneral, menu);
	    return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
	    int pagina= getIndiceSitio();
    	menu.findItem(R.id.masdatos).setEnabled(!Config.isEngland());
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
		return procesarItemSelected(item);

	}	
 
	public void cargarPreferencias() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		pies=prefs.getBoolean("pies", false);
		MareaInfo.pies=pies;
		
		this.mostrarAemet=prefs.getBoolean("aemet", true);
	}
	
	public void mostrarAjustes() {
		Intent myIntent = new Intent(this,PreferenciasActivity.class);
		startActivityForResult(myIntent, PREFERENCIAS);    			
	}
	
	public void setRespetarTouchEvent(boolean respetarTouchEvent) {
		this.respetarTouchEvent=respetarTouchEvent;
	}
	

	
    public void elegirSitio() {
		new ElegirSitio(this,
    			Modelo.get().getSitio(getIndiceSitio()),
				sitioSeleccionado  -> {
					int indice=Modelo.get().getIndiceSitio(sitioSeleccionado);
					mareaVisor.setIndiceSitio(indice);
					sitioCambiado(indice);
					guardarPosicion();
				}
				).show();
    }

	public void elegirFecha() {
		Calendar calendar = Calendar.getInstance();

		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);

		DatePickerDialog datePickerDialog = new DatePickerDialog(
				this,
				(view, selectedYear, selectedMonth, selectedDay) -> {

					Calendar cal = Calendar.getInstance();
					cal.set(Calendar.YEAR, selectedYear);
					cal.set(Calendar.MONTH, selectedMonth);       // Ojo: enero = 0
					cal.set(Calendar.DAY_OF_MONTH, selectedDay);

					Date fechaElegida = cal.getTime();
					cambiarFecha(getIndiceSitio(), fechaElegida);
					Log.i("X", "Elegida fecha " + fechaElegida);

				},
				year, month, day
		);
		datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
		calendar.add(Calendar.YEAR, 1);
		datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
		datePickerDialog.show();
	}
    
    private void cambiarFecha(int position, Date fecha) {
    	Modelo modelo=Modelo.get();
    	setFechaVista(fecha);
    	mareaVisor.refresh();
    }        
	
    public int getIndiceSitio() {
    	return mareaVisor.getIndiceSitio();
    }
    
}