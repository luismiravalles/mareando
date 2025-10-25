package miravalles.tumareapro.domain;

import miravalles.tumareapro.vo.GeoLocalizacion;
import miravalles.tumareapro.R;

public class England implements Pais {

	public Sitio[] getSitios() {
		
		Sitio[] sitios=new Sitio[] {
		

			
			// new Sitio("Avonmouth", 50.8184381,-1.1678308), Tiene las mareas locas.
				
			new Sitio("Barrow-in-furness", 54.1003583,-3.2928292).escala(98, 100),
			new Sitio("Liverpool",53.4198855,-2.9808854),			
			new Sitio("Beachley (aust)", 51.6192761,-2.6607578).escala(121, 52),
			// new Sitio("Immingham", 53.6122011,-0.2421095), Alturas desbocadas

			
			
			
			new Sitio("Falmouth", null, new GeoLocalizacion(50.166,-5.092639),  
					new Foto(R.drawable.falmouth_1m, 1)
				).escala(104, 85),			
			new Sitio("Plymouth Devon Port",50.3770081,-4.1849667),
			new Sitio("Portland",50.5671881,-2.4547898),
			new Sitio("Southampton",50.9167317,-1.4705322),
			new Sitio("Portsmouth", 50.8184381,-1.1678308),
			new Sitio("Shoreham", 50.8375888,-0.2911642),
		

			
			new Sitio("Margate",51.3770211,1.348057),
			//new Sitio("Sheerness", 51.4358033,0.7458255),			
			new Sitio("Chatham", 51.40, 0.5500).escala(145, 20),			
			new Sitio("London Bridge", 51.5078788,-0.0899208),
			new Sitio("Waltononthenaze",51.8549417,1.2512852),
			new Sitio("Harwich", 51.933011,1.2282875).escala(104, 120),			
			new Sitio("Lowestoft",52.4758526,1.690296),
			new Sitio("Hull",53.7663622,-0.4020249),					
			new Sitio("Rivertees", 54.633, -1.166),
			new Sitio("River Tyne Entrance", 55.008272, -1.419164),
			new Sitio("Northshields",55.0134649,-1.4971704),
	
			
			
			
		};

		return sitios;
	}
}
