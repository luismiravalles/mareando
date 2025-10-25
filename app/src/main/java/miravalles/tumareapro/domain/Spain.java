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


	public Sitio[] getSitios() {
		Sitio [] basicos=		
			new Sitio[] {
		//new Sitio("Ribadeo", 280, 40, -13, -1),
		new Sitio("Guarda", "vigo", "3602305", new GeoLocalizacion(41.9025171,-8.8736718),  -3, -10, -4),
		new Sitio("Baiona", "vigo", "3600303",  new GeoLocalizacion(42.1181052,-8.8497045),  -3, -6, -5),
		new Sitio("Vigo", "3605701", new GeoLocalizacion(42.2313564,-8.7124471), +16, +6),
		
		
		new Sitio("Mar�n", "vigo", "3602601", new GeoLocalizacion(42.3914686,-8.6998529), +2, +14, +6),
		new Sitio("San Xenxo", "vigo", "3605105", new GeoLocalizacion(42.3994787,-8.8069274), +2, 0, -9),
		new Sitio("Vilagarcia", "vigo", "3606102",  new GeoLocalizacion(42.5938693,-8.7659623), 0,  +16, +8),
		new Sitio("Portosin", "vigo", "1507101", new GeoLocalizacion(42.7567997,-8.9483123), +5,  +8, +10),
		new Sitio("Fisterra", "vigo", "1503701", new GeoLocalizacion(42.9050171,-9.2643961), -4, -20, -10),
		new Sitio("Camarinas", "vigo", "1501602", new GeoLocalizacion(43.1286027,-9.184966), +11, +12, +5),
		new Sitio("Malpica", "vigo", "1504302", new GeoLocalizacion(43.3241627,-8.8093119), +8, +11, -6),
		
		
		new Sitio("Coruna","1503005", new GeoLocalizacion(43.3708731,-8.395835), +45, +25 ),
		new Sitio("Sada", "coruna", "1507507", new GeoLocalizacion(43.350928,-8.2546106), +4,  +36, +15),

		new Sitio("Cedeira").aemet("1502202").geo(43.66142,-8.0554694).idIHM(17),
		new Sitio("Ferrol").aemet("1503603").geo(43.4883872,-8.2225093).idIHM(18),


		new Sitio("Carino", "ferrol", "1590101", new GeoLocalizacion(43.7326479,-7.8777663), +3, +17, -2),
		new Sitio("Burela", "ferrol", "2790201", new GeoLocalizacion(43.6559434,-7.3635145), +4, +43, +19 ),
		new Sitio("Foz", "ferrol", "2701901", new GeoLocalizacion(43.5725022,-7.2613973), +8, +16, -11),
		new Sitio("Tapia", "aviles", "3307007", new GeoLocalizacion(43.5702629,-6.9433305), +0, +25, +11),
		new Sitio("Navia", "aviles", "3304101", new GeoLocalizacion(43.5400759,-6.7232064), 0, +25, +11),
		new Sitio("Luarca", "aviles", "3303407", new GeoLocalizacion(43.542322,-6.5360659), -3, +11, +2),
		new Sitio("Cudillero", "aviles","3302120", new GeoLocalizacion(43.563321,-6.144876), -2, +11, +2,
				new Foto(R.drawable.concha_1m, 100),
				new Foto(R.drawable.concha_3m, 300),				
				new Foto(R.drawable.concha_4m, 400)),
		//new Sitio("San Esteban", 390, 0, -8, -1,
		//		new Foto(R.drawable.sanesteban_300m, 300)),

		new Sitio("Avil�s", "3301608", new GeoLocalizacion(43.55571,-5.9276413), 0, +12,
				new Foto(R.drawable.aviles_3m, 300)),
			
		//new Sitio("Luanco", 	440, -5,  -2,  -4,
		//		new Foto(R.drawable.candas_2m, 200)
		//		),
		
		new Sitio("Luanco", "gijon", "3302509", new GeoLocalizacion(43.6146284,-5.7933439), 2,   0,   0),
		new Sitio("Cand�s", "gijon", "3301403", new GeoLocalizacion(43.5912816,-5.7657528), 2,   0,   0),
		
		new Sitio("Gij�n", 	"3302403", new GeoLocalizacion(43.5452608,-5.6619264), +28, +11,
				new Foto(R.drawable.gijon_1m, 100)),
		//new Sitio("El Puntal",	500, 10,   1,   0,
		//		new Foto(R.drawable.rodiles_2m, 200)),
		new Sitio("Lastres", "gijon", "3301903", new GeoLocalizacion(43.5135613,-5.2694475), -1,   0,   0,
				new Foto(R.drawable.lastres_197cm, 197)) ,
		new Sitio("Ribadesella", "gijon", "3305601", new GeoLocalizacion(43.4617192,-5.0587802), +8, +18, +5,
				new Foto(R.drawable.ribadesella_074, 74),
				new Foto(R.drawable.ribadesella_380, 380)),

		new Sitio("Llanes", "gijon", "3303626", new GeoLocalizacion(43.4195333,-4.7509136), +8, +28 , +11,
				new Foto(R.drawable.torimbia_1m, 100),
				new Foto(R.drawable.torimbia_1m, 100),
				new Foto(R.drawable.toro_382, 382),
				new Foto(R.drawable.llanes_345cm, 345)),

		new Sitio("San Vicente", "santander", "3908004", new GeoLocalizacion(43.388258,-4.399976), -2, +13, +16),
				
				
				
		new Sitio("Santander",	"3907506", new GeoLocalizacion(43.4609602,-3.8079336), +19, +16),
		
		new Sitio("Laredo", "santander", "3903502", new GeoLocalizacion(43.41041,-3.416939), -2,   +19,  +16),
		new Sitio("Castro Urdiales", "bilbao", "3902002", new GeoLocalizacion(43.381901,-3.219427), -8,  +30,  +23),
				
		new Sitio("Bilbao",		"4807101", new GeoLocalizacion(43.2569629,-2.9234409), 33, +24),
		new Sitio("Pasajes",	"2006904", new GeoLocalizacion(43.3252778,-1.9211111),
				new Foto(R.drawable.pasajes_000, 0)
				),
		new Sitio("Ayamonte",	"2101001", new GeoLocalizacion(37.220547,-7.405674)),
		new Sitio("Isla Canela", "ayamonte", "2101001", new GeoLocalizacion(37.168,-7.372), -2, +18, +10 ),
		new Sitio("Isla Cristina","ayamonte", "2104202", new GeoLocalizacion(37.2005266,-7.3233035), -2, 0, 0),
		new Sitio("Huelva", "2104101", new GeoLocalizacion(37.2708665,-6.95)),			
		new Sitio("Punta Umbr�a",  "ayamonte","2106002",  new GeoLocalizacion(37.1852585,-6.9704976), -2, 0, 0),
		new Sitio("Mazag�n", "ayamonte","2105501", new GeoLocalizacion(37.134306,-6.804948), -2, +35, +20,
				new Foto(R.drawable.mazagon_000, 0)),		
		
		new Sitio("Bonanza", "1103203", new GeoLocalizacion(36.7725774,-6.3529689))
				.escala(93, 80) // Datos de Escala extrapolados de la muestra de un �nico d�a 14.11.06
								// Pendiente de calcular el ajuste de forma m�s rigurosa.
				,
		
		
		new Sitio("Sevilla","1103203", new GeoLocalizacion(37.3880961,-5.9823299))
			.desfasar(-70)	// Desfase tomado en un dia 14.11.2016, pendiente
							// de hacer un c�lculo m�s riguroso.
			.escala(93, 80) // Misma escala que Bonanza a ojo. Falta m�s rigurosidad
		,
	
		new Sitio("Chipiona", "1101604", new GeoLocalizacion(36.41,-6.4288169) ),
		new Sitio("Rota", "1103006", new GeoLocalizacion(36.62629,-6.362981)),
		new Sitio("Puerto Sta Mar�a", "rota","1101201",  new GeoLocalizacion(36.600595,-6.2329094), -4, +7, 0),
		new Sitio("C�diz","1101201", new GeoLocalizacion(36.5270612,-6.2885962))
			.desfasar(+10),
		new Sitio("La Carraca", "cadiz", "1101201",
					new GeoLocalizacion(36.5069533,-6.265359), -12,  16, 0),
		//new Sitio("Gallineras" "cadiz", , new GeoLocalizacion(36.5270612,-6.2885962)),
		new Sitio("Sancti Petri", "cadiz", "1101503", 
				new GeoLocalizacion(36.4101251,-6.1102943), -0, -10, -5),
		new Sitio("Conil", "cadiz","1101406",  
				new GeoLocalizacion(36.2776845,-6.087779), -19, -15, -11),
		new Sitio("Barbate","cadiz", "1100706", 
				new GeoLocalizacion(36.1900204,-5.9224799), -29, -88 ,-10 ),
		
		new Sitio("Tarifa", "cadiz", "1103504", 
			new GeoLocalizacion(36.018776,-5.600819), 0, 0, 0)
				.escala(47, 47)
				.desfasar(-34, -58)
		
		,		
		new Sitio("Algeciras", "cadiz", "1100401", 
			new GeoLocalizacion(36.1329769,-5.453909), 0 , 0, 0)
				.escala(36, 37)
				.desfasar(-20, -40)
		,
		new Sitio("Ceuta", "5100104", new GeoLocalizacion(35.88,-5.37)),
		new Sitio("Melilla", "ceuta", "5200103", new GeoLocalizacion(35.26,-2.95),0,0,0),
		new Sitio("M�laga","2906707",  new GeoLocalizacion(36.76475,-4.5642754)),
		
		
		
		new Sitio("Santa Cruz de Tenerife", "3803801", new GeoLocalizacion(28.4578159,-16.3213542)),
		new Sitio("Las Palmas de Gran Canaria", 
				"santacruzdetenerife","3501601", new GeoLocalizacion(28.139, -15.42), -2, +13, +0),



		
//		new Sitio("Arrecife", new GeoLocalizacion(28.961573,-13.551516)),
//		new Sitio("Puerto del Rosario", new GeoLocalizacion(28.498801,-13.860104)),
//		new Sitio("Morro Jable", new GeoLocalizacion(28.053068,-14.350415)),		
//		new Sitio("Puerto de la luz", new GeoLocalizacion(28.1350349,-15.4215569)),
//		new Sitio("Arinaga", new GeoLocalizacion(27.8584838,-15.3943618)),
//		new Sitio("Pasito blanco", new GeoLocalizacion(27.7481833,-15.6247581)),
//		new Sitio("Puerto de las nieves", new GeoLocalizacion(27.7481833,-15.6247581)),		
//		new Sitio("Sta Cruz de Tenerife", new GeoLocalizacion(28.4636296,-16.2518467)),
//		new Sitio("Granadilla",new GeoLocalizacion(28.1222699,-16.5768911)),
//		new Sitio("Los Cristianos",new GeoLocalizacion(28.05,-16.7166667)),
//		new Sitio("Los Gigantes",new GeoLocalizacion(28.2412559,-16.8403286)),
//		new Sitio("Puerto de la Cruz", new GeoLocalizacion(28.411413,-16.5449611)),
//		new Sitio("SS de la Gomera",new GeoLocalizacion(28.0937998,-17.1098582)),
//		new Sitio("Sta Cruz de la Palma", new GeoLocalizacion(28.6839885,-17.7645747)),
//		new Sitio("Puerto de La Estaca",new GeoLocalizacion(27.785768,-17.903419))
		};		
		List<Sitio> result=new ArrayList<Sitio>();
		result.addAll(filtrarExistentes(Arrays.asList(basicos)));
		return result.toArray(new Sitio[0]);
	};	

	private static List<Sitio> filtrarExistentes(List<Sitio> sitios) {
		Set<String> sitiosExistentes=new HashSet<String>();

		sitiosExistentes.add("santander");
		sitiosExistentes.add("aviles");
		sitiosExistentes.add("gijon");
		sitiosExistentes.add("bilbao");
		sitiosExistentes.add("marin");
		sitiosExistentes.add("coruna");
		sitiosExistentes.add("ferrol");
		sitiosExistentes.add("ayamonte");
		sitiosExistentes.add("laCarraca");
		sitiosExistentes.add("vigo");
		sitiosExistentes.add("pasajes");
		sitiosExistentes.add("bonanza");
		sitiosExistentes.add("sevilla");
		sitiosExistentes.add("ceuta");
		sitiosExistentes.add("cadiz");
		sitiosExistentes.add("chipiona");
		sitiosExistentes.add("rota");
		sitiosExistentes.add("huelva");
		sitiosExistentes.add("malaga");
		sitiosExistentes.add("santacruzdetenerife");

	
		
			
		
		ArrayList<Sitio> filtrado=new ArrayList<Sitio>();
		for(Sitio sitio:sitios) {
			if(sitiosExistentes.contains(sitio.getNombreNormalizado()) 
				|| sitiosExistentes.contains(sitio.getEquivalente())	
					) {
				filtrado.add(sitio);
			}
		}
		return filtrado;
	}
	
	
}
