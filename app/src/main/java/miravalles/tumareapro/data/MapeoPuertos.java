package miravalles.tumareapro.data;


public class MapeoPuertos implements MapeoPuertosAnual {
	
	
	
	public String getNombreNormalizado(String nombre) {
		String nom=nombre.toLowerCase()
				.replaceAll("�","e")
				.replaceAll("�","i")
				.replaceAll("�","o")
				.replaceAll("�","a")
				.replaceAll("�","n")
				.replaceAll("\\.","")
				.replaceAll("[^A-Za-z0-9_]", "")
				.replaceAll(" " , "");

		return nom;
	}	
	
	public String capitalizado(String nombre) {
		return nombre.substring(0,1).toUpperCase() + 
				nombre.substring(1);
	}
	
	public String getNombreClase(String puerto) {
		return capitalizado(getNombreNormalizado(puerto));
	}

}
