package miravalles.tumareapro;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;

public class AguaTabla extends View {
	
	
	private int altura;
	private int min;
	private int max;
	private boolean bajamar;
	private int color;
	private int backgroundColor;
	
	public int getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(int backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public boolean isBajamar() {
		return bajamar;
	}

	public void setBajamar(boolean bajamar) {
		this.bajamar = bajamar;
	}

	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		this.min = min;
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}

	public void setAltura(int altura) {
		this.altura = altura;
	}

	private int width;
	private int height;

	   @Override
	    public void onSizeChanged (int w, int h, int oldw, int oldh){
	        super.onSizeChanged(w, h, oldw, oldh);
	        width = w;
	        height = h;
	    }

	public AguaTabla(Context context) {
		super(context);		
	}
	
	protected Rect rectPiscina(Canvas canvas) {
		Rect r=new Rect(0,0,width, height);
		return new Rect(r);
	}
	
	public int getPct() {
		return (altura - min) * 100 / (max-min);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		Rect rect=rectPiscina(canvas);	
		Paint fondo=new Paint(Paint.ANTI_ALIAS_FLAG);
		fondo.setColor(getBackgroundColor());
		fondo.setStyle(Style.FILL);
		
		if(bajamar)	{
			Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
			paint.setColor(getColor());
			paint.setStyle(Style.FILL);		
			canvas.drawRect(rect,  paint);
			
			rect.left = rect.left + 	(rect.right-rect.left)	* getPct() / 100;
			RectF rectf=new RectF(rect);
			canvas.drawRoundRect(rectf, height/2, height/2, fondo);
			
			rect.left += height/2;
			canvas.drawRect(rect,  fondo);
		} else {
			
			canvas.drawRect(rect, fondo);
			rect.right = rect.left + 	(rect.right-rect.left)	* getPct() / 100;
			Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
			paint.setColor(getColor());
			paint.setStyle(Style.FILL);
			RectF rectf=new RectF(rect);
			canvas.drawRoundRect(rectf, height/2, height/2, paint); 
	
			rect.right-=height/2;
			canvas.drawRect(rect,  paint);
			
		}
		
	}

	
	
}
