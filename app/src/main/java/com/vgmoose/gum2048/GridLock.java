package com.vgmoose.gum2048;

import java.io.Serializable;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.Menu;

public class GridLock implements Serializable
{
	private static final long serialVersionUID = 7836966011291989659L;

	// height and width of grid (typically 4x4)
	int width, height;
	Gummy [][] gumgrid;
	transient Paint mpaint = new Paint();
	transient Paint textpaint = new Paint();
	transient Paint rectpaint = new Paint();

	int radius = 25;

	transient GameCanvas canvas;
	protected boolean hasMoved = false;
	private Menu slidePuppies;

	long timeStoreDuration= 0;

	public GridLock(int x, int y, GameCanvas canvas)
	{
		width = x;
		height = y;

		this.canvas = canvas;

		canvas.dv.ctx.startTime = System.currentTimeMillis();

		gumgrid = new Gummy[width][height];
		setupPaints();

		// add the two initial gummies
		addNewGummy();
		addNewGummy();
		
//		gumgrid[2][2] = new Gummy(2,2,0);
//		gumgrid[2][3] = new Gummy(2,3,0);
//		gumgrid[2][0] = new Gummy(2,0,0);
//		gumgrid[0][2] = new Gummy(0,2,0);
//		gumgrid[3][2] = new Gummy(3,2,0);


	}

	void setupPaints() 
	{

		mpaint = new Paint();
		textpaint = new Paint();
		rectpaint = new Paint();

		if (canvas.numbers == 0)
			textpaint.setTextSize(70);
		else if (canvas.numbers == 1)
			textpaint.setTextSize(35);

		textpaint.setColor(Color.WHITE);
		mpaint.setStyle(Style.FILL);
		rectpaint.setStyle(Style.STROKE);
		rectpaint.setColor(Color.LTGRAY);

	}

	public void addNewGummy() 
	{
		float s = canvas.dv.sensitivity;
		float xsize = (canvas.width-s*2/3)/this.width;

		int randx, randy;
		do {
			randx = (int)(Math.random()*this.width);
			randy = (int)(Math.random()*this.width);
		} 
		while (gumgrid[randx][randy] !=null);

		Gummy testGum = new Gummy(randx,randy, xsize);
		gumgrid[randx][randy] = testGum;

	}

	public boolean move(String gesture)
	{
		// fail safe
		if (gesture.equals(""))
			return false;

		hasMoved = true;

		boolean moved = false;
		int xtar = 0, ytar = 0;

		if (gesture.equals("up"))
			ytar = -1;
		else if (gesture.equals("down"))
			ytar = 1;
		else if (gesture.equals("left"))
			xtar = -1;
		else if (gesture.equals("right"))
			xtar = 1;
		else
			return false;
		
		int xStarter, xBreakOff, yStarter, yBreakOff, yChange, xChange;
		
		if (gesture.equals("up") || gesture.equals("left"))
		{
			xStarter = 0;
			xBreakOff = this.width-1;
			yStarter = 0;
			yBreakOff = this.height-1;
			xChange = 1;
			yChange = 1;
		}
		else
		{
			xStarter = this.width - 1;
			xBreakOff = 0;
			yStarter = this.height - 1;
			yBreakOff = 0;
			xChange = -1;
			yChange = -1;
		}

		while (true)
		{
			int swaps = 0;
			
			int x = xStarter;

			while (true)
			{
				int y = yStarter;
				
				while (true)
				{
					if (x+xtar >= 0 && y+ytar >= 0 && x+xtar<this.width && y+ytar<this.height && gumgrid[x][y] != null)
					{
						if (gumgrid[x+xtar][y+ytar] == null && gumgrid[x+xtar][y+ytar] != gumgrid[x][y])
						{
							// move the gummy
							gumgrid[x+xtar][y+ytar] = gumgrid[x][y];
							gumgrid[x+xtar][y+ytar].setTween(x+xtar, y+ytar);

							gumgrid[x][y] = null;
							swaps++;

							moved = true;

						} else if (gumgrid[x+xtar][y+ytar].getIntensity() == gumgrid[x][y].getIntensity() && !gumgrid[x][y].merged && !gumgrid[x+xtar][y+ytar].merged)
						{
							gumgrid[x+xtar][y+ytar].makeMoreIntense();
							gumgrid[x+xtar][y+ytar].setPast(gumgrid[x][y]);
							gumgrid[x][y].setParent(gumgrid[x+xtar][y+ytar]);

							gumgrid[x+xtar][y+ytar].past.setTween(x+xtar,y+ytar);
							gumgrid[x+xtar][y+ytar].merged = true;

							gumgrid[x][y] = null;
							swaps++;
							moved = true;
						}
					}
					
					if (y == yBreakOff)
						break;
					y += yChange;

				}
				
				
				if (x == xBreakOff)
					break;
				
				x += xChange;

			}
			if (swaps == 0)
				break;

		}

		return moved;

	}

	public void drawSelf(Canvas g, float height, float width) 
	{
		float s = canvas.dv.sensitivity;
		float xsize = (width-s*2/3)/this.width;

		//		if (canvas.numbers)

		g.drawRect(s/3, s, s/3+xsize+xsize*(this.width-1), s+xsize+xsize*(this.width-1), rectpaint);

		int count = 0;

		for (int x=0; x<this.width; x++)
			for (int y=0; y<this.height; y++)
			{
				if (gumgrid[x][y] == null)
					continue;

				count ++;
				gumgrid[x][y].merged = false;

				mpaint.setColor(gumgrid[x][y].getColor());

				RectF rect = new RectF(s/3+xsize*x, s+xsize*y, s/3+xsize+xsize*x, s+xsize+xsize*y);
				if (canvas.numbers%2 == 0)
					g.drawRoundRect(rect, radius, radius, mpaint);
				else
					g.drawRect(rect, mpaint);				

				Rect bounds = new Rect();
				textpaint.getTextBounds(""+gumgrid[x][y].getIntensity(), 0, (""+gumgrid[x][y].getIntensity()).length(), bounds);

				if (canvas.numbers >=2 && canvas.numbers <= 5)
					g.drawText(""+gumgrid[x][y].getIntensity(), s/3+xsize*x, s+xsize+xsize*y, textpaint);
				else if (canvas.numbers >= 0 && canvas.numbers <= 1)
					g.drawText(""+gumgrid[x][y].getIntensity(), (float)(s/3+gumgrid[x][y].x+xsize/2.0-bounds.width()/1.65), (float)(s+gumgrid[x][y].y+xsize/2.0+bounds.height()/2.0), textpaint);

			}
		//		Log.v("eee", ""+count);
	}

	public void calculateMoves(final float height, final float width) 
	{
		final int bwidth = this.width;
		final int bheight = this.height;
		final GameCanvas c = this.canvas;

		new Thread( new Runnable(){
			public void run()
			{
				while (true)
				{
					boolean breakout = true;
					for (int x=0; x<bwidth; x++)
						for (int y=0; y<bheight; y++)
						{
							if (gumgrid[x][y] == null)
								continue;

							if (!gumgrid[x][y].calculate_move(height, width, bwidth))
								breakout = false;
						}

					canvas.dv.ctx.runOnUiThread(new Runnable(){
						public void run(){
							canvas.dv.invalidate();
						}
					});

					int delay = 100000;

					// busy wait for nanoseconds
					long start = System.nanoTime();
					while(start + delay >= System.nanoTime());

					//					try {
					//						Thread.sleep(1);
					//					} catch (InterruptedException e) {
					//						// TODO Auto-generated catch block
					//						e.printStackTrace();
					//					}

					if (breakout) 
					{

						if (canvas.dv.moved_global)
							canvas.grid.addNewGummy();

						canvas.dv.ctx.runOnUiThread(new Runnable(){
							public void run(){
								canvas.dv.invalidate();
							}
						});

						if (checkForWin())
							canvas.dv.ctx.loseGame(true);
						else if (checkForLoss())
							canvas.dv.ctx.loseGame(false);
						else
							canvas.dv.locked = false;

						break;
					}
				}


			}}).start();
	}

	protected boolean checkForWin() 
	{
		for (int x=0; x<this.width; x++)
			for (int y=0; y<this.height; y++)
			{
				if (gumgrid[x][y] == null)
					continue;

				if (gumgrid[x][y].getIntensity() == 2048)
					return true;
			}

		return false;
	}

	void removePasts()
	{
		for (int x=0; x<this.width; x++)
			for (int y=0; y<this.height; y++)
				if (gumgrid[x][y] != null)
					gumgrid[x][y].past = null;
	}

	protected boolean checkForLoss() 
	{
		for (int x=0; x<this.width; x++)
			for (int y=0; y<this.height; y++)
			{
				if (gumgrid[x][y] == null)
					return false;

				if (x-1>=0 && gumgrid[x-1][y].getIntensity() == gumgrid[x][y].getIntensity())
					return false;

				if (y-1>=0 && gumgrid[x][y-1].getIntensity() == gumgrid[x][y].getIntensity())
					return false;
			}

		return true;

	}

	public void drawRaw(Canvas g, float height, float width) 
	{
		float s = canvas.dv.sensitivity;

		float xsize = (width-s*2/3)/this.width;
		g.drawRect(s/3, s, s/3+xsize+xsize*(this.width-1), s+xsize+xsize*(this.width-1), rectpaint);

		for (int x=0; x<this.width; x++)
			for (int y=0; y<this.height; y++)
			{
				if (gumgrid[x][y] == null)
					continue;

				Gummy thisGum = gumgrid[x][y];
				Gummy pastGum = gumgrid[x][y].past;

				if (pastGum != null)
				{
					// draw the old version for a merge too
					mpaint.setColor(pastGum.getColor());

					RectF rect = new RectF(s/3+thisGum.x, s+thisGum.y, s/3+xsize+thisGum.x, s+xsize+thisGum.y);
					if (canvas.numbers%2 == 0)
						g.drawRoundRect(rect, radius, radius, mpaint);
					else
						g.drawRect(rect, mpaint);

					rect = new RectF(s/3+pastGum.x, s+pastGum.y, s/3+xsize+pastGum.x, s+xsize+pastGum.y);
					if (canvas.numbers%2 == 0)
						g.drawRoundRect(rect, radius, radius, mpaint);
					else
						g.drawRect(rect, mpaint);
					
					Rect bounds = new Rect();
					textpaint.getTextBounds(""+pastGum.getIntensity(), 0, (""+pastGum.getIntensity()).length(), bounds);

					if (canvas.numbers >=2 && canvas.numbers <= 5)
					{
						g.drawText(""+pastGum.getIntensity(), s/3+thisGum.x, s+thisGum.y+xsize, textpaint);
						g.drawText(""+pastGum.getIntensity(), s/3+pastGum.x, s+pastGum.y+xsize, textpaint);
					}
					else if (canvas.numbers >= 0 && canvas.numbers <= 1)
					{
						g.drawText(""+pastGum.getIntensity(), (float)(s/3+thisGum.x+xsize/2.0-bounds.width()/1.65), (float)(s+thisGum.y+xsize/2.0+bounds.height()/2.0), textpaint);
						g.drawText(""+pastGum.getIntensity(), (float)(s/3+pastGum.x+xsize/2.0-bounds.width()/1.65), (float)(s+pastGum.y+xsize/2.0+bounds.height()/2.0), textpaint);
					}

				}
				else
				{
					mpaint.setColor(thisGum.getColor());

					RectF rect = new RectF(s/3+thisGum.x, s+thisGum.y, s/3+xsize+thisGum.x, s+xsize+thisGum.y);
					
					if (canvas.numbers%2 == 0)
						g.drawRoundRect(rect, radius, radius, mpaint);
					else
						g.drawRect(rect, mpaint);

					Rect bounds = new Rect();
					textpaint.getTextBounds(""+thisGum.getIntensity(), 0, (""+thisGum.getIntensity()).length(), bounds);

					if (canvas.numbers >=2 && canvas.numbers <= 5)
						g.drawText(""+thisGum.getIntensity(), s/3+thisGum.x, s+thisGum.y+xsize, textpaint);
					else if (canvas.numbers >= 0 && canvas.numbers <= 1)
						g.drawText(""+thisGum.getIntensity(), (float)(s/3+thisGum.x+xsize/2.0-bounds.width()/1.65), (float)(s+thisGum.y+xsize/2.0+bounds.height()/2.0), textpaint);

				}
			}
	}



}
