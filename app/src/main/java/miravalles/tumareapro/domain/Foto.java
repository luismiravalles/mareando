package miravalles.tumareapro.domain;

import android.util.Log;

public class Foto {
	

	private static  final int COD_FOTO_EXTERNA=-1;
	
	public int codFoto;
	public int centimetros;
	
	public Foto(int codFoto, int centimetros) {
		this.codFoto=codFoto;
		this.centimetros=centimetros;
	}
	
	public Foto(int centimetros) {
		this(COD_FOTO_EXTERNA, centimetros);
	}
	
	public boolean isExterna() {
		return (codFoto==COD_FOTO_EXTERNA);
	}
	
	public String getNombreExterna(Sitio sitio) {
		String result=sitio.getNombreNormalizado() + "-" + centimetros + ".jpg";
		Log.i("F", result);
		return result;
	}
}
