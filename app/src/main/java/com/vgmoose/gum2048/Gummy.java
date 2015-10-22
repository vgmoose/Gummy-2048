package com.vgmoose.gum2048;

import java.io.Serializable;
import android.graphics.Color;

public class Gummy implements Serializable
{
	private static final long serialVersionUID = -5070662518184965729L;
	
	int intensity = 0;
	float x, y;
	float nextx, nexty;
	public boolean merged = false;
	private float xsize;
	Gummy past, parent;
	boolean newgum = true;
	
	public Gummy(int x, int y, float xsize)
	{
		this.xsize = xsize;
		newgum = true;
		updateXY(x, y);
	}
	
	public void updateXY(int x, int y)
	{
		this.x = xsize*x;
		this.y = xsize*y;
		nextx = this.x;
		nexty = this.y;
	}
	
	public void makeMoreIntense()
	{
		intensity += 20;
	}
	
	public int getIntensity()
	{
		return (int) Math.pow(2, (intensity/20+1));
	}
	
	public int getColor()
	{
		return Color.rgb(intensity, (100+intensity*2)%256, Math.abs((160-intensity*3)%256));
	}

	public void setTween(int i, int y2) 
	{
		nextx = xsize*i;
		nexty = xsize*y2;
		
	}

	public boolean calculate_move(float height, float width, int boardwidth) 
	{
		boolean auto_return = true;
		
		if (past != null)
			auto_return = past.calculate_move(height, width, boardwidth);
		
		if (nextx == x && nexty == y)
		{
			if (parent != null)
				parent.past = null; // social suicide
			
			return auto_return;
		}

		double speed = 1;
		
		if (x > nextx)
		{
			x -= speed;
			if (x < nextx)
				x = nextx;
		}
		
		if (y > nexty)
		{
			y -= speed;
			if (y < nexty)
				y = nexty;
		}
		
		if (x < nextx)
		{
			x += speed;
			if (x > nextx)
				x = nextx;
		}
		
		if (y < nexty)
		{
			y += speed;
			if (y > nexty)
				y = nexty;
		}
						
		return false;
		
	}

	public void setPast(Gummy gummy) 
	{
		past = gummy;
	}
	
	public void clearPast()
	{
		past = null;
	}

	public void setParent(Gummy gummy) {
		parent = gummy;
		
	}
}
