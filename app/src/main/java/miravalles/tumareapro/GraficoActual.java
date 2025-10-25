package miravalles.tumareapro;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import miravalles.tumareapro.domain.AemetInfo;
import miravalles.tumareapro.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class GraficoActual extends View {
	
	private static Bitmap barquito=null;
	private static Bitmap agua=null;
	private static Bitmap iconoSubiendo=null;
	private static Bitmap iconoBajando=null;
	private static Bitmap iconoSol=null;
	private static Bitmap iconoCoef=null;
	
	private static Map<String, Bitmap> bitmaps=new HashMap<String,Bitmap>();
	
	private int position;
	
	MareaInfo info;
	private int width;
	private int height;
	
	final int getMargenTop() {
		return 32 +
			getDiametroLuna();
	}
	
	final int getDiametroLuna() {
		return height/6;
	}

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
	}

	public GraficoActual(Context context, int position) {
		super(context);		
		this.position=position;
	}
	
	protected Rect rectPiscina() {
		Rect r=new Rect(2, 0, width-2, height-getMargenInferior());
		r.left = r.left + getMargenIzquierdo();
		// r.right=r.right-margenTexto;
		return new Rect(r);
	}
	
	public int getTextSize() {
		return 
				Math.min(
						height/ 14,
						width / 18 );
	}
	
	public int getMargenIzquierdo() {
		final int textSize=getTextSize();
		final int margenTexto=textSize*8;
		return margenTexto;
	}
	
	public int getMargenInferior() {
		return getTextSize() * 4;
	}
	
	
	/**
	 * Devuelve la posici�n relativa al rect�ngulo del gr�fico que 
	 * representa la altura de la marea.
	 * 
	 * El valor 0 es la base del gr�fico, y el valor 600 es la m�xima altura dibujable.
	 * 
	 * @param altura
	 * @return
	 */
	int getYMarea(int altura) {
		Rect r=rectPiscina();
		int altoPiscina=r.bottom-r.top;
		int rangoMarea = altoPiscina - getAltoFilaTexto() * 3 - getLeyendaSize(); // NUmero de filas de texto + 1
		int y=rangoMarea * altura / Config.maxAltura();
		return  altoPiscina - ( y + r.top ) - getLeyendaSize();
	}
		
	
	@Override
	protected void onDraw(Canvas canvas) {
	
		if(barquito==null) {
			barquito = BitmapFactory.decodeResource(
					getResources(), R.drawable.barquito_fondotransparente);            

		}
		if(agua==null) {
			agua = BitmapFactory.decodeResource(getResources(), R.drawable.agua);
		}
		
		if(iconoSubiendo==null) {
			iconoSubiendo=BitmapFactory.decodeResource(
					getResources(), R.drawable.subiendo); 
		}
		if(iconoBajando==null) {
			iconoBajando=BitmapFactory.decodeResource(
					getResources(), R.drawable.bajando);
		}
		if(iconoSol==null) {
			iconoSol=BitmapFactory.decodeResource(
					getResources(), R.drawable.sol);
		}
		if(iconoCoef==null) {
			iconoCoef=BitmapFactory.decodeResource(
					getResources(), R.drawable.coef);
		}
	
		//Rect rect=new Rect(0, canvas.getHeight()/2, canvas.getWidth(), canvas.getHeight());
		final int anchoBorde=2;
		
		
		// Pintando el agua
		Rect rect=rectPiscina();
		int yAgua=getYMarea(info.getAltura());
		rect.top=yAgua;
		rect.left = rect.left + anchoBorde;
		rect.bottom = rect.bottom - anchoBorde;
		rect.right = rect.right - anchoBorde;
		Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(0xFF0044FF);
		paint.setStyle(Style.FILL);
		
		GradientDrawable gd=new GradientDrawable(
				GradientDrawable.Orientation.TOP_BOTTOM, new int[] {0xFF0022CC, 0xFF001166});
		gd.setBounds(rect);
		gd.draw(canvas);
		


		Rect rectCielo = pintarCielo(canvas, rect);
		
		pintarEstrellas(canvas, rectCielo);
		try {
			pintarLuna(canvas, rectCielo);
		} catch(Exception e) {
			
		}
		
		// canvas.drawRect(rect, paint);
		
		
		
	    pintarBarquito(canvas, rect);
		

		
	    pintarAgua(canvas);
		pintarBordePiscina(canvas, anchoBorde);
		
		
		pintarFondoAlturas(canvas);
		pintarLeyendaMaximo(canvas,info,  Estilo.COLOR_TEXTO_HORA_PLEAMAR);
		pintarLeyendaMinimo(canvas, info, Estilo.COLOR_TEXTO_HORA_BAJAMAR);
		pintarLeyenda(canvas, info.getAltura(), getYMarea(info.getAltura()),
					   info.getStringAltura(), Estilo.COLOR_ACTUAL);
		
		pintarIconoEstado(canvas, info);
		pintarTexto(canvas, rectCielo, rect);
		
		pintarFondoTransicion(canvas);
		pintarEventoAnterior(canvas);
		pintarEventoSiguiente(canvas);
		pintarEstadoActual(canvas);
		
		pintarAemet(canvas);
	}
	
	private void pintarAemet(Canvas canvas) {

		String texto=""; //	"?" + "�";
		String codigoAemet=Modelo.get().getSitio(position).getCodigoAemet();
		if(Config.isVersionPro() && codigoAemet!=null) {			
			AemetInfo aemetInfo=Aemet.getCache(codigoAemet);
			if(aemetInfo!=null) {
				texto=aemetInfo.getTemperaturaAgua();
			}

			if(DateUtils.isToday(info.hora.getTime())) { 
				int y=getYMarea(info.getAltura()) + getTextSize() + 8;
				int x=width - 16;
				  
				Paint paint=new Paint();
				paint.setAntiAlias(true);
				paint.setTextAlign(Align.RIGHT);
				paint.setColor(Estilo.COLOR_TEXTO_TEMPERATURA);
				paint.setTextSize(getTextSize());
				canvas.drawText(texto, x, y, paint);
			}
			
			
			AemetInfo.Datos datos=aemetInfo!=null?aemetInfo.getDatos(info.hora):null;
			int x=getMargenIzquierdo();
			if(datos!=null) {
				
				if(datos.getCielo()!=null) {
					Bitmap imagen=getBitmap(datos.getNombreBitmapCielo());
					if(imagen!=null) {
						Matrix matrix=new Matrix();
						float escalaCielo=((float)width / 4f) / (float)imagen.getWidth();
						matrix.postScale(escalaCielo,escalaCielo);
						matrix.postTranslate(x, - (imagen.getWidth()*escalaCielo/6));
						canvas.drawBitmap(imagen,matrix, null);	
						x+= imagen.getWidth() * escalaCielo + 2;
					
					}
				}

				if(datos.getDireccionViento()!=null && datos.getIntVelocidadViento()>0) {

					Bitmap imagen=getBitmap(datos.getImagenViento());
					int y= 32;
					if(imagen!=null) {						
						

						for(int i=0; i<1; i+=10) {
							Matrix matrix=new Matrix();
							float escala=((float)width / 10f) / (float)imagen.getWidth();
							
							matrix.postRotate(datos.getAngulo(), imagen.getWidth()/2, imagen.getHeight()/2);	
							int dy=0;
							int dx=0;
							if("E".equals(datos.getDireccionViento()) ||
									"O".equals(datos.getDireccionViento())) {
								dy=i*2;
							} else {
								dx=i*2;
							}
							matrix.postScale(escala,escala);
							matrix.postTranslate(x + dx, y+dy);												
							canvas.drawBitmap(imagen, matrix, null);
						}
					String textoVelocidad=datos.getVelocidadViento() + "km/h";
					Paint paint=new Paint();
					paint.setTextSize(getTextSize() * 3 / 5);
					paint.setColor(Estilo.COLOR_TEXTO_TEMPERATURA);
					canvas.drawText(textoVelocidad,x, y, paint);

					}
				}
			} else {
				Log.i("D", "NO hay datos para " + info.hora);
			}
			
		}
	
	}



	private Rect pintarCielo(Canvas canvas, Rect rect) {
	
		
		Log.i("x", "Luz = " + info.getLuz());
		int altoRojo =  0x00 * info.getLuz() / 100;
		int altoVerde = Math.min(0xFF, 0x10 * info.getLuz() / 100);
		int altoAzul=   Math.min(0xFF, 0x40 * info.getLuz() / 100);
		
		int bajoRojo = Math.min(0xFF,  0x30 + 0xFF * (50 - Math.abs(info.getLuz()-50)) / 100);
		int bajoVerde =Math.min(0xFF,  0xBB * info.getLuz() / 100 + 0x70 * (50 - Math.abs(info.getLuz()-50)) / 100);
		int bajoAzul = Math.max(0x00, 
					Math.min(0xFF,    0xFF * info.getLuz() / 100 
									- 0x90 * (50 - Math.abs(info.getLuz()-50)) / 100)
					);
		
		
		// Solo quiero nocturno
//		altoRojo = 0x00;
//		altoVerde = 0x00;
//		altoAzul = 0x00;
//		bajoRojo = 0x00;
//		bajoVerde = 0x00;
//		bajoAzul = 0x00;
		
		GradientDrawable gdCielo=new GradientDrawable(
				GradientDrawable.Orientation.BOTTOM_TOP, new int[] {
						0xFF000000 + bajoRojo * 256*256+ bajoVerde * 256 + bajoAzul,
						//0xFF000000 + altoRojo * 256*256 +altoVerde * 256 + altoAzul, 
						0xFF000000 + altoRojo * 256*256 +altoVerde * 256 + altoAzul});
		
		Rect rectCielo=new Rect(rect);
		rectCielo.bottom=rect.top;
		rectCielo.top=0;
		gdCielo.setBounds(rectCielo);
		gdCielo.draw(canvas);
		return rectCielo;
	}
	
	// retorna el porcentaje de tiempo recorrido entre los dos eventos.
	public int getPctPeriodo() {
		if(info.siguiente==null || info.anterior==null) {
			return 0;
		}
		long difSiguienteAnterior = info.siguiente.getTime() - info.anterior.getTime();
		long difHoraAnterior = info.hora.getTime() - info.anterior.getTime();
		
		return (int)(difHoraAnterior * 100 / difSiguienteAnterior);
	}

	private void pintarBarquito(Canvas canvas, Rect rect) {
		
		
		int anchoPosible=rect.right-rect.left-barquito.getWidth();
		int posX = (int)
				((anchoPosible) * getPctPeriodo() / 100);
		
		canvas.drawBitmap(barquito,
	    		rect.left + posX, 	    		
	    		rect.top-barquito.getHeight(), null);
	}
	
	private void pintarFondoAlturas(Canvas canvas) {
		
		LinearGradient g=new LinearGradient(
				0, getYMarea(info.getIntMaximo()), 0, getYMarea(info.getIntMinimo()), 
				Estilo.ROJO, Estilo.VERDE, TileMode.CLAMP);
		Paint paint=new Paint();
		paint.setShader(g);
		canvas.drawRect(0, 0
					,  getMargenIzquierdo() + 2, 
					height, paint);
		
	}
	
	private void pintarFondoTransicion(Canvas canvas) {
		int izquierdo;
		int derecho;
		if(info.getIntAlturaAnterior()>info.getIntAlturaSiguiente()) {
			izquierdo=Estilo.ROJO;
			derecho=Estilo.VERDE;
		} else {
			izquierdo=Estilo.VERDE;
			derecho=Estilo.ROJO;
		}
		LinearGradient g=new LinearGradient(
				getMargenIzquierdo(), 0, rectPiscina().right, 0, 
				izquierdo, derecho, TileMode.CLAMP);
		Paint paint=new Paint();
		paint.setShader(g);
		canvas.drawRect(0, height-getMargenInferior()
					,  rectPiscina().right, height, paint);
		Paint paintRaya=new Paint();
		paintRaya.setColor(Estilo.TEXTO_BLANCO);
		canvas.drawLine(0, height-getMargenInferior(), rectPiscina().right,
					height-getMargenInferior(), paintRaya);
	}
	
	
	private void pintarLeyendaMaximo(Canvas canvas, 
			MareaInfo info,
			int color) {
		
		int valor=info.getIntMaximo();
		
		String textoHora = info.estaSubiendo()? info.getHoraSiguiente():info.getHoraAnterior();

		String texto=info.getMaximo();
		int yTexto=Math.min(getYMarea(info.getIntMaximo()),
				 getYMarea(info.getAltura()) - getTextSize() );
				
				
		pintarLeyenda(canvas, valor, yTexto, /* textoHora + " - " + */ texto, color);
	}
	
	private void pintarLeyendaMinimo(Canvas canvas, MareaInfo info,
			int color) {
		
		int valor=info.getIntMinimo();
		
		String textoHora = info.estaSubiendo()? info.getHoraAnterior():info.getHoraSiguiente();

		String texto=info.getMinimo();
		
		int yTexto=Math.max(getYMarea(info.getIntMinimo()),
				 getYMarea(info.getAltura()) + getLeyendaSize());
		
		pintarLeyenda(canvas, valor, yTexto, /* textoHora + " - " +  */ texto , color);
	}
	
	private int getLeyendaSize() {
		return getTextSize() * 5 / 4;
	}
	
	private void pintarLeyenda(Canvas canvas, int valor, int yTexto, String texto, int color) {
		Paint paint=new Paint();
		paint.setAntiAlias(true);
		paint.setTextAlign(Align.RIGHT);
		paint.setColor(color);
		paint.setTextSize(getLeyendaSize());
		
		int y=getYMarea(valor);
		int x=getMargenIzquierdo()-getTextSize();
		canvas.drawText(texto, x, yTexto, paint);
		
		Paint paintLinea=new Paint();
		paintLinea.setColor(Estilo.COLOR_LINEA);
		canvas.drawLine(
					getMargenIzquierdo()-getTextSize(),
					yTexto,
					getMargenIzquierdo()-getTextSize()/2+1,
					y, paintLinea);
		canvas.drawLine(getMargenIzquierdo()-getTextSize()/2+1, 
						y, 
						getMargenIzquierdo(),
						y, paintLinea);
		
	}
	
	private void pintarIconoEstado(Canvas canvas, MareaInfo info) {

		int y=getYMarea(info.getAltura()) - getLeyendaSize();
		int x=getMargenIzquierdo() - getLeyendaSize() * 5;
		
		
		
		Bitmap bitmap=info.getIconoEstado()==R.drawable.subiendo ? iconoSubiendo:iconoBajando;
		
		if(y + iconoBajando.getHeight() > height - getMargenInferior()) {
			y = height - getMargenInferior() - iconoBajando.getHeight();
		}
		pintarImagen(canvas, bitmap, x, y, getLeyendaSize());
	}

	private void pintarBordePiscina(Canvas canvas,
			final int anchoBorde) {
		// Pintar el borde de la piscina no se ve afectado
	    // por el alto del barquito.
//		Rect rFondo=rectPiscina();
//		rFondo.right=rFondo.left+anchoBorde;
//		Paint pFondo=new Paint();
//		//pFondo.setColor(0xFF0b2f96);
//		pFondo.setColor(Estilo.COLOR_LINEA);
//		pFondo.setStyle(Style.FILL);
//		canvas.drawRect(rFondo, pFondo);
//		rFondo=rectPiscina();
//		rFondo.left=rFondo.right-anchoBorde;
//		canvas.drawRect(rFondo, pFondo);
//		rFondo=rectPiscina();
//		rFondo.top=rFondo.bottom-anchoBorde;
//		canvas.drawRect(rFondo, pFondo);
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
						height-getMargenInferior()+
						paint.getTextSize(), 
					paint);
			canvas.drawText(
					getContext().getString(info.getEstado()),
						width/2,
						height-getMargenInferior()+
						paint.getTextSize() + paint.getTextSize(), 
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
					height-getMargenInferior()+paint.getTextSize()*1,paint);			
			canvas.drawText(
						info.getHora(),
						width/2,
						height-getMargenInferior()+paint.getTextSize()*2,paint);
			canvas.drawText(
					getContext().getString(info.getEstado()), 
						width/2,
						height-getMargenInferior()+paint.getTextSize()*3,paint);
						
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
		int textSize=getTextSize()*2;
		paint.setTextSize(textSize);
		paint.setColor(Estilo.TEXTO_BLANCO);
		paint.setTypeface(Typeface.DEFAULT_BOLD);
		paint.setAntiAlias(true);
		return paint;
	}
	
	Paint getPaintEstadoEventos() {
		Paint paint=new Paint();
		int textSize=getTextSize();
		paint.setTextSize(textSize*5/4);
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
				pad, height-getMargenInferior()+paint.getTextSize(),
				paint);
		
		
		canvas.drawText(
				getContext().getString(info.getNombreAnterior()),
				pad, 
				height-getMargenInferior()+
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
				this.width-pad, height-getMargenInferior()+paint.getTextSize(),
				paint);
		
		
		paintEstado.setTextAlign(Align.RIGHT);
		canvas.drawText(
				getContext().getString(info.getNombreProximo()),
				this.width-pad, 
				height-getMargenInferior()+
					paint.getTextSize()+paintEstado.getTextSize(),
				paintEstado);		
	}
	
	void pintarAgua(Canvas canvas) {		
		Rect src=new Rect(0,0,agua.getWidth(),agua.getHeight());
		Rect dst=new Rect(getMargenIzquierdo() + 4,
						  getYMarea(info.getAltura()),
						  width -4,
						  height-getMargenInferior()-1);
				
		canvas.drawBitmap(agua, src, dst, null);
		
	   }
	

	void pintarLuna(Canvas canvas, Rect rectCielo) throws IOException {
		int pct=getPctPeriodo();
		InputStream is=
			getContext().getAssets().open("luna/luna-" +
					info.getEdadLunar() + ".png");
		Bitmap luna = BitmapFactory.decodeStream(is);
		int pad=8;
		
		float escalaLuna=((float)width / 8f) / luna.getWidth();		
		float posx =	rectCielo.right -pad - luna.getWidth() * escalaLuna ;
		
		Rect origen=new Rect(0,0, luna.getWidth(), luna.getHeight());
		Rect destino=new Rect((int)posx, 2+pad, 
				(int)( posx+luna.getWidth()*escalaLuna) , (int)(2+pad+luna.getHeight()*escalaLuna));

		
		canvas.drawBitmap(luna, origen, destino, null);
	}
	
	private SimpleDateFormat sdfHoraMinuto=new SimpleDateFormat("HH:mm");
	
	private String formatHoraMinuto(Calendar cal) {
		return sdfHoraMinuto.format(cal.getTime());
	}
	
	private int getAltoFilaTexto() {
		return getTextSize() + 4 ;
	}
	
	private void pintarTexto(Canvas canvas, Rect rectCielo, Rect rectAgua) {	
		int pad=getTextSize() ;
		int altoFila=getAltoFilaTexto();

		Paint paint=new Paint();
		paint.setAntiAlias(true);
		paint.setColor(Estilo.TEXTO_BLANCO);
		paint.setTextSize(getTextSize());
		
		final int lineas=2;
	
		Paint paintFondo=new Paint();
		paintFondo.setColor(Estilo.COLOR_FONDO_INFO);
		canvas.drawRect(0, 1, getMargenIzquierdo()+2,
							pad + altoFila * lineas, paintFondo);
		
		int linea=0;
		int ajuste=altoFila / 5; 
		pintarImagen(canvas, iconoSol, 0, pad / 2 + ajuste, getTextSize());
		String textoSol=formatHoraMinuto(info.orto) + " -> " + formatHoraMinuto(info.ocaso);
		canvas.drawText(textoSol, getTextSize()+2,	pad + altoFila * linea + getTextSize() / 2, paint); 
		linea++;
		if(info.coeficiente> 0) {
			pintarImagen(canvas, iconoCoef, 0, pad / 2 + altoFila * linea + ajuste, getTextSize());
			String textoCoef="Coef: " + info.coeficiente;
			canvas.drawText(textoCoef, getTextSize()+2,	pad + altoFila * linea + getTextSize() / 2, paint); 
		
		}
		linea++;
				
		Paint paintLinea=new Paint();
		paintLinea.setColor(Estilo.COLOR_LINEA);
		canvas.drawLine(0, pad + altoFila *linea, 
						getMargenIzquierdo()+2,  pad + altoFila *linea,
						paintLinea);
	}
	
	void pintarImagen(Canvas canvas, Bitmap bitmap, int x, int y, int textSize) {
		Paint paint=new Paint();
		Rect rectOrigen=new Rect(0,0,bitmap.getWidth(), bitmap.getHeight());
		Rect rectDestino=new Rect(x,y, textSize+x, textSize+y);
		canvas.drawBitmap(bitmap, rectOrigen, rectDestino, paint);		
	}
	

	
	private void pintarEstrellas(Canvas canvas, Rect rectCielo) {
//		Random r=new Random(1);
//		Paint paint=new Paint();
//		paint.setColor(0xFFFFFFFF);
//		for(int i=0; i<100; i++) {
//			int luminosidad=
//					(128 + r.nextInt(128));
//			
//			paint.setColor(0xFF000000 +
//						luminosidad * 256 * 256 +
//						luminosidad * 256 +
//						luminosidad);
//			canvas.drawPoint(
//					rectCielo.left+r.nextInt(rectCielo.width()),
//					rectCielo.top+r.nextInt(rectCielo.height()),
//					paint);
//					
//		}
	}

	public int getPosition() {
		return position;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(event.getAction()==MotionEvent.ACTION_DOWN) {
			if(event.getY() > height - getMargenInferior()) {
				Log.i("g","setRespetar porque " + event.getY() + " > " + (height-getMargenInferior()));
				((TuMareaActivity)getContext()).setRespetarTouchEvent(true);		
			} 
		}
		
		return super.onTouchEvent(event);
	}

	public Bitmap getBitmap(String nombre) {
		Bitmap result=bitmaps.get(nombre);
		if(result!=null) {
			return result;
		}
		
		try {
			InputStream is=	getContext().getAssets().open(nombre + ".png");
			result = BitmapFactory.decodeStream(is);
			is.close();
			bitmaps.put(nombre, result);
			return result;
		} catch (IOException e) {
			Log.e("E", e.toString());
			return null;
		}

		
	}
	
}
