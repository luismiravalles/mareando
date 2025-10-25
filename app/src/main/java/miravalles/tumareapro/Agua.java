package miravalles.tumareapro;

import java.io.IOException;
import java.io.InputStream;

import miravalles.tumareapro.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.View;

public class Agua extends View {
	
	
	private static Bitmap barquito=null;

	
	MareaInfo info;
	private int width;
	private int height;
	
	final int getMargenTop() {
		return 32 +
			getDiametroLuna();
	}
	
	final int getDiametroLuna() {
		return (width - 12 - getMargenTexto() ) * 2 / 3;
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

	public Agua(Context context) {
		super(context);		
	}
	
	protected Rect rectPiscina(Canvas canvas, int margenTexto) {
		Rect r=new Rect(2, 0, width-2, height);
		r.left = r.left + margenTexto;
		// r.right=r.right-margenTexto;
		return new Rect(r);
	}
	
	public int getTextSize() {
		return Util.dp(12,this);
	}
	
	public int getMargenTexto() {
		final int textSize=getTextSize();
		final int margenTexto=textSize*4;
		return margenTexto;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		
		final int textSize=getTextSize();
		final int margenTexto=getMargenTexto();
		
		if(barquito==null) {
			barquito = BitmapFactory.decodeResource(
					getResources(), R.drawable.barquito);            

		}
		
		
		//Rect rect=new Rect(0, canvas.getHeight()/2, canvas.getWidth(), canvas.getHeight());
		final int anchoBorde=4;
		
		
		// Pintando el agua
		Rect rect=rectPiscina(canvas, margenTexto);
		int yAgua=rect.bottom - 
				(rect.height()-getMargenTop()) * info.getPct() / 100;
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
		


		GradientDrawable gdCielo=new GradientDrawable(
				GradientDrawable.Orientation.BOTTOM_TOP, new int[] {0xFF000011, 0xFF000000});
		
		Rect rectCielo=new Rect(rect);
		rectCielo.bottom=rect.top;
		rectCielo.top=0;
		gdCielo.setBounds(rectCielo);
		gdCielo.draw(canvas);

		try {
			pintarLuna(canvas, rectCielo);
		} catch(Exception e) {
			Log.e("X", e.getMessage());
		}
		
		// canvas.drawRect(rect, paint);
		
		
	    canvas.drawBitmap(barquito,
	    		rect.left +
	    		(rect.right-rect.left - barquito.getWidth()) /2,
	    		rect.top-barquito.getHeight(), null);
		

		

		// Pintar el borde de la piscina no se ve afectado
	    // por el alto del barquito.
		Rect rFondo=rectPiscina(canvas, margenTexto);
		rFondo.right=rFondo.left+anchoBorde;
		Paint pFondo=new Paint();
		//pFondo.setColor(0xFF0b2f96);
		pFondo.setColor(0xFF808080);
		pFondo.setStyle(Style.FILL);
		canvas.drawRect(rFondo, pFondo);
		rFondo=rectPiscina(canvas, margenTexto);
		rFondo.left=rFondo.right-anchoBorde;
		canvas.drawRect(rFondo, pFondo);
		rFondo=rectPiscina(canvas, margenTexto);
		rFondo.top=rFondo.bottom-anchoBorde;
		canvas.drawRect(rFondo, pFondo);
		
		
		// Pintar los textos.
		
		Paint pTexto=new Paint(Paint.ANTI_ALIAS_FLAG);
		pTexto.setColor(0xFFFFFFFF);
		pTexto.setStyle(Style.FILL);
		
		pTexto.setTextSize(textSize);
		
		Paint pTextoAltura=new Paint(Paint.ANTI_ALIAS_FLAG);
		pTextoAltura.setColor(0xFFFFFF77);
		pTextoAltura.setStyle(Style.FILL);
		pTextoAltura.setTextSize(textSize);

		
		rect=new Rect(0, 0, width-2, height);
		canvas.drawText(info.getMaximo(), 
				2, 
				rect.top+getMargenTop()-4, pTexto);
		
		Paint paintLinea=new Paint();
		paintLinea.setColor(0xFFA0A0A0);
		canvas.drawLine(margenTexto-10, 
						rect.top+getMargenTop(), 
						margenTexto,
						rect.top+getMargenTop(), paintLinea);
		
		canvas.drawLine(margenTexto-10, 
				rect.bottom-anchoBorde, 
				margenTexto,
				rect.bottom-anchoBorde, paintLinea);		

		/*
		Bitmap bEstado=BitmapFactory.decodeResource(getResources(), info.getIconoEstado());
		canvas.drawBitmap(
				bEstado, 
				rect.right-bEstado.getWidth(),
				rect.top+margenTop+textSize+1, null);
		*/
		
		int yTextoAltura=rect.top + getMargenTop() + (rect.height())/2;
		yTextoAltura=Math.max(yAgua, rect.top + getMargenTop() + textSize);
		yTextoAltura=Math.min(yTextoAltura, rect.bottom-textSize-4);
		
		canvas.drawLine(margenTexto-10, 
						yTextoAltura, 
						margenTexto, 
						yTextoAltura
						, paintLinea);	
		canvas.drawText(info.getStringAltura(),
				2, 
				yTextoAltura, pTextoAltura);
		
		canvas.drawText(info.getMinimo(), 
				2, 
				rect.bottom-anchoBorde-2, pTexto);
		
	}

	void pintarLuna(Canvas canvas, Rect rectCielo) throws IOException {
		InputStream is=
			getContext().getAssets().open("luna/luna-" +
					info.getEdadLunar() + ".jpg");
		Bitmap luna = BitmapFactory.decodeStream(is);

		Rect rectOrigen=new Rect(0,0,95,95);
		Rect rectDestino=new Rect();
		rectDestino.left=rectCielo.right - getDiametroLuna();
		rectDestino.right=rectDestino.left + getDiametroLuna();
		rectDestino.top = 2;
		rectDestino.bottom= rectDestino.top + getDiametroLuna();
		
	    canvas.drawBitmap(luna, rectOrigen, rectDestino, null); 
	}
	
	
}
