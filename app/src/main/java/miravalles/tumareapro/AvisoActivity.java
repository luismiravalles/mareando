package miravalles.tumareapro;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TableLayout.LayoutParams;
import android.widget.Toast;

import androidx.core.view.WindowCompat;

public class AvisoActivity extends Activity  {
	
	WebView vista;

	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		String aviso=(String)getIntent().getExtras().get("aviso");
		setContentView(R.layout.aviso_layout);

		// Según ChatGpt esto evita que machaquemos la barra de estado de arriba.
		WindowCompat.setDecorFitsSystemWindows(getWindow(), true);

		vista=findViewById(R.id.avisoView);
		vista.getSettings().setAllowFileAccess(true);
		vista.getSettings().setJavaScriptEnabled(true);
		if(aviso.startsWith("http")) {
			vista.loadUrl(aviso);
		} else {
			String textoHtml=Util.leerAsset(this.getApplicationContext(),aviso + ".html");
			vista.loadDataWithBaseURL("file:///android_asset/", textoHtml, "text/html", "utf-8", null);
		}

	}

	
	
	
}
