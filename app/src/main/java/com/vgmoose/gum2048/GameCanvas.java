package com.vgmoose.gum2048;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.widget.LinearLayout;


public class GameCanvas {

	float width, height;
	private Paint mpaint;
	String gesture = "idk";
	final int text_size=100;
	GridLock grid;
	DrawPanel dv;
	int actionBarHeight, navBarHeight, statusBarHeight;
	int numbers = 0;

	public GameCanvas(DrawPanel g)
	{
		// set height and width
		Display mDisplay = g.ctx.getWindowManager().getDefaultDisplay();
		width  = mDisplay.getWidth();
		height = mDisplay.getHeight();
		numbers = g.ctx.sharedPrefs.getInt("numbers", 0);

		TypedValue tv = new TypedValue();
		if (g.ctx.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
		{
			actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, g.ctx.getResources().getDisplayMetrics());
		}

		final LinearLayout ll = (LinearLayout) g.ctx.findViewById(R.id.fullscreen_content_controls);
		ll.post(new Runnable(){
		    public void run(){
		         navBarHeight = ll.getHeight();
//		 		Log.v("THINGS", ""+actionBarHeight+" "+navBarHeight+" "+statusBarHeight); 

		    }
		});

		int resourceId = g.ctx.getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			statusBarHeight = g.ctx.getResources().getDimensionPixelSize(resourceId);
		} 
		

		mpaint = new Paint();
		mpaint.setTextSize(text_size);
		g.sensitivity = height/10;
		dv = g;
		grid = new GridLock(dv.size,dv.size, this);
	}

	public void paintBoard(Canvas g)
	{
		//		float center_horiz = mpaint.measureText(gesture);
		//		g.drawText(gesture, (width-center_horiz)/2, (height-text_size)/2, mpaint);

		if (dv.locked)
			grid.drawRaw(g, height, width);
		else
			grid.drawSelf(g, height, width);
	}

	public void setGesture(String string) 
	{
		gesture = string;
	}
}



