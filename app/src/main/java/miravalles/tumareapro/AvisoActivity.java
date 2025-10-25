package miravalles.tumareapro;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TableLayout.LayoutParams;
import android.widget.Toast;

public class AvisoActivity extends Activity  {
	
	WebView vista;

	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		String aviso=(String)getIntent().getExtras().get("aviso");
		
		vista=new WebView(this);
		LinearLayout.LayoutParams lp=
				new LinearLayout.LayoutParams(
						LayoutParams.FILL_PARENT, 
						LayoutParams.FILL_PARENT);
		vista.setLayoutParams(lp);
		setContentView(vista);
		vista.addJavascriptInterface(new Escuchador(), "android");
		vista.loadUrl("file:///android_asset/" + aviso + ".html");
	}

	public class Escuchador {
		public void visitar() {
			Toast.makeText(AvisoActivity.this, "Hola mundo", Toast.LENGTH_LONG).show();
			/*
			Intent intent = new Intent(Intent.ACTION_VIEW);		
			intent.setData(Uri.parse("market://details?id=miravalles.tumarea"));
			startActivity(intent);
			*/				
		}
	}
	
	
	
}
