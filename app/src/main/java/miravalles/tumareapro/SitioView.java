package miravalles.tumareapro;

import java.text.SimpleDateFormat;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;

public class SitioView extends Button {
	
	final SimpleDateFormat sdf=new SimpleDateFormat("dd MMM");	
	

	public SitioView(final Context context, Modelo modelo) {
		super(context);
		setBackgroundResource(R.drawable.boton_sin_fondo_selector);
		setTag("Sitio");
		((Activity)context).registerForContextMenu(this);
		
		setGravity(Gravity.CENTER_HORIZONTAL);
		new Sizer().set(this).fillWidth().wrapHeight();
		setTextColor(0xFFFFFFFF);		
		//sitio.setBackgroundColor(0xFF002244);
		
		setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				((TuMareaActivity)getContext()).elegirSitio();
			}			
		});
		
		
	}
	

    private TuMareaActivity getContexto() {
    	return ((TuMareaActivity)getContext());
    }



    
    public void mostrarSitio(int position) {
//		int textSizeSitio=TEXT_SIZE_SITIO;
//		if(Modelo.get().getNombreSitio(position).length() < 10) {
//			textSizeSitio++;
//		} else if(Modelo.get().getNombreSitio(position).length() > 15) {
//			textSizeSitio--;
//		}
//		setTextSize(TypedValue.COMPLEX_UNIT_PX, 
//				Util.widthPct(textSizeSitio, this));
//    	
//		this.setText(
//				Html.fromHtml(
//						Modelo.get().getNombreSitio(position) + " - "
//					  + "<small>" + sdf.format(
//							  getContexto().getFechaVista()) + "</small>"
//		));    	
    }
    

}
