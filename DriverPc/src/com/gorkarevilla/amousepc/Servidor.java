package com.gorkarevilla.amousepc;

public class Servidor {

	private static Controlador _controlador;
	
	private static ServidorBluetooth _bluetooth;
	
	/**
	 * Controlador de PC que activa el bluetooth y se pone visible a la espera de que
	 * se conecte algun cliente.
	 * 
	 */
	public static void main(String[] args) {
		
		_controlador = new Controlador();
		
		_bluetooth = new ServidorBluetooth(_controlador);

	}
	
	
	/**
	 * Funcion que prueba los clicks
	 */
	private static void pruebaClicks() {
		_controlador.clickDerecho();
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


}
