package com.homelinux.ramuh76;

import java.util.ArrayList;
import java.util.Set;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;

public class ClienteBluetooth{
	// Debugging
	private static final String TAG = "ClienteBluetooth";
	private static final boolean D = true;


	/*
	 * CODIGOS DE LOS ENVIOS
	 */
	private static final int CLICK_IZQUIERDO = 1;
	private static final int CLICK_DERECHO = 2;
	private static final int CLICK_IZQUIERDO_LARGO = 3;
	private static final int CLICK_DERECHO_LARGO = 4;
	private static final int MOVIMIENTO = 5;
	private static final int SOLTAR_BOTON = 6; //TODO

	/*
	 * CODIGOS DE ESTADO DEL DISPOSITIVO BLUETOOTH
	 */
	public static final int REQUEST_CONNECT_DEVICE = 11;
	public static final int REQUEST_ENABLE_BT = 12;
	public static final int REQUEST_DEVICE_FOUND = 13;

	/*
	 * CODIGOS DE ESTADO DEL CLIENTE
	 */
	public static final int CONECTADO = 21;
	public static final int DESCONECTADO = 22;
	public static final int CONECTANDO = 23;
	
	/*
	 * CODIGOS DE ERROR
	 */
	//MENSAJES DE TOAST
	public static final int ERROR_CONEXION = 91;

	//Estado del cliente por defecto sera desconectado
	private int _estado=DESCONECTADO;
	
	//Se encarga de pasar mensajes
	private final Handler _manejador;

	private Context _context;

	// Adaptador Bluetooth del dispositivo
	private BluetoothAdapter _adaptadorBluetooth = null;
	
	//Dispositivo de ultima conexion
	private BluetoothDevice _dispCon;
	
	//Socket de la conexion
	private BluetoothSocket _socket;
	
	
	//Hilos de Conexiones
	private HiloConexion _hiloConexion=null;
	private HiloConectado _hiloConectado=null;


	// Recibidor de las llamadas a broadcast para los dispositivos bluetooth
	public final BroadcastReceiver _recibidor = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			// Cuando se encuentra un dispositivo
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Cargamos el dispositivo bluetooth
				BluetoothDevice dispositivo = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				
				// Add the name and address to an array adapter to show in a ListView
				_dispositivosDesconocidos.add(dispositivo.getName() + "\n" + dispositivo.getAddress());;

				_manejador.obtainMessage(Vista.MENSAJE_NUEVO_DISPOSITIVO_ENCONTRADO, -1, -1).sendToTarget();
				
				if(D)Log.i(TAG,"NOMBRE: "+dispositivo.getName()+ " MAC: "+dispositivo.getAddress());
			}

			//Cuando se termina de buscar
			else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				if(D)Log.i(TAG,"Fin de la busqueda!");

			}
			
			//Cuando se empieza a buscar
			else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
				if(D)Log.i(TAG,"Dispositivos nuevos encontrados:");
			}
		}
	};



	//Dispositivos
	private ArrayList<String> _dispositivosConocidos;
	private ArrayList<String> _dispositivosDesconocidos;





	/**
	 * CONSTRUCTOR DEL BLUETOOTH
	 * 
	 * @param context
	 */
	public ClienteBluetooth(Context context, Handler manejador) {

		if(D)Log.i(TAG, "Arranca el Bluetooth");

		_context = context;
		_manejador = manejador;

		_dispositivosConocidos = new ArrayList<String>();
		_dispositivosDesconocidos = new ArrayList<String>();
		

		setEstado(DESCONECTADO);

		cargarBluetooth();

		activarBluetooth();

		registrarIntents();


	}

	/**
	 * Carga el adaptador Bluetooth
	 */
	private void cargarBluetooth() {
		//Cargamos el dispositivo bluetooth
		_adaptadorBluetooth = BluetoothAdapter.getDefaultAdapter();

		if (_adaptadorBluetooth == null) {
			//Decirle al Controlador que hay error
			if(D) Log.e(TAG,"Bluetooth No Disponible!!");
		}

	}

	/**
	 * En caso de estar desactivado el bluetooth lo activa
	 * 
	 */
	private void activarBluetooth() {


		//Si no esta activado, lo activamos.
		if (!_adaptadorBluetooth.isEnabled()) {
			Intent intentActivarBlue = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			((Activity) _context).startActivityForResult(intentActivarBlue, REQUEST_ENABLE_BT);
		}
	}


	/**
	 * Se encarga de registrar los Intents necesarios
	 */
	private void registrarIntents() {

		// Registrar el filtro de encontrar
		IntentFilter filtroEncontrarDispositivo = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		_context.registerReceiver(_recibidor, filtroEncontrarDispositivo); // Hay que eliminarlo en el onDestroy!!

		//Registrar el filtro de terminar la busqueda
		filtroEncontrarDispositivo = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		_context.registerReceiver(_recibidor, filtroEncontrarDispositivo); // Hay que eliminarlo en el onDestroy!!

		//Registrar el filtro de empezar la busqueda
		filtroEncontrarDispositivo = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		_context.registerReceiver(_recibidor, filtroEncontrarDispositivo); // Hay que eliminarlo en el onDestroy!!
	}


	/*
	 * Busqueda de clientes Conocidos
	 */
	public ArrayList<String> getDispositivosConocidos(){
		if(D)Log.i(TAG, "Buscamos Clientes Conocidos");

		_dispositivosConocidos.clear();

		Set<BluetoothDevice> pairedDevices = _adaptadorBluetooth.getBondedDevices();
		// If there are paired devices
		if (pairedDevices.size() > 0) {
			// Loop through paired devices
			for (BluetoothDevice device : pairedDevices) {
				// Add the name and address to an array adapter to show in a ListView
				_dispositivosConocidos.add(device.getName() + "\n" + device.getAddress());
			}
		}

		return _dispositivosConocidos;
	}



	/*
	 * Busqueda de clientes Nuevos
	 */
	public void buscarDispositivosDesconocidos() {

		if(D)Log.i(TAG, "Buscamos Clientes Nuevos");

		_dispositivosDesconocidos.clear();

		// Si ya estamos buscando lo cancelamos
		if (_adaptadorBluetooth.isDiscovering()) {
			_adaptadorBluetooth.cancelDiscovery();
		}

		//Empezamos a buscar
		_adaptadorBluetooth.startDiscovery();



	}

	/*
	 * Devuelve los clientes Nuevos cargados en la lista
	 */
	public ArrayList<String> getDispositivosDesconocidos() {
		return _dispositivosDesconocidos;
	}





	/*
	 * CONEXIONES Y COMUNICACIONES
	 */



	/**
	 * Conecta con el dispositivo determinado
	 * 
	 * 
	 * @param _dispCon
	 */
	public synchronized void conectar(String disp) {
		
		_dispCon = _adaptadorBluetooth.getRemoteDevice(disp);
		
		if (D) Log.d(TAG, "Conectar con: " + _dispCon);

		// Si esta conectado cancelamos el hilo
		if (_estado == CONECTADO) {
			/*
			if (_hiloConectado != null) {
				_hiloContectado.cancel();
				_hiloContectado = null;
			}
			*/
		}

		// Cancel any thread currently running a connection
//		if (_hiloConectado != null) {_hiloConectado.cancel(); _hiloConectado = null;}

		// Start the thread to connect with the given device
		_hiloConexion = new HiloConexion(_adaptadorBluetooth,_dispCon,_manejador,this);
		_hiloConexion.start();
		
		
		setEstado(CONECTANDO);
	}
	
	
	public synchronized void conectado(BluetoothSocket socket) {
		if (D) Log.d(TAG, "Conectado con: " + _dispCon);
		
		_socket = socket;
		
		_hiloConectado = new HiloConectado(_socket,_manejador,this);
		_hiloConectado.start();
		
	}




	public void setEstado(int estado) {
		if(D)Log.i(TAG, "Estado: "+_estado+" -> "+ estado);
		
		_estado = estado;
		
        // Hay que decirle a la vista que a cambiado el estado
        _manejador.obtainMessage(Vista.MENSAJE_CAMBIO_ESTADO, _estado, -1).sendToTarget();
        
	}







	/*
	 * EVENTOS QUE LLEGAN DESDE EL CONTROLADOR
	 */
	//CLICKS
	public void enviarClickIzquierdo() {
		int codigo = CLICK_IZQUIERDO;

		if(D)Log.i(TAG, "Codigo: "+codigo);

		//TODO
	}

	public void enviarClickDerecho() {
		int codigo = CLICK_DERECHO;

		if(D)Log.i(TAG, "Codigo: "+codigo);

		//TODO
	}

	public void enviarClickLargoIzquierdo() {
		int codigo = CLICK_IZQUIERDO_LARGO;

		if(D)Log.i(TAG, "Codigo: "+codigo);

		//TODO
	}

	public void enviarClickLargoDerecho() {
		int codigo = CLICK_DERECHO_LARGO;

		if(D)Log.i(TAG, "Codigo: "+codigo);

		//TODO
	}

	//MOVIMIENTOS
	public void enviarDesplazamiento(float x,float y) {
		int codigo = MOVIMIENTO;
		byte[] buffer = new byte[3];
		
		
		buffer[0]= (byte) MOVIMIENTO;
		buffer[1]=(byte) x;
		buffer[2]=(byte) y;

		if(_hiloConectado!=null)
		{
			if(D)Log.i(TAG, "Codigo: "+codigo);
			if(D)Log.i(TAG, "Values: ");
			if(D)Log.i(TAG, "-----0: "+buffer[0]);
			if(D)Log.i(TAG, "-----1: "+buffer[1]);
			if(D)Log.i(TAG, "-----2: "+buffer[2]);
			
			_hiloConectado.write(buffer);
		}


		//TODO
	}

}
