package miravalles.tumareapro;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.function.Consumer;

import miravalles.tumareapro.domain.Sitio;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TimePicker;

public class ElegirSitio  {
	

	Sitio sitio;
	Spinner listaSitios;
	
	List<Sitio> sitiosAlfa;

	AlertDialog.Builder builder;
	

	public ElegirSitio(final Context context, Sitio sitio, Consumer<Sitio> alAceptar) {
		builder=new AlertDialog.Builder(context);
		builder.setTitle("Elegir Sitio");

		LayoutInflater inflater=LayoutInflater.from(context);
		View dialogView=inflater.inflate(R.layout.elegir_sitio, null);
		builder.setView(dialogView);

		listaSitios=dialogView.findViewById(R.id.sitio);
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
				context,android.R.layout.simple_spinner_dropdown_item,
				nombres));
		listaSitios.setSelection(seleccionado);
		this.sitio=sitio;

		builder.setPositiveButton("Aceptar", (dialog, which) -> {
			int item=listaSitios.getSelectedItemPosition();
			alAceptar.accept(sitiosAlfa.get(item));
		});
	}


	public void show() {
		builder.show();
	}
	

	
}
