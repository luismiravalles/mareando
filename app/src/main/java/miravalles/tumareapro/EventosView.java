package miravalles.tumareapro;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import miravalles.tumareapro.domain.AemetInfo;

public class EventosView extends View {


	MareaInfo info;
	private int width;
	private int height;


	public float getAltura() {
		return 0;
	}

	public void setInfo(MareaInfo info) {
		this.info=info;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		this.width=w;
		this.height=h;
		Log.i("EV", "Establecido alto en " + h);
	}

	public EventosView(Context context) {
		super(context);		
	}
	

	public int getTextSize() {
		return height / 6;
	}
	
	public int getMargenIzquierdo() {
		final int textSize=getTextSize();
		final int margenTexto=textSize*8;
		return margenTexto;
	}



	@Override
	protected void onDraw(Canvas canvas) {

		if(info==null) {
			return; // Aun no tenemos datos...
		}
		pintarFondoTransicion(canvas);
		pintarEventoAnterior(canvas);
		pintarEventoSiguiente(canvas);
		pintarEstadoActual(canvas);
	}
	

	private void pintarFondoTransicion(Canvas canvas) {
		int izquierdo;
		int derecho;
		if(info.getIntAlturaAnterior()>info.getIntAlturaSiguiente()) {
			izquierdo=Estilo.FONDO_MAREA_ALTA;
			derecho=Estilo.FONDO_MAREA_BAJA;
		} else {
			izquierdo=Estilo.FONDO_MAREA_BAJA;
			derecho=Estilo.FONDO_MAREA_ALTA;
		}
		LinearGradient g=new LinearGradient(
				getMargenIzquierdo(), 0, getWidth(), 0,
				izquierdo, derecho, TileMode.CLAMP);
		Paint paint=new Paint();
		paint.setShader(g);
		CanvasUtil.drawRect(canvas,0, 0, getWidth(), height, paint, CanvasUtil.paintBorde());
		Paint paintRaya=new Paint();
		paintRaya.setStrokeWidth(4);
		paintRaya.setColor(Estilo.TEXTO_BLANCO);
		canvas.drawLine(0, 0, getWidth(), 0, paintRaya);
	}

	SimpleDateFormat diaMesFormat=new SimpleDateFormat("dd MMM");
	
	void pintarEstadoActual(Canvas canvas) {
		Paint paint=getPaintEstadoActual();
		paint.setTextAlign(Align.CENTER);
		
		Date ahora=new Date();
		if(DateUtils.isToday(info.hora.getTime())) {
			canvas.drawText(
					info.getHora(), 
						width/2,
						paint.getTextSize(),
					paint);
			canvas.drawText(
					getContext().getString(info.getEstado()),
						width/2,
						 2 *  paint.getTextSize(),
					paint);
			
		} else {
			// ES OTRO DIA VAMOS A PINTAR EL DIA Y LA HORA
			paint=getPaintEstadoActual();
			paint.setTextAlign(Align.CENTER);
			paint.setTextSize(paint.getTextSize() * 3 / 4);
			
			String texto=null;
			if(Util.isManana(info.hora)) {
				texto=getResources().getString(R.string.tomorrow);
			} else {
				texto=diaMesFormat.format(info.hora);
			}
			canvas.drawText(
					texto,
					width/2,
					paint.getTextSize()*1,paint);
			canvas.drawText(
						info.getHora(),
						width/2,
						paint.getTextSize()*2,paint);
			canvas.drawText(
					getContext().getString(info.getEstado()), 
						width/2,
						paint.getTextSize()*3,paint);
						
		}
	}


	Paint getPaintEstadoActual() {
		Paint paint=new Paint();
		int textSize=getTextSize()*3/2;
		paint.setTextSize(textSize);
		paint.setColor(Estilo.COLOR_ACTUAL);
		paint.setTypeface(Typeface.DEFAULT_BOLD);
		paint.setAntiAlias(true);
		return paint;
	}
	
	
	Paint getPaintHoraEventos() {
		Paint paint=new Paint();
		int textSize=getTextSize();
		paint.setTextSize(textSize*6/4);
		paint.setColor(Estilo.TEXTO_BLANCO);
		paint.setTypeface(Typeface.DEFAULT_BOLD);
		paint.setAntiAlias(true);
		return paint;
	}
	
	Paint getPaintEstadoEventos() {
		Paint paint=new Paint();
		int textSize=getTextSize();
		paint.setTextSize(textSize);
		paint.setColor(Estilo.TEXTO_BLANCO);
		paint.setTypeface(Typeface.DEFAULT_BOLD);
		paint.setAntiAlias(true);
		return paint;
	}
	
	void pintarEventoAnterior(Canvas canvas) {
		Paint paint=getPaintHoraEventos();
		Paint paintEstado=getPaintEstadoEventos();
		if(info.estaSubiendo()) {
			paint.setColor(Estilo.COLOR_TEXTO_HORA_BAJAMAR);
			paintEstado.setColor(Estilo.COLOR_TEXTO_BAJAMAR);
		} else {
			paint.setColor(Estilo.COLOR_TEXTO_HORA_PLEAMAR);
			paintEstado.setColor(Estilo.COLOR_TEXTO_PLEAMAR);
		}
		
		int pad=4;
		
		canvas.drawText(
				info.getHoraAnterior(),
				pad, paint.getTextSize(),
				paint);
		
		
		canvas.drawText(
				getContext().getString(info.getNombreAnterior()),
				pad, 

					paint.getTextSize()+paintEstado.getTextSize(),
				paintEstado);
				
	}
	
	void pintarEventoSiguiente(Canvas canvas) {
		int pad=4;
		Paint paint=getPaintHoraEventos();
		Paint paintEstado=getPaintEstadoEventos();
		if(info.estaSubiendo()) {
			paint.setColor(Estilo.COLOR_TEXTO_HORA_PLEAMAR);
			paintEstado.setColor(Estilo.COLOR_TEXTO_PLEAMAR);
		} else {
			paint.setColor(Estilo.COLOR_TEXTO_HORA_BAJAMAR);
			paintEstado.setColor(Estilo.COLOR_TEXTO_BAJAMAR);
		}
		
		paint.setTextAlign(Align.RIGHT);
		canvas.drawText(
				info.getHoraSiguiente(),
				this.width-pad, paint.getTextSize(),
				paint);
		
		
		paintEstado.setTextAlign(Align.RIGHT);
		canvas.drawText(
				getContext().getString(info.getNombreProximo()),
				this.width-pad, 
				paint.getTextSize()+paintEstado.getTextSize(),
				paintEstado);		
	}

	private SimpleDateFormat sdfHoraMinuto=new SimpleDateFormat("HH:mm");
	


}
