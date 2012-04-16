package com.homelinux.ramuh76;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;


public class ServidorBluetooth {

	private Controlador _controlador;
	
	private LocalDevice _dispositivoBluetooth;
	
	private StreamConnectionNotifier _notificador;

	private final String _nombreServicio = "aMouse";	
	private final String _UUIDServicio = "08a6637066ed11e1b86c0800200c9a66";
	private final String _UUIDStackOverFlow = "0000110100001000800000805F9B34FB";
	private final UUID _UUID = new UUID(_UUIDStackOverFlow, false);
	
	private StreamConnection _socket=null;

	private InputStream _inStream;

	/**
	 * Constructor del servidor
	 * 
	 */
	public ServidorBluetooth(Controlador control) {
		//Tiene que cargar el dispositivo bluetooth, ponerse visible, arrancar el servicio y escuchar conexiones.

		_controlador = control;
		cargarDispositivo();
		
		setVisible(true);
		
		arrancarServicio();
		
		escucharConexiones();
		
		clienteNuevo();

	}


	/**
	 * Carga el dispositivo bluetooth del equipo en el que se ejecute. En caso de que no tenga dispositivo
	 * o no este accesible lanzara un aviso.
	 */
	public void cargarDispositivo() {

		try {
			_dispositivoBluetooth = LocalDevice.getLocalDevice();
			
		} catch (BluetoothStateException e) {
			e.printStackTrace();
			System.err.println("No se puede cargar el dispositivo Bluetooth");
		}	
		String address = _dispositivoBluetooth.getBluetoothAddress();
		System.out.println("Dirección bluetooth del"+
		" dispositivo local: "+address);
		
	}



	/**
	 * Pone al dispositivo en modo visible o invisible.
	 * 
	 * @param visible
	 */
	public void setVisible(boolean visible) {

		
		if(visible && _dispositivoBluetooth.getDiscoverable()==DiscoveryAgent.NOT_DISCOVERABLE) {
			try {
				if(!_dispositivoBluetooth.setDiscoverable(DiscoveryAgent.GIAC))
					System.out.println("El dispositivo no soporta el modo de conectividad 'GIAC'");
			} catch(BluetoothStateException e) {
				e.printStackTrace();
				System.err.println("No se pudo establecer la propiedad 'discoverable' a 'GIAC'");
			}
		} else if (!visible && _dispositivoBluetooth.getDiscoverable()!=DiscoveryAgent.NOT_DISCOVERABLE) {
			try {
				if(!_dispositivoBluetooth.setDiscoverable(DiscoveryAgent.NOT_DISCOVERABLE))
					System.out.println("No se puede deshabilitar la visibilidad del dispositivo");
			} catch(BluetoothStateException e) {
				e.printStackTrace();
				System.err.println("No se pudo establecer la propiedad 'NOT_DISCOVERABLE' al dispositivo");
			}
		}


	}
	

	
	public void arrancarServicio() {
		StringBuffer url = new StringBuffer("btspp://localhost:");
		//url.append((new UUID(0x12345)).toString());
		url.append(_UUID.toString());
		url.append(";name="+_nombreServicio);
		try {
			_notificador =	(StreamConnectionNotifier) Connector.open(url.toString());
			System.out.println("Creada Conexion: "+url.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	
	
	
	public void escucharConexiones() {
		System.out.println("Espero Conexion...");
		try {
			_socket = _notificador.acceptAndOpen();
			System.out.println("Conexion Entrante.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/*
	 * Cuando se a aceptado el cliente nuevo
	 */
	public void clienteNuevo() {
        InputStream tmpin = null;
        int totalx=0, totaly=0;

        // Get the BluetoothSocket input and output streams
        try {
            tmpin = _socket.openInputStream();
        } catch (IOException e) {
            System.out.println("FALLO: Socket de lectura NO cargado!");
        }
        
        _inStream = tmpin;
        
		while(_socket!=null) {
			byte[] buffer = new byte[3];
			int numbytes=0;
			try {
				numbytes = _inStream.read(buffer, 0, 3); //Leemos 3 bytes
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Numero de Bytes: "+numbytes);
			System.out.println("Bytes: "+buffer[0]+","+buffer[1]+","+buffer[2]);
			totalx+=buffer[1];
			totaly+=buffer[2];
			
			System.out.println("TotalX: "+totalx+" TotalY: "+totaly);
			
			_controlador.desplazarCursor(buffer[2], -buffer[1]);
		}
		
		System.out.println("Fin del Cliente.Se cierra el socket!");
		
	}
	
}
