package miravalles.tumareapro;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import miravalles.tumareapro.domain.Sitio;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TimePicker;

public class ElegirSitio extends Dialog {
	
	Date fecha;
	Sitio sitio;
	DatePicker datePicker;
	TimePicker timePicker;
	Spinner listaSitios;
	
	List<Sitio> sitiosAlfa;
	

	public ElegirSitio(Context context, Date fecha, Sitio sitio) {
		super(context);		
		this.fecha=fecha;
		this.sitio=sitio;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.elegir_sitio);
		//datePicker=(DatePicker)findViewById(R.id.datePicker);
		//timePicker=(TimePicker)findViewById(R.id.timePicker);
		listaSitios=(Spinner)findViewById(R.id.sitio);
		GregorianCalendar gc=new GregorianCalendar();
		gc.setTime(fecha);

		/*
			Es posible que no quiera definir el datePicker porque no quiero
			permitir un cambio de fecha, quizás lo permita en un diálogo separado.
		 */
		if(datePicker!=null) {
			datePicker.updateDate(gc.get(gc.YEAR), gc.get(gc.MONTH), gc.get(gc.DAY_OF_MONTH));
			timePicker.setIs24HourView(true);
			timePicker.setCurrentHour(gc.get(gc.HOUR_OF_DAY));
			timePicker.setCurrentMinute(gc.get(gc.MINUTE));
		}
		
		
		sitiosAlfa=Modelo.get().getListaSitiosAlfa();
		String []nombres=new String[sitiosAlfa.size()];
		int seleccionado=0;
		for(int i=0; i<nombres.length; i++) {
			if(sitio==sitiosAlfa.get(i)) {
				seleccionado=i;
			}
			nombres[i]=sitiosAlfa.get(i).nombre;
		}
		listaSitios.setAdapter(new ArrayAdapter<String>(
					getContext(),android.R.layout.simple_spinner_dropdown_item, 
					nombres));
		listaSitios.setSelection(seleccionado);

		// setOnShowListener(d -> listaSitios.post(() -> listaSitios.performClick()));
	}
	
	public Date getFecha() {
		if(datePicker==null) {
			return new Date();
		}
		GregorianCalendar gc=new GregorianCalendar(
				datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth()
			, timePicker.getCurrentHour(), timePicker.getCurrentMinute());
		return gc.getTime();
	}
	
	public Sitio getSitioSeleccionado() {
		int seleccionado=listaSitios.getSelectedItemPosition();
		return sitiosAlfa.get(seleccionado);
	}
	
	

	
}
