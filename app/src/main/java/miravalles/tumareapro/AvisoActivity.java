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
		vista.getSettings().setAllowFileAccess(true);
		String textoHtml=Util.leerAsset(this.getApplicationContext(),aviso + ".html");
		vista.loadDataWithBaseURL("file:///android_asset/", textoHtml, "text/html", "utf-8", null);
	}

	
	
	
}
