package miravalles.tumareapro;

import miravalles.tumareapro.R;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class CrearSitioActivity extends Activity implements LocationListener {
	
	private EditText nombreNuevoSitio;
	private EditText coordenadas;
	
	private final static String ESPERE="Obteniendo coordenadas...";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.crear_sitio);
		nombreNuevoSitio=(EditText)findViewById(R.id.nombreNuevoSitio);
		coordenadas=(EditText)findViewById(R.id.coordenadas);
		coordenadas.setText(ESPERE);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		activarGps();
	}
	
	public void activarGps() {
    	LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    	locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 
    			3000, 0, this);
    	locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 
    			3000, 0, this);
    	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 
    			3000, 0, this);	    			
	}
	
	public void noMasGps() {
    	LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    	locationManager.removeUpdates(this);
	}
	
	
	public void aceptar(View view) {
		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		
		if(!coordenadas.getText().toString().matches("^-*[0-9]+\\.[0-9]+;-*[0-9]+\\.[0-9]+")) {
			Toast.makeText(this, "Coordenadas incorrectas", Toast.LENGTH_LONG).show();
			return;
		}
		
		Modelo.get().crearSitio(nombreNuevoSitio.getText().toString(),
				coordenadas.getText().toString());
		setResult(RESULT_OK);
		noMasGps();
		finish();
	}
	
	
	public void cancelar(View view) {
		setResult(RESULT_CANCELED);
		noMasGps();
		finish();
	}


	public void onLocationChanged(Location location) {
		if(coordenadas.getText()==null || coordenadas.getText().toString().equals("")
			|| coordenadas.getText().toString().equals(ESPERE)) {
			coordenadas.setText("" + location.getLatitude() + ";" + location.getLongitude());
		}
	}


	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub
		
	}


	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub
		
	}


	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		
		
	}
	
	
}
