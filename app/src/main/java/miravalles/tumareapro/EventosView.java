package miravalles.tumareapro;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
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
		return height / 10;
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



		if(info.anterior!=null && info.siguiente!=null) {

			if(infoSiguiente !=null) {
				pintarMarcas(canvas, info.anterior, infoSiguiente.siguiente);
			}
			pintarOnda(canvas,info.getIntAlturaAnterior()-info.getIntAlturaSiguiente());
			pintarPuntoActual(canvas);
			pintarEvento(canvas, info.getHoraAnterior(), info.getNombreAnterior(), -1 );
			pintarEvento(canvas, info.getHoraSiguiente(), info.getNombreProximo(), 0);

		}

		if(infoSiguiente!=null) {
			pintarEvento(canvas, infoSiguiente.getHoraSiguiente(),
							 infoSiguiente.getNombreProximo(), 1);
		}
		/*
		pintarEventoAnterior(canvas);
		pintarEventoSiguiente(canvas);
		*/
		if(info.anterior!=null && info.siguiente!=null) {
			pintarEstadoActual(canvas);
		}
	}

	private void pintarMarcas(Canvas canvas, Date desde, Date hasta) {
		int amplitudMinutos=distanciaMinutos(desde, hasta);
		float w = anchoOnda();
		float ratioPixelMinuto = anchoOnda() / (float)amplitudMinutos;

		Paint paintTexto=paintBasico();
		paintTexto.setTextSize(getTextSize() * 2f / 3f);
		paintTexto.setTextAlign(Align.CENTER);
		paintTexto.setColor(Estilo.COLOR_LINEA);

		/**
		 * La posicion inicial es la misma que la onda, pero hay que empezar
		 * a pintar desde la hora siguiente y por tanto vamos a considerar xini
		 * como el punto donde se pintaria la horaInicio, pero luego empezaremos
		 * por la siguiente hora.
		 */
		float xIni = xIniOnda()	- ratioPixelMinuto * extraerMinutos(desde);
		Date horaInicio=horaRedonda(desde);
		Date horaFin=horaRedonda(hasta);

		Paint paintRaya=CanvasUtil.paintBorde();
		paintRaya.setColor(0xFF334455);
		paintRaya.setPathEffect(new DashPathEffect(new float[]{20,20}, 0));


		float y =  canvas.getHeight() - ALTO_ZONA_MARCAS / 2;
		for(Date hora=horaSiguiente(horaInicio); hora.getTime()<horaFin.getTime(); hora=horaSiguiente(hora)) {
			float x = xIni + difHoras(hora , horaInicio) * ratioPixelMinuto * 60;
			float yActual = puntoActual(canvas, hora.getTime()).y;
			canvas.drawLine(x, yActual, x, y - paintTexto.getTextSize(), paintRaya);
			String texto= "" + extraerHora(hora);
			canvas.drawText(texto, x,y, paintTexto);
		}
	}

	private long difHoras(Date hasta, Date desde) {
		return (hasta.getTime() - desde.getTime()) / 60 / 60 / 1000;
	}

	private Date horaRedonda(Date desde) {
		Calendar cal=Calendar.getInstance();
		cal.setTime(desde);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		return cal.getTime();
	}

	private Date horaSiguiente(Date desde) {
		Calendar cal=Calendar.getInstance();
		cal.setTime(desde);
		cal.add(Calendar.HOUR, 1);
		return cal.getTime();
	}

	int minutosRestantes(Date desde) {
		return 60 - extraerMinutos(desde);
	}

	/**
	 * El tiempo en minutos entre dos momentos en el tiempo.
	 * @param desde
	 * @param hasta
	 * @return
	 */
	private int distanciaMinutos(Date desde, Date hasta) {
		int amplitudMinutos=extraerHoraYMinutos(hasta) - extraerHoraYMinutos(desde);
		if(amplitudMinutos<0) {
			amplitudMinutos+=24 * 60;
		}
		return amplitudMinutos;
	}

	private int extraerMinutos(Date date) {
		Calendar cal=Calendar.getInstance();
		cal.setTime(date);
		return cal.get(Calendar.MINUTE);
	}

	private int extraerHoraYMinutos(Date date) {
		Calendar cal=Calendar.getInstance();
		cal.setTime(date);
		return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
	}

	private int extraerHora(Date date) {
		Calendar cal=Calendar.getInstance();
		cal.setTime(date);
		return cal.get(Calendar.HOUR_OF_DAY);
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

	int ALTO_ZONA_MARCAS = 40;
	/**
	 * Este es el espacio reservado a la zona de la Onda y de sus textos
	 * tenemos que dejar un margen para la zona de las marcas de horas.
	 * @return
	 */
	int getAltoGrafico(Canvas canvas) {
		return canvas.getHeight() - ALTO_ZONA_MARCAS;
	}
	
	void pintarEstadoActual(Canvas canvas) {
		Paint paint=getPaintEstadoActual();

		float deltaY=(getAltoGrafico(canvas) - paint.getTextSize()*2)/2;
		float posx=width/4;

		PointF punto=puntoActual(canvas,  info.hora.getTime());

		posx=Math.max(
				Math.min(punto.x, width/2-paint.getTextSize()*4),
				paint.measureText("Pleamar Subi")
				);
		deltaY=Math.min(punto.y, getAltoGrafico(canvas) - paint.getTextSize()*3);
		
		Date ahora=new Date();
		if(DateUtils.isToday(info.hora.getTime())) {
			canvas.drawText(
					info.getHora(), 
						posx,
						deltaY+paint.getTextSize(),
					paint);
			canvas.drawText(
					getContext().getString(info.getEstado()),
						posx,
						 deltaY + 2 *  paint.getTextSize(),
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
					deltaY+paint.getTextSize()*1,paint);
			canvas.drawText(
						info.getHora(),
						posx,
						deltaY+paint.getTextSize()*2,paint);
			canvas.drawText(
					getContext().getString(info.getEstado()), 
						posx,
						deltaY+paint.getTextSize()*3,paint);
						
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
			delta=getAltoGrafico(canvas)-paint.getTextSize()*2;
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


	PointF puntoActual(Canvas canvas, long ahora) {
		if(info.siguiente==null || info.anterior==null) {
			return new PointF(0,0);
		}

		long  distanciaEntreMareas = info.siguiente.getTime() - info.anterior.getTime();

		double  x = (ahora - info.anterior.getTime()) * (Math.PI) / distanciaEntreMareas;

		boolean bajando=info.alturaSiguiente < info.alturaAnterior;
		float desplazamiento = (float)( bajando? Math.PI / 2f : -Math.PI / 2f);
		float centerY = altoOnda() / 2f;
		float amplitude = altoOnda() / 2f; // altura máxima de la onda

		float xPunto = (float)(xIniOnda() + x * (anchoOnda()/2) / Math.PI);
		float yPunto = (float)(yIniOnda() + centerY - Math.sin(x + desplazamiento) * amplitude);

		return new PointF(xPunto, yPunto);
	}

	private void pintarPuntoActual(Canvas canvas) {
		Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(0xFFCC7777);

		PointF punto=puntoActual(canvas, info.hora.getTime());
		canvas.drawCircle(punto.x, punto.y, 10, paint);
	}

	private float anchoOnda() {
		return getWidth() - (2*xIniOnda());
	}

	private float xIniOnda() {
		return getWidth()/6;
	}

	private float yIniOnda() {
		return getPaintHoraEventos().getTextSize();
	}

	private float altoOnda() {
		return getHeight()  - ALTO_ZONA_MARCAS - 2 * yIniOnda();
	}

	private void pintarOnda(Canvas canvas, int fase) {

		float xIni = xIniOnda();
		float w = anchoOnda();

		float yIni=yIniOnda();
		float h = altoOnda();
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
