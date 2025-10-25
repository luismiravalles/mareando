package miravalles.tumareapro;

import android.app.Activity;
import android.view.Display;
import miravalles.tumareapro.R.drawable;

public class Mapa {
	private int imagen;
	private double minLongitud;
	private double maxLongitud;
	private double minLatitud;
	private double maxLatitud;
	private int	   yTexto; 	// Zona donde poder colocar el texto en %
	
	private static int ALTO_MAPA_ORIGINAL = 249;
	
	public int getyTexto() {
		return yTexto;
	}
	
	private static final double Y_IBERIANORTE = 41.94;
	private static final double RANGO_Y =  43.86 - Y_IBERIANORTE;
	private static final double Y_IBERIASUR = 35.048;
	private static final double Y_IBERIASUR_TOP = 38.07;
	private static final double Y_CANARIAS = 27.518;
	
	private static final double Y_ENGLAND = 49.92;
	private static final double Y_ENGLAND_TOP = 59.45;
	private static final double X_ENGLAND = -6.64;
	private static final double X_ENGLAND_RIGHT=1.80;
	


	private static Mapa mapas[] = {
		
			// 43.86 - 41.75 = 
			new Mapa(0, 0.0, 0.0, 0.0, 0.0, 7 ),
			new Mapa(drawable.iberianorte , Y_IBERIANORTE, Y_IBERIANORTE+RANGO_Y, -9.51, -1.26, 7 ),
			new Mapa(drawable.iberiasur, 	Y_IBERIASUR, Y_IBERIASUR+(RANGO_Y*1.468), -9.1, -1.05, 0),
			new Mapa(drawable.canarias, Y_CANARIAS, Y_CANARIAS+RANGO_Y, -18.94, -10.66, 0),
			new Mapa(drawable.england, Y_ENGLAND, Y_ENGLAND_TOP, X_ENGLAND, X_ENGLAND_RIGHT, 0)
	};
	
	public Mapa(int imagen, double minLatitud, double maxLatitud, double minLongitud, double maxLongitud, int yTexto) {
		this.imagen=imagen;
		this.minLongitud=minLongitud;
		this.maxLongitud=maxLongitud;
		this.minLatitud=minLatitud;
		this.maxLatitud=maxLatitud;
		this.yTexto=yTexto;
	}
	
	
	
	public static Mapa getMapa(double latitud)  {
		for(Mapa mapa:mapas) {
			if(latitud>=mapa.minLatitud && latitud<mapa.maxLatitud) {
				return mapa;
			}
		}
		return mapas[0];				
	}
	
	public int getResourceId() {
		return imagen;
	}

	public double getRangoLatitud() {
		return 	(maxLatitud) - getMinLatitud();
	}
	
	public double getRangoLongitud() {
		return maxLongitud - getMinLongitud();
	}
	
	public double getMinLatitud() {
		return (minLatitud);
	}
	
	public double getMinLongitud() {
		return minLongitud;
	}	
	
	
	public static double getBordeInferiorPx(Activity contexto) {
    	Display display = contexto.getWindowManager().getDefaultDisplay(); 
    	double escalado = (double)display.getWidth() / (double)800;    	
		return 180 * escalado;
	}
	

}
