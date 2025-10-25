package miravalles.tumareapro;

import android.content.Context;


public class Config {

	private static boolean england=false;
	private static boolean versionPro=true;
	
	public static void init(Context context) {
		if(context.getPackageName().startsWith("miravalles.mytide")) {
			england=true;
		}
		if(context.getPackageName().startsWith("miravalles.tumareapro")) {
			versionPro=true;
		}		
	}
	
	/**
	 * Retorna el ultimo a�o incluido En england 2017
	 * en Espa�a 2025.
	 * @return
	 */
	public static int maxAny() {
		if(isEngland()) {
			return 2017;
		} else {
			return 2025;
		}
	}
	
	public static boolean isEngland() {
		return england;
	}
	
	public static int maxAltura() {
		if(isEngland()) {
			return 1300;
		} else {
			return 650;
		}
	}
	
	public static boolean isVersionPro() {
		return versionPro;
	}
	
}
