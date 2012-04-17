package com.gorkarevilla.amousepc;


import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;

public class Prueba {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Robot robot = null;
		try {
			robot = new Robot();
		} catch (AWTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		for(int i=0;i<1000;++i)
		{
			Point p = MouseInfo.getPointerInfo().getLocation();
			robot.mouseMove(p.x+1, p.y+1);
		}
		
	}

}
