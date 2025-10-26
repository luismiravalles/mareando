package miravalles.tumareapro;

import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

public class Sizer {
	
	private View v;
	
	public Sizer() {
		
	}
	
	public Sizer set(View v) {
		this.v=v;
		LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,0);
		v.setLayoutParams(lp);
		return this;
	}
	
	public Sizer(View v) {
		set(v);
	}
	
	public Sizer fillWidth() {
		v.getLayoutParams().width=LayoutParams.MATCH_PARENT;
		return this;
	}


	
	public Sizer fillHeight() {
		v.getLayoutParams().height=LayoutParams.MATCH_PARENT;
		return this;
	}
	
	public Sizer setHeight(int peso) {
		v.getLayoutParams().height=peso;
		return this;
	}
	
	
	
	public Sizer wrapHeight() {
		v.getLayoutParams().height=LayoutParams.WRAP_CONTENT;
		return this;
	}
	
	public Sizer wrapWidth() {
		v.getLayoutParams().width=LayoutParams.WRAP_CONTENT;
		return this;
	}
	
	public Sizer marginTop(int top) {
		((LinearLayout.LayoutParams)v.getLayoutParams()).topMargin =top;
		return this;
	}
	
	
	public Sizer relativePctHeight(int y) {
		v.getLayoutParams().height=0;
		((LinearLayout.LayoutParams)v.getLayoutParams()).weight=y;
		return this;
	}
	
	public Sizer pctHeight(int pct) {
		v.getLayoutParams().height=pct *
		v.getContext().getResources().getDisplayMetrics().heightPixels
		/ 100;			
		return this;
	}	

	public Sizer pctWidth(int pct) {
		v.getLayoutParams().width=pct *
		v.getContext().getResources().getDisplayMetrics().widthPixels
		/ 100;			
		return this;
	}

	public Sizer weightY(float w) {
		((LinearLayout.LayoutParams)v.getLayoutParams()).weight=w;
		v.getLayoutParams().height=0;
		return this;
	}


}
