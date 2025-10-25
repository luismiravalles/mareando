package miravalles.tumareapro.vo;

/**
 * Localizaci�n global. En vez de usar latitud y longitud, cosas que me confunden
 * utilizar� x,y.
 * 
 * Donde y=Latitud x=Longitud.
 * @author Luis
 *
 */
public class GeoLocalizacion {

	// Latitud, es decir lo cerca o lejos del Ecuador
	private double y;

	// La Longitud, lo cerca o lejos del Meridiano cero
	private double x;
	
	
	public double y() {
		return y;
	}
	public double latitud(){return y;}
	public void setY(double y) {
		this.y = y;
	}
	public double x() {
		return x;
	}
	public double longitud(){return x;}
	public void setX(double x) {
		this.x = x;
	}
	
	
	/**
	 * Creaci�n del objeto, donde y=latitud, x=longitud.
	 * @param y
	 * @param x
	 */
	public GeoLocalizacion(double y, double x) {
		this.y=y;
		this.x=x;
	}

	/**
	 * Numeramos Zonas 1=Norte, 2=Sur, 3=Canarias a efectos de
	 * facilitar la ordenación.
	 * @return
	 */
	public int getZona() {
		if(isSurPeninsular()) {
			return 2;
		} else if(isCanarias()) {
			return 3;
		} else {
			return 1;
		}
	}

	public boolean isSurPeninsular() {
		return y < 40.0;
	}

	public boolean isCanarias() {
		return y < 30.0;
	}
	
}
