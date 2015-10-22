package com.vgmoose.gum2048;


import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences.Editor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Shader;
import android.content.SharedPreferences;

public class DrawPanel extends View implements View.OnTouchListener {
	static final String DEBUG_TAG = "no";

	GameCanvas canvas;
	boolean locked = false;

	Gummy2048 ctx;
	float xUp, yUp, xDown, yDown;
	float sensitivity = 150;
	boolean moved_global = false;
	int size = 4;

	protected boolean refresh;

	public DrawPanel(Context cont, Gummy2048 mactivity) 
	{
		super(cont);  
		ctx = mactivity;
		refresh = false;
		setOnTouchListener(this);
		this.size = ctx.sharedPrefs.getInt("size", 4);
		canvas = new GameCanvas(this);
	}

	public void onDraw(Canvas g)
	{
		canvas.paintBoard(g);
	}

	public boolean onTouch(View v, MotionEvent e) 
	{
		int action = e.getAction();
		String gesture = "idk";

		if (locked)
			return true;

		switch(action) {
		case (MotionEvent.ACTION_DOWN) :
			xDown = e.getX();
		yDown = e.getY();
		break;
		
//		case (MotionEvent.ACTION_UP):
//			if (ctx.GodMode)
//			{
//				canvas.grid.gumgrid[e.getX()/size][e.getY()/size];
//			}
//		break;

		case (MotionEvent.ACTION_MOVE) :
			xUp = e.getX();
		yUp = e.getY();
		
		if (xDown == -1 || yDown == -1)
			break;

		if (xUp - xDown > sensitivity)
			gesture = "right";

		if (yUp - yDown > sensitivity)
			gesture = "down";

		if (xDown - xUp > sensitivity)
			gesture = "left";

		if (yDown - yUp > sensitivity)
			gesture = "up";
		
		if (gesture.equals("idk"))
		{
				
			if (refresh)
			{
				refresh = false;
				canvas.grid = new GridLock(size, size, canvas);
				invalidate();
			}
			break;
		}

		// set the gesture string and invalidate the view
		canvas.setGesture(gesture);

		moved_global = canvas.grid.move(gesture);
		locked = true;
		canvas.grid.calculateMoves(canvas.height, canvas.width);

		xDown = -1;
		yDown = -1;
		break;
		default : 
			return super.onTouchEvent(e);
		}      

		return true;
	}

}