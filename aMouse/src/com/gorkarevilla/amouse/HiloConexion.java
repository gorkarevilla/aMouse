package com.gorkarevilla.amouse;

import java.io.IOException;
import java.util.UUID;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

/**
 * Hilo que se ejecutara cuando queramos conectar con un
 * dispositivo
 * 
 */
class HiloConexion extends Thread {
	// Debugging
	private static final String TAG = "HiloConexion";
	private static final boolean D = true;

	private final BluetoothSocket _socket;
	private final BluetoothDevice _dispositivo;
	private final BluetoothAdapter _adaptadorBluetooth;
	private final ClienteBluetooth _bluetooth;
	private Handler _manejador;

	private final String _UUIDServicio = "08a66370-66ed-11e1-b86c-0800200c9a66"; 
	private final String _UUIDStackOverFlow = "00001101-0000-1000-8000-00805F9B34FB";
	private final UUID _UUID = UUID.fromString(_UUIDStackOverFlow);

	public HiloConexion(BluetoothAdapter adapter,BluetoothDevice device, Handler manejador,ClienteBluetooth bluetooth) {
		_dispositivo = device;
		_adaptadorBluetooth = adapter;
		_manejador = manejador;
		_bluetooth = bluetooth;
		
		BluetoothSocket tmp = null;

		// Get a BluetoothSocket for a connection with the
		// given BluetoothDevice
		try {
			tmp = _dispositivo.createRfcommSocketToServiceRecord(_UUID);
		} catch (IOException e) {
			if(D)Log.e(TAG, "Fallo al crear el Socket!", e);
		}
		_socket = tmp;
	}

	public void run() {
		if(D)Log.i(TAG, "Empieza HiloConexion");
		setName("HiloConexion");

		// Always cancel discovery because it will slow down a connection
		_adaptadorBluetooth.cancelDiscovery();

		// Make a connection to the BluetoothSocket
		try {
			// This is a blocking call and will only return on a
			// successful connection or an exception
			if(D)Log.i(TAG, "Conectando...");
			_socket.connect();
			if(D)Log.i(TAG, "Conectado!");
		} catch (IOException e) {
			//            connectionFailed();


			if(D)Log.e(TAG, "Se cancela la conexion.",e);
			cancelar();
			// Start the service over to restart listening mode

			//            BluetoothChatService.this.start();
			return;
		}
		
		aceptar();

		// Reset the ConnectThread because we're done
		//        synchronized (BluetoothChatService.this) {
		//            mConnectThread = null;
		//        }

		// Start the connected thread
//		     connected(_socket, _dispositivo);
	}

	public void cancelar() {
		if(D)Log.i(TAG, "Se cancela la conexion.");
		_bluetooth.setEstado(ClienteBluetooth.DESCONECTADO);
		_manejador.obtainMessage(Vista.MENSAJE_TOAST, ClienteBluetooth.ERROR_CONEXION, -1).sendToTarget();
		try {

			_socket.close();

		} catch (IOException e) {
			if(D)Log.e(TAG, "Se a cancelado la conexion.", e);
		}
	}
	
	public void aceptar() {
		if(D)Log.i(TAG, "Se acepta la conexion.");
		_bluetooth.setEstado(ClienteBluetooth.CONECTADO);	
		_bluetooth.conectado(_socket);
		
	}
	

}
