package com.gorkarevilla.amousepc;




import java.io.IOException;
import java.util.Vector;

import javax.bluetooth.*;

public class PruebasBlue implements DiscoveryListener
{

	private static String _address;
	private static String _friendlyName;
	private static String _discoverableString;
	private static StringBuffer _device;
	private static Object lock=new Object();
	private static Vector<RemoteDevice> dispositivos= new Vector<RemoteDevice>();
	



	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		PruebasBlue Prueba = new PruebasBlue();

		Prueba.arrancarblue();


		Prueba.imprimirInfo();


		Prueba.comenzarbusqueda();
		
		
		Prueba.mostrarresultadosbusqueda();
		

		Prueba.mostrarDispositivosRemotos();



	}


	public void arrancarblue(){
		LocalDevice localDevice = null;
		try {
			localDevice = LocalDevice.getLocalDevice();
		} catch(BluetoothStateException e) {
			System.out.println("Error al iniciar el sistema Bluetooth");
			System.out.println(e.getMessage());
			return;
		}
		
		_address = localDevice.getBluetoothAddress();
		System.out.println("Dirección bluetooth del dispositivo local: "+_address);



		_friendlyName = localDevice.getFriendlyName();
		if(_friendlyName == null)
			System.err.println("El dispositivo local no soporta un 'friendly-name' o no ha sido establecido");
		else
			System.out.println("El 'friendly-name' del dispositivo local es: "+	_friendlyName);
		try {
			if(!localDevice.setDiscoverable(DiscoveryAgent.GIAC))
				System.out.println("El dispositivo no soporta el modo de conectividad 'GIAC'");
		} catch(BluetoothStateException e) {
			System.out.println(e.getMessage());
			System.err.println("No se pudo establecer la propiedad 'discoverable' a 'GIAC'");
		}

		DeviceClass deviceClass = localDevice.getDeviceClass();
		int discoverable = localDevice.getDiscoverable();
		_discoverableString = null;
		switch(discoverable) {
		case DiscoveryAgent.GIAC:
			_discoverableString = "General / Unlimited Inquiry Access";
			break;
		case DiscoveryAgent.LIAC:
			_discoverableString = "Limited Dedicated Inquiry Access";
			break;
		case DiscoveryAgent.NOT_DISCOVERABLE:
			_discoverableString = "Not discoverable";
			break;
		default:
			_discoverableString = "Desconocido";
		}
		_device = new StringBuffer("0x");
		_device.append(Integer.toHexString(deviceClass.getMajorDeviceClass()));
		_device.append(", 0x");
		_device.append(Integer.toHexString(deviceClass.getMinorDeviceClass()));
		_device.append(", 0x");
		_device.append(Integer.toHexString(deviceClass.getServiceClasses()));
	}



	public void imprimirInfo(){
		System.out.println("---- LocalDevice-----");
		System.out.println("Dirección bluetooth: "+	_address);
		System.out.println("Nombre del dispositivo: "+_friendlyName);
		System.out.println("Modo de conectividad: "+ _discoverableString);
		System.out.println("Tipo de dispositivo: "+ _device.toString());
		System.out.println("----------------");
	}



	public void mostrarDispositivosRemotos(){
		LocalDevice localDevice = null;

		try {
			localDevice = LocalDevice.getLocalDevice();
		} catch(BluetoothStateException e) {
			System.out.println("Error al iniciar el sistema Bluetooth");
			return;
		}

		DiscoveryAgent discoveryAgent =	localDevice.getDiscoveryAgent();

		RemoteDevice[] preknown = discoveryAgent.retrieveDevices(
				DiscoveryAgent.PREKNOWN);
		if(preknown == null) {
			System.out.println("No hay dispositivos conocidos");
		} else {
			System.out.println("Dispositivos conocidos:");
			for(int i=0; i<preknown.length; i++) {
				String address = preknown[i].getBluetoothAddress();
				String friendlyName = null;

				try {
					preknown[i].getFriendlyName(false);
				} catch(IOException e) {
					System.err.println(e);
				}

				System.out.println(i+": "+friendlyName+"; "+address);
			}
		}
		
		RemoteDevice[] cached =	discoveryAgent.retrieveDevices(DiscoveryAgent.CACHED);
		
		if(cached == null) {
			System.out.println("No hay dispositivos encontrados en búsquedas previas");
		} else {
			System.out.println("Dispositivos encontrados en búsquedas previas:");
			for(int i=0; i<cached.length; i++) {
				String address = cached[i].getBluetoothAddress();
				String friendlyName = null;
				try {
					friendlyName=cached[i].getFriendlyName(false);
					
				} catch(IOException e) {
					System.err.println(e);
				}
				System.out.println((i+1)+": "+
						friendlyName+"; "+address);
			}
		}



	}



	private void comenzarbusqueda() {
		//dispositivos.clear();
		LocalDevice localDevice = null;
		try {
		localDevice = LocalDevice.getLocalDevice();
		localDevice.setDiscoverable(DiscoveryAgent.GIAC);
		} catch(Exception e) {
			e.printStackTrace();
			System.err.println("Error: No se puede hacer uso de Bluetooth");
		}

		
		
		System.out.println("Comenzamos la busqueda....");
		try {
			DiscoveryAgent discoveryAgent =	LocalDevice.getLocalDevice().getDiscoveryAgent();
			discoveryAgent.startInquiry(DiscoveryAgent.GIAC,this);

		} catch(BluetoothStateException e) {
			e.printStackTrace();
			System.err.println("Error: No se pudo comenzar la busqueda");
		}
		
        try {
            synchronized(lock){
                lock.wait();
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
		
		System.out.println("Fin de la busqueda:");
		
	}
	
	
	

	private void mostrarresultadosbusqueda() throws IOException {
        //imprimir todos los dispositivos
        int numdisp=dispositivos.size();
       
        if(numdisp <= 0){
            System.out.println("No se encontraron dispositivos.");
        }
        else{
            //Mostrar los dispositivos encontrados [ No. address (name) ]
            System.out.println("Dispositivos Bluetooth: ");
            for (int i = 0; i <numdisp; i++) {
                RemoteDevice remoteDevice=(RemoteDevice)dispositivos.elementAt(i);
                System.out.println((i+1)+". "+remoteDevice.getBluetoothAddress()+" ("+remoteDevice.getFriendlyName(true)+")");
            }
        }
		
	}


	@Override
	public void deviceDiscovered(RemoteDevice remoteDevice, DeviceClass deviceClass) {
		
		String address = remoteDevice.getBluetoothAddress();
		String friendlyName = null;
		try {
			friendlyName = remoteDevice.getFriendlyName(true);
		} catch(IOException e) { }
		String device = null;
		if(friendlyName == null) {
			device = address;
		} else {
			device = friendlyName + " ("+address+")";
		}
		System.out.println("Un dispositivo encontrado: "+device);
		dispositivos.addElement(remoteDevice);
		

	}

	@Override
	public void inquiryCompleted(int discType) {
        synchronized(lock){
            lock.notify();
        }
		switch(discType) {
		case DiscoveryListener.INQUIRY_COMPLETED:
			System.out.println("Busqueda concluida con normalidad");
			break;
		case DiscoveryListener.INQUIRY_TERMINATED:
			System.out.println("Busqueda cancelada");
			break;
		case DiscoveryListener.INQUIRY_ERROR:
			System.out.println("Busqueda finalizada debido a un error");
			break;
		}



	}

	@Override
	public void serviceSearchCompleted(int transID, int respCode) {
		// TODO Auto-generated method stub

	}

	@Override
	public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
		// TODO Auto-generated method stub

	}


}
