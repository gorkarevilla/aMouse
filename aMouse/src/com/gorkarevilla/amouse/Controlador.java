package com.gorkarevilla.amouse;

import java.util.ArrayList;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.os.Handler;
import android.util.Log;

public class Controlador {
	// Debugging
	private static final String TAG = "Controlador";
	private static final boolean D = true;
	
	private Context _context;
	private Handler _manejador;
	
	//Datos
	private int _sensibilidad=50;
	private boolean _movimientoshabilitados=false;
	private float[] _velocidad = {0f,0f,0f};
	
	//Conexion Bluetooth
	public ClienteBluetooth _bluetooth;
	
	
	/**
	 * Constructor del controlador
	 */
	public Controlador(Context context, Handler manejador) {
		
		if(D)Log.i(TAG, "Arranca el Controlador");
		_context = context;
		_manejador = manejador;
		_bluetooth = new ClienteBluetooth(_context, manejador);
		
	}
	
	
	/*
	 * EVENTOS QUE LLEGAN DESDE LA VISTA
	 */
	//CLICKS
	public void clickIzquierdo() {
		_bluetooth.enviarClickIzquierdo();
	}
	
	public void clickDerecho() {
		_bluetooth.enviarClickDerecho();
	}
	
	public void clickLargoIzquierdo() {
		_bluetooth.enviarClickLargoIzquierdo();
	}
	
	public void clickLargoDerecho() {
		_bluetooth.enviarClickLargoDerecho();
	}
	
	//SENSORES
	public void cambioEnSensor(SensorEvent event) {
		/*
		 * Los values son:
		 * 0: X
		 * 1: Y
		 * 2: Z
		 * 
		 * Dejando el movil sobre una superficie plana y la pantalla hacia arriba y mirandolo de frente:
		 * 
		 *        .
		 *       / \
		 *        |   Y
		 *        |
		 *   __________
		 *  /          \
		 *  |  ______  |
		 *  | |      | |
		 *  | |      | |
		 *  | |      | |
		 *  | |      | |    ----> X
		 *  | |      | |
		 *  | |______| |
		 *  |          |
		 *  |----------|
		 *  |    O     |
		 *  \----------/
		 *  
		 *  
		 *  
		 *  Z seria el eje hacia arriba de la pantalla.
		 */
		

/*
 		//Version 2.2 y menores
		if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER) {



			if(D)Log.i(TAG, "Movimiento en el Sensor de Aceleracion!!");
			if(D)Log.i(TAG, "SensorValues X: "+event.values[0]);
			if(D)Log.i(TAG, "SensorValues Y: "+event.values[1]);
			if(D)Log.i(TAG, "SensorValues Z: "+event.values[2]);
			
			
			// In this example, alpha is calculated as t / (t + dT),
			// where t is the low-pass filter's time-constant and
			// dT is the event delivery rate.

			final float alpha = 0.8f;

			float[] gravity = new float[3];
			float[] linear_acceleration = new float[3];

			// Isolate the force of gravity with the low-pass filter.
			gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
			gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
			gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

			// Remove the gravity contribution with the high-pass filter.
			linear_acceleration[0] = event.values[0] - gravity[0];
			linear_acceleration[1] = event.values[1] - gravity[1];
			linear_acceleration[2] = event.values[2] - gravity[2];

			if(D)Log.i(TAG, "Aceleracion X: "+linear_acceleration[0]);
			if(D)Log.i(TAG, "Aceleracion Y: "+linear_acceleration[1]);
			if(D)Log.i(TAG, "Aceleracion Z: "+linear_acceleration[2]);
			
			//Hay que quitarle la gravedad!!

			linear_acceleration[0]=linear_acceleration[0]*_sensibilidad;
			linear_acceleration[1]=linear_acceleration[1]*_sensibilidad;
			linear_acceleration[2]=linear_acceleration[2]*_sensibilidad;
			
			if(D)Log.i(TAG, "Desplazamiento X: "+linear_acceleration[0]);
			if(D)Log.i(TAG, "Desplazamiento Y: "+linear_acceleration[1]);
			if(D)Log.i(TAG, "Desplazamienta Z: "+linear_acceleration[2]);
			
			
			_bluetooth.enviarDesplazamiento(linear_acceleration[0], linear_acceleration[1]);
			
			
		}
		
		
*/
		
		//Version 2.3 y superiores
		if(event.sensor.getType()==Sensor.TYPE_LINEAR_ACCELERATION) {
			
			
			float[] aceleracion = new float[3]; //Aceleracion con un decimal p.e.: 1.2f {X,Y,Z}
			
			aceleracion[0] = (float) Math.rint(event.values[0]*10)/10;
			aceleracion[1] = (float) Math.rint(event.values[1]*10)/10;
			aceleracion[2] = (float) Math.rint(event.values[2]*10)/10;
			
			if(D)Log.i(TAG, "Movimiento en el Sensor de Aceleracion Lineal!!");
			if(D)Log.i(TAG, "Aceleracion X: "+aceleracion[0]);
			if(D)Log.i(TAG, "Aceleracion Y: "+aceleracion[1]);
			if(D)Log.i(TAG, "Aceleracion Z: "+aceleracion[2]);
			
			
			
			_velocidad[0] += aceleracion[0];
			_velocidad[1] += aceleracion[1];
			_velocidad[2] += aceleracion[2];
			
			if(D)Log.i(TAG, "Velocidad X: "+aceleracion[0]);
			if(D)Log.i(TAG, "Velocidad Y: "+aceleracion[1]);
			if(D)Log.i(TAG, "Velocidad Z: "+aceleracion[2]);
			
			
			if(D)Log.i(TAG, "--------------------------------------------------");
			
			
			
		
			if( _velocidad[0]!=0.0f || _velocidad[1]!=0.0f || _velocidad[2]!=0.0f ) {

				
				_bluetooth.enviarDesplazamiento(_velocidad[0] * _sensibilidad, _velocidad[1] * _sensibilidad);
				
			}
			

		}
		
		
		else {
			//if(D)Log.i(TAG, "Sensor No Tratado: "+event.sensor.getName());
		}
	}
	
	//BLUETOOTH
	public void buscarDispositivosDesconocidos() {

		 _bluetooth.buscarDispositivosDesconocidos();
	}
	
	public ArrayList<String> getDispositivosDesconocidos() {
		
		return _bluetooth.getDispositivosDesconocidos();
		
	}
	
	public ArrayList<String> getDispositivosConocidos() {
		
		return _bluetooth.getDispositivosConocidos();
	}
	
	public boolean linkarDispConocido(String disp) {
		_bluetooth.conectar(disp);
		return true;
		
	}
	
	public boolean linkarDispDesconocido(String disp) {
		_bluetooth.conectar(disp);
		return true;
		
	}
	
	
	
	
	
	
	
	/*
	 * GETTERS Y SETTERS
	 */
	
	public int getSensibilidad() {
		return _sensibilidad;
	}
	
	public void setSensibilidad(int s) {
		_sensibilidad=s;
	}
	
	public boolean getMoviminetosHabilitados() {
		return _movimientoshabilitados;
	}
	
	public void setMovimientosHabilitados(boolean b) {
		_movimientoshabilitados=b;
	}

}
