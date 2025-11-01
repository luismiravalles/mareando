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
import android.graphics.Path;
import android.graphics.Point;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import miravalles.tumareapro.domain.AemetInfo;

public class EventosView extends View {


	MareaInfo info;
	private int width;
	private int height;

	private Paint paintOnda;

	MareaInfo infoSiguiente;


	public float getAltura() {
		return 0;
	}

	public void setInfo(MareaInfo info) {
		this.info=info;
		infoSiguiente=null;
		if(info!=null && info.siguiente!=null) {
			Date momentoSiguiente=new Date(info.siguiente.getTime() + 1);
			infoSiguiente=Modelo.get().getMareaInfo(
					info.getSitio(), momentoSiguiente);
		}
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

		paintOnda = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintOnda.setColor(Color.DKGRAY);
		paintOnda.setStrokeWidth(16f);
		paintOnda.setStyle(Paint.Style.STROKE);
	}
	

	public int getTextSize() {
		return height / 8;
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
		pintarOnda(canvas,info.getIntAlturaAnterior()-info.getIntAlturaSiguiente());


		pintarEvento(canvas, info.getHoraAnterior(), info.getNombreAnterior(), -1 );
		pintarEvento(canvas, info.getHoraSiguiente(), info.getNombreProximo(), 0);

		if(infoSiguiente!=null) {
			pintarEvento(canvas, infoSiguiente.getHoraSiguiente(),
							 infoSiguiente.getNombreProximo(), 1);
		}
		/*
		pintarEventoAnterior(canvas);
		pintarEventoSiguiente(canvas);
		*/

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

		float delta=(getHeight() - paint.getTextSize()*2)/2;
		int posx=width/4;
		
		Date ahora=new Date();
		if(DateUtils.isToday(info.hora.getTime())) {
			canvas.drawText(
					info.getHora(), 
						posx,
						delta+paint.getTextSize(),
					paint);
			canvas.drawText(
					getContext().getString(info.getEstado()),
						posx,
						 delta + 2 *  paint.getTextSize(),
					paint);
			
		} else {
			// ES OTRO DIA VAMOS A PINTAR EL DIA Y LA HORA
			//paint.setTextSize(paint.getTextSize() * 3 / 4);
			
			String texto=null;
			if(Util.isManana(info.hora)) {
				texto=getResources().getString(R.string.tomorrow);
			} else {
				texto=diaMesFormat.format(info.hora);
			}
			canvas.drawText(
					texto,
					posx,
					delta+paint.getTextSize()*1,paint);
			canvas.drawText(
						info.getHora(),
						posx,
						delta+paint.getTextSize()*2,paint);
			canvas.drawText(
					getContext().getString(info.getEstado()), 
						posx,
						delta+paint.getTextSize()*3,paint);
						
		}
	}

	Paint paintBasico() {
		Paint paint=new Paint();
		paint.setTypeface(Typeface.DEFAULT_BOLD);
		paint.setAntiAlias(true);
		return paint;
	}

	Paint getPaintEstadoActual() {
		Paint paint=paintBasico();
		paint.setTextSize(getTextSize());
		paint.setTextAlign(Align.CENTER);
		paint.setColor(Estilo.COLOR_ACTUAL);
		return paint;
	}

	Paint getPaintHoraEventos() {
		Paint paint=paintBasico();
		paint.setTextSize(getTextSize()*6/4);
		paint.setColor(Estilo.TEXTO_BLANCO);
		return paint;
	}
	
	Paint getPaintEstadoEventos() {
		Paint paint=paintBasico();
		int textSize=getTextSize();
		paint.setTextSize(textSize);
		paint.setColor(Estilo.TEXTO_BLANCO);
		return paint;
	}

	void pintarEvento(Canvas canvas, String hora, int nombreMarea, int zonaX) {
		Paint paint=getPaintHoraEventos();
		Paint paintEstado=getPaintEstadoEventos();
		float delta=0;
		int posx;

		if(zonaX<0) {
			// Zona Izquierda
			paintEstado.setTextAlign(Align.LEFT);
			paint.setTextAlign(Align.LEFT);
			posx=4;
		} else if(zonaX==0) {
			// Zona Centro
			paintEstado.setTextAlign(Align.CENTER);
			paint.setTextAlign(Align.CENTER);
			posx=width/2;
		} else {
			// Zona Centro
			paintEstado.setTextAlign(Align.RIGHT);
			paint.setTextAlign(Align.RIGHT);
			posx=width-4;
		}

		if(nombreMarea==R.string.bajamar) {
			paint.setColor(Estilo.COLOR_TEXTO_HORA_BAJAMAR);
			paintEstado.setColor(Estilo.COLOR_TEXTO_BAJAMAR);
			delta=height-paint.getTextSize()*2;
		} else {
			paint.setColor(Estilo.COLOR_TEXTO_HORA_PLEAMAR);
			paintEstado.setColor(Estilo.COLOR_TEXTO_PLEAMAR);
			delta=0;
		}
		canvas.drawText(
				hora,
				posx, delta+paint.getTextSize(),
				paint);

		canvas.drawText(
				getContext().getString(nombreMarea),
				posx,
				delta+paint.getTextSize()+paintEstado.getTextSize(),
				paintEstado);

	}
	

	private void pintarOnda(Canvas canvas, int fase) {

		float xIni = getWidth()/8;
		float w = getWidth() - (2*xIni);

		float yIni=getPaintHoraEventos().getTextSize();
		float h = getHeight() - 2 * yIni;
		float centerY = h / 2f;
		float amplitude = h / 2f; // altura máxima de la onda
		float desplazamiento = (float)( fase>0? Math.PI / 2f : -Math.PI / 2f);

		int steps = 100; // número de puntos para dibujar (más = más suave)

		final double RANGO= Math.PI * 2;

		Path path = new Path();
		for (int i = 0; i <= steps; i++) {
			float x = xIni + (w * i / (float) steps);
			float angle = (float) ((RANGO * i) / steps) + desplazamiento; // medio ciclo
			float y = yIni + centerY - amplitude * (float) Math.sin(angle); // resta para que y crezca hacia abajo
			if (i == 0) {
				path.moveTo(x, y);
			} else {
				path.lineTo(x, y);
			}
		}
		canvas.drawPath(path, paintOnda);
	}
}
