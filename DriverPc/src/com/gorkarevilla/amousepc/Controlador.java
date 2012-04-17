package com.gorkarevilla.amousepc;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;


/**
 * 
 * Esta clase se encarga de gestionar todos los eventos necesarios en el equipo
 * Principalmente realiza movimientos del puntero, y pulsaciones de las teclas del raton.
 * 
 * @author ramuh
 *
 */
public class Controlador {


	Robot _robot = null;


	public Controlador() {

		try {
			_robot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
			System.err.println("Se necesitan privilegios especiales!");
		}


	}


	/**
	 * Desplaza el cursor tantos pixeles como los indicados en x e y.
	 * Pueden ser positivos o negativos.
	 * 
	 * la pantalla seria:
	 * 
	 *    .----> X
	 * 	  |
	 *    |
	 *    |
	 *   \ /   Y
	 *    '
	 * 
	 * @param x
	 * @param y
	 */
	public void desplazarCursor(int x, int y) {

		Point p = MouseInfo.getPointerInfo().getLocation();
		_robot.mouseMove(p.x+x, p.y+y);

	}
	
	
	/**
	 * Realiza una pulsacion del primer boton del raton.
	 * Pulsar+Soltar
	 * 
	 */
	public void clickIzquierdo() {
		_robot.mousePress(InputEvent.BUTTON1_MASK);
		_robot.mouseRelease(InputEvent.BUTTON1_MASK);
	}
	
	/**
	 * Realiza una pulsacion del tercer boton del raton.
	 * Pulsar+Soltar
	 * 
	 */
	public void clickDerecho() {
		_robot.mousePress(InputEvent.BUTTON3_MASK);
		_robot.mouseRelease(InputEvent.BUTTON3_MASK);
	}
	
	/**
	 * Cuando se mantiene pulsado el primer boton
	 */
	public void clickIzquierdoPulsar() {
		_robot.mousePress(InputEvent.BUTTON1_MASK);
	}
	
	/**
	 * Cuando se suelta el primer boton
	 */
	public void clickIzquierdoSoltar() {
		_robot.mouseRelease(InputEvent.BUTTON1_MASK);
	}
	
	
	/**
	 * Cuando se mantiene pulsado el tercer boton
	 */
	public void clickDerechoPulsar() {
		_robot.mousePress(InputEvent.BUTTON3_MASK);
	}
	
	/**
	 * Cuando se suelta el tercer boton
	 */
	public void clickDerechoSoltar() {
		_robot.mouseRelease(InputEvent.BUTTON3_MASK);
	}

}
