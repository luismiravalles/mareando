package miravalles.tumareapro;

import android.graphics.Canvas;
import android.graphics.Paint;

public class CanvasUtil {

    public static void drawRect(Canvas canvas,
                          float left, float top, float right, float bottom,
                          Paint paintFondo, Paint paintBorde) {
        canvas.drawRect(left, top, right, bottom, paintFondo);

        canvas.drawRect(left, top, right, bottom, paintBorde);
    }




    public static Paint paintBorde() {
        Paint paint=new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(Estilo.COLOR_LINEA);
        return paint;
    }

}
