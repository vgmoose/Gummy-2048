package com.vgmoose.gum2048;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.util.Timer;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class Gummy2048 extends Activity {

	static final String DEBUG_TAG = "debug";

	private DrawPanel drawView;
	Gummy2048 ctx;
	SharedPreferences sharedPrefs;
	SharedPreferences.Editor editor;
	long startTime;
	Toast notify;

	AdView adView;

	private MenuItem menu1;

	private MenuItem menu2;

	private boolean showTheAd;

	private boolean debugMode = false;

	private int clickCounter;

	boolean GodMode = false;

	private Menu menu_tracker;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_gummy2048);
		sharedPrefs = getPreferences(Context.MODE_PRIVATE);
		editor = sharedPrefs.edit();
		drawView = new DrawPanel(this, this);
		//		drawView.setBackgroundColor(Color.LTGRAY);


		RelativeLayout layout2 = (RelativeLayout)findViewById(R.id.fullscreen_content); 

		layout2.addView(drawView);
		ctx = this;
		notify = Toast.makeText(ctx, "", Toast.LENGTH_SHORT);

		Button btn = (Button) findViewById(R.id.dummy_button);
		btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) 
			{
				drawView.canvas.numbers = (drawView.canvas.numbers+1)%8;
				notify.setText(ctx.getResources().getStringArray(R.array.display)[drawView.canvas.numbers]);

				if (drawView.canvas.numbers <= 3)
					drawView.canvas.grid.textpaint.setTextSize(70);
				else
					drawView.canvas.grid.textpaint.setTextSize(30);

				notify.show();
				drawView.invalidate();

				editor.putInt("numbers", drawView.canvas.numbers);
				editor.commit();
			}
		});

		Button btn2 = (Button) findViewById(R.id.reset_button);
		btn2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) 
			{
				if (!drawView.canvas.grid.hasMoved)
				{
					// reset the game
					ctx.setHighScores();
					drawView.canvas.grid = new GridLock(drawView.size,drawView.size, drawView.canvas);
					drawView.invalidate();
				}
				else
				{
					AlertDialog.Builder show = new AlertDialog.Builder(ctx)
					.setTitle(ctx.getString(R.string.warning))
					.setMessage(ctx.getString(R.string.resettext))
					.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) 
						{ 
							// reset the game
							ctx.setHighScores();
							drawView.canvas.grid = new GridLock(drawView.size,drawView.size, drawView.canvas);
							drawView.invalidate();
						}})
						.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) { 
								// do nothing
							}
						});

					show.show();
				}


			}
		});

		Button btn3 = (Button) findViewById(R.id.size_button);
		btn3.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) 
			{
				if (!drawView.canvas.grid.hasMoved)
				{
					// reset the game
					ctx.setHighScores();
					drawView.size = 3 + (drawView.size-2)%4; // max size 6, min 3
					drawView.canvas.grid = new GridLock(drawView.size,drawView.size, drawView.canvas);
					drawView.invalidate();

					notify.setText(""+drawView.size+"x"+drawView.size);
					notify.show();
				} else
				{
					AlertDialog.Builder show = new AlertDialog.Builder(ctx)
					.setTitle(ctx.getString(R.string.warning))
					.setMessage(ctx.getString(R.string.resettext))
					.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) 
						{ 
							// reset the game
							ctx.setHighScores();
							drawView.size = 3 + (drawView.size-2)%4; // max size 6, min 3
							drawView.canvas.grid = new GridLock(drawView.size,drawView.size, drawView.canvas);
							drawView.invalidate();
						}})
						.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) { 
								// do nothing
							}
						});

					show.show();
				}

				editor.putInt("size", drawView.size);
				editor.commit();

			}
		});

		Button btn4 = (Button) findViewById(R.id.hs_button);
		btn4.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) 
			{
				String scores = getScores();
				AlertDialog.Builder show = new AlertDialog.Builder(ctx)
				.setTitle(ctx.getString(R.string.stattext))
				.setMessage(ctx.getString(R.string.size_button)+"\n"+scores)
				.setNegativeButton(R.string.dismiss, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) { 
						// do nothing
					}
				});
				//                 .setIcon(R.drawable.ic_launcher);
				show.show();
			}

		});

		showTheAd = sharedPrefs.getBoolean("showAd", true);

		if (android.os.Build.VERSION.SDK_INT >= 8 && showTheAd)
			makeAd();
		else
			showTheAd = false;
		
		loadSaveFile();

	}

	private void rmAd()
	{
		LinearLayout layout = (LinearLayout)findViewById(R.id.adLayout);
		layout.removeAllViews();
	}


	private void makeAd()
	{
		showTheAd = true;

		// Create the adView.
		adView = new AdView(this);
		adView.setAdUnitId("ca-app-pub-8148658375496745/2163947704");
		adView.setAdSize(AdSize.BANNER);

		// Lookup your LinearLayout assuming it's been given
		// the attribute android:id="@+id/mainLayout".
		LinearLayout layout = (LinearLayout)findViewById(R.id.adLayout);

		// Add the adView to it.
		layout.addView(adView);

		// Initiate a generic request.
		AdRequest adRequest = new AdRequest.Builder().build();

		// Load the adView with the ad request.
		adView.loadAd(adRequest);

	}


	public boolean setHighScores()
	{
		int z = drawView.size;
		int highGummy = sharedPrefs.getInt("high"+z+"gummy", 0);
		int realHighGummy = highGummy;
		int highTime = sharedPrefs.getInt("high"+z+"time", 0);

		boolean noteworthy = false;
		boolean setNew = false;

		// stop the timer
		long endTime = System.currentTimeMillis();

		// find the current highest
		for (int x=0; x<drawView.canvas.grid.gumgrid.length; x++)
			for (int y=0; y<drawView.canvas.grid.gumgrid[x].length; y++)
				if (drawView.canvas.grid.gumgrid[x][y] != null)
					if (drawView.canvas.grid.gumgrid[x][y].getIntensity() >= highGummy)
					{
						noteworthy = true;
						highGummy = drawView.canvas.grid.gumgrid[x][y].getIntensity();
					}

		if (noteworthy)
		{
			int thisTime = (int) (endTime - startTime);
			thisTime /= 1000;

			if (realHighGummy < highGummy)
			{
				highTime = thisTime;
				setNew = true;
			}
			else if (thisTime <= highTime)
			{
				setNew = true;
				highTime = thisTime;
			}
		}

		editor.putInt("high"+z+"gummy", highGummy);
		editor.putInt("high"+z+"time", highTime);

		editor.commit();

		return setNew;
	}
	
	protected void onResume()
	{
		super.onResume();
		loadSaveFile();
	}
	
	protected void loadSaveFile()
	{
		String filename = "inprogress.sav";
		File mydir = getDir("saves", Context.MODE_PRIVATE); //Creating an internal dir;
		File fileWithinMyDir = new File(mydir, filename); //Getting a file within the dir.
		
		if (fileWithinMyDir.exists())
		{
			try {
				load(-1);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void finish()
	{
		try {
			if (drawView.canvas.grid.hasMoved)
				serialize();
		} catch (Exception e) {
			e.printStackTrace();
		}

		super.finish();
	}

	protected void onPause()
	{
		try {
			if (drawView.canvas.grid.hasMoved)
				serialize();
		} catch (Exception e) {
			e.printStackTrace();
		}

		super.onPause();
	}

	public void serialize() throws IOException
	{
		serialize(-1);
	}

	public void serialize(int which) throws IOException
	{
		String filename = "inprogress.sav";

		if (which != -1)
			filename = "debug" + which + ".sav";

		File mydir = getDir("saves", Context.MODE_PRIVATE); //Creating an internal dir;
		File fileWithinMyDir = new File(mydir, filename); //Getting a file within the dir.
		FileOutputStream fileOut = new FileOutputStream(fileWithinMyDir); //Use the stream as usual to write into the file

		// set timestamp
		drawView.canvas.grid.timeStoreDuration = System.currentTimeMillis() - startTime;
		
		ObjectOutputStream out = new ObjectOutputStream(fileOut);
		out.writeObject(drawView.canvas.grid);
		out.close();
		
	}

	private void load(int which) throws OptionalDataException, ClassNotFoundException, IOException 
	{
		String filename = "inprogress.sav";

		if (which != -1)
			filename = "debug" + which + ".sav";

		File mydir = getDir("saves", Context.MODE_PRIVATE); //Creating an internal dir;
		File fileWithinMyDir = new File(mydir, filename); //Getting a file within the dir.
		FileInputStream fileIn = new FileInputStream(fileWithinMyDir); //Use the stream as usual to write into the file

		ObjectInputStream in = new ObjectInputStream(fileIn);
		drawView.canvas.grid = (GridLock) in.readObject();
		in.close();
		
		// delete file if a progress load
		if (which == -1)
			fileWithinMyDir.delete();
		
		// load some variables
		drawView.size = drawView.canvas.grid.height;
		startTime = System.currentTimeMillis() - drawView.canvas.grid.timeStoreDuration;
		
		drawView.refresh = false;
		
		// set some others
		drawView.canvas.grid.canvas = drawView.canvas;
		
		drawView.canvas.grid.setupPaints();
		drawView.invalidate();

	}

	private String getScores() 
	{
		String s = "";
		for (int x=3; x<=6; x++)
		{
			String thisGummy = ""+sharedPrefs.getInt("high"+x+"gummy", 0);
			//			for (int y=thisGummy.length(); y<4; y++)
			//				thisGummy = "-" + thisGummy;

			int time_sec = sharedPrefs.getInt("high"+x+"time", 9999);
			int time_min = time_sec/60;
			time_sec = (time_sec%60);
			String thisTime = ((time_min!=0)? ""+time_min+"m " : "") + time_sec + "s";
			//			for (int y=thisTime.length(); y<5; y++)
			//				thisTime = "-" + thisTime;
			s += " "+x+"x"+x+":\t\t"+thisGummy+"\t\t("+thisTime+")\n";
		}
		return s;
	}



	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub

		menu_tracker = menu;
		
		menu.clear();

		menu1 = menu.add(Menu.NONE, 1, Menu.NONE, ctx.getString(R.string.showad)).setCheckable(true).setChecked(!showTheAd);
		menu2 = menu.add(Menu.NONE, 2, Menu.NONE, ctx.getString(R.string.review));

		if (debugMode)
		{
			menu.add(Menu.NONE, 3, Menu.NONE, ctx.getString(R.string.save));
			menu.add(Menu.NONE, 4, Menu.NONE, ctx.getString(R.string.load));
//			menu.add(Menu.NONE, 5, Menu.NONE, ctx.getString(R.string.gmode));
		}

		return super.onCreateOptionsMenu(menu);

	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public boolean onKeyLongPress(int keyCode, KeyEvent event)
	{
//		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH && !debugMode)
//		{
//			ActionBar bar = getActionBar();
//			bar.setHomeButtonEnabled(true);
//		}
		clickCounter++;
		if (clickCounter == 2)
		{
		debugMode = true;
		onCreateOptionsMenu(menu_tracker);
		}
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case android.R.id.home:

			clickCounter++;

			if (clickCounter >= 5 && !debugMode)
			{
				updateOptions();
				debugMode = true;
			}
			break;
		case 1:
			showTheAd = !showTheAd;
			if (showTheAd)
				makeAd();
			else
				rmAd();
			menu1.setChecked(!showTheAd);
			editor.putBoolean("showAd", showTheAd);
			editor.commit();
			break;
		case 2:
			try
			{
				Uri marketUri = Uri.parse("market://details?id=" + getPackageName());
				Intent i = new Intent(Intent.ACTION_VIEW, marketUri);
				startActivity(i);
			}
			catch (Exception e)
			{
				notify.setText(R.string.market);
				notify.show();
			}
			break;
		case 3: 
			openStateMenu(true);
			break;
		case 4:
			openStateMenu(false);
			break;
//		case 5:
//			GodMode = !GodMode ;
//			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void openStateMenu(final boolean b) 
	{		
		String[] saves = new String[5];
		for (int x=0; x<saves.length; x++)
			saves[x] = "Slot " + (x+1);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(b? R.string.save : R.string.load)
		.setItems(saves, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) 
			{
				if (b)
				{
					try {
						serialize(which);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				else
				{
					try {
						load(which);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		});
		
		builder.show();
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void updateOptions() 
	{
//		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
//		{
//			invalidateOptionsMenu();
//			ActionBar bar = getActionBar();
//			bar.setHomeButtonEnabled(false);
//		}
	}

	public void loseGame(final boolean b) 
	{

		runOnUiThread(new Runnable(){
			public void run(){
				boolean setNew = setHighScores();

				String thisTitle, thisMessage;

				if (b)
				{
					thisTitle = ctx.getString(R.string.congrats);
					thisMessage = ctx.getString(R.string.gamewintext)+((setNew)? ctx.getString(R.string.newscore) : "");
				}
				else
				{
					thisTitle = ctx.getString(R.string.gameover);
					thisMessage = ctx.getString(R.string.gameovertext)+((setNew)? ctx.getString(R.string.newscore) : "");
				}

				AlertDialog.Builder show = new AlertDialog.Builder(ctx)
				.setTitle(thisTitle)
				.setCancelable(false)
				.setMessage(thisMessage)
				.setNegativeButton(R.string.dismiss, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) { 
						////						drawView.canvas.grid = new GridLock(drawView.size,drawView.size, drawView.canvas);
						//						drawView.invalidate();
						drawView.locked = false;
						drawView.canvas.grid.hasMoved = false;
						drawView.refresh = true;

					}
				});
				//                 .setIcon(R.drawable.ic_launcher);

				show.show();
			}
		});



	}

}
