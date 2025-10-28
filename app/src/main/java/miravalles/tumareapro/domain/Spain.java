package miravalles.tumareapro.domain;

import android.content.Context;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import miravalles.tumareapro.vo.GeoLocalizacion;
import miravalles.tumareapro.R;

public class Spain implements Pais {


	public Sitio[] getSitios(Context contexto) {
		List<Sitio> sitios=new ArrayList<Sitio>();
		try(InputStream in=contexto.getAssets().open("estaciones.xml")) {
			Log.i("X","Cargando estaciones");
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(in);
			doc.getDocumentElement().normalize();
			Element raiz=doc.getDocumentElement();
			NodeList estaciones=raiz.getElementsByTagName("estacion");
			for(int i=0; i<estaciones.getLength(); i++) {
				// Log.i("X", "Cargando estación");
				Element estacion=(Element)estaciones.item(i);
				GeoLocalizacion geo=new GeoLocalizacion(
						Double.parseDouble(estacion.getAttribute("lat")),
						Double.parseDouble(estacion.getAttribute("lon")));
				Sitio sitio=new	Sitio(estacion.getAttribute("puerto"))
						.idIHM(Integer.parseInt(estacion.getAttribute("id")))
						.geo(geo);
				String aemet=estacion.getAttribute("aemet");
				if(aemet!=null) {
					sitio.aemet(aemet);
				}
				sitios.add(sitio);
			}
		} catch(Exception e) {
			Log.e("X", e.getMessage());
		}
		ordenar(sitios);
		return sitios.toArray(new Sitio[0]);
	}

	private void ordenar(List<Sitio> sitios) {
		Collections.sort(sitios, (a,b) -> {
			int difZona=a.getGeo().getZona() - b.getGeo().getZona();
			if(difZona!=0) {
				return difZona;
			}
			if(a.getGeo().longitud() > b.getGeo().longitud()) {
				return 1;
			} else if(a.getGeo().longitud() < b.getGeo().longitud()) {
				return -1;
			} else {
				return 0;
			}
		});
	}



}
