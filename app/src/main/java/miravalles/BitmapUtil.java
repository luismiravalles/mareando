package miravalles;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;


public class BitmapUtil {

	
	public static Bitmap decodeResource(Resources res, int resource){
	    
        //Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resource, o);


        //The new size we want to scale to
        final int REQUIRED_SIZE=200;

        //Find the correct scale value. It should be the power of 2.
        int scale=1;
        while(o.outWidth/scale/2>=REQUIRED_SIZE && o.outHeight/scale/2>=REQUIRED_SIZE)
            scale*=2;

        //Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inDither=false;                     //Disable Dithering mode
        o2.inPurgeable=true;                   //Tell to gc that whether it needs free memory, the Bitmap can be cleared
        o2.inInputShareable=true; 	        
        o2.inSampleSize=scale;
        o2.inTempStorage=new byte[32 * 1024]; 
        return BitmapFactory.decodeResource(res, resource, o2);
	     

	}	

	/**
	 * Retorna el alto con el que realmente se ha representado una imagen
	 * dentro de un bitmap. Pero solo sirve si la imagen es FILL_PARENT,
	 * ademas Scale FIT_START y el bitmap es mas pequeno que el ancho del display.
	 * 
	 * @param View
	 * @return
	 */
	public static int getAltoReal(Bitmap bitmap, int anchoDisplay) {
		double ratio=(double)anchoDisplay / (double)bitmap.getWidth();
		Log.i("M", "anchoDisplay=" + anchoDisplay);
		Log.i("M" ,"bitmapWidth=" + bitmap.getWidth());
		Log.i("M", "ratio=" + ratio);
		return (int)(bitmap.getHeight() * ratio);
	}

	
	
	//decodes image and scales it to reduce memory consumption	
	public static Bitmap decodeFile(File f){
	    try {
	        //Decode image size
	        BitmapFactory.Options o = new BitmapFactory.Options();
	        o.inJustDecodeBounds = true;
	        BitmapFactory.decodeStream(new FileInputStream(f),null,o);

	        //The new size we want to scale to
	        final int REQUIRED_SIZE=200;

	        //Find the correct scale value. It should be the power of 2.
	        int scale=1;
	        while(o.outWidth/scale/2>=REQUIRED_SIZE && o.outHeight/scale/2>=REQUIRED_SIZE)
	            scale*=2;

	        //Decode with inSampleSize
	        BitmapFactory.Options o2 = new BitmapFactory.Options();
	        o2.inDither=false;                     //Disable Dithering mode
	        o2.inPurgeable=true;                   //Tell to gc that whether it needs free memory, the Bitmap can be cleared
	        o2.inInputShareable=true; 	        
	        o2.inSampleSize=scale;
	        o2.inTempStorage=new byte[32 * 1024]; 
	        return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
	    } catch (FileNotFoundException e) {
	    	Log.e("F", "Imagen " + f.getAbsolutePath() + " no encontrada");
	    }
	    return null;
	}	
}
