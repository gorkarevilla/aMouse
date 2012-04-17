package com.gorkarevilla.amouse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;


/**
 * Hilo que se usa para la transmision de los datos
 * al servidor, solamente sera de Output, ya que no tendra que escuchar nada 
 * de la comunicación.
 */
class HiloConectado extends Thread {
	// Debugging
	private static final String TAG = "HiloConectado";
	private static final boolean D = true;
	
    private final BluetoothSocket _socket;
    private final Handler _manejador;
    private ClienteBluetooth _cliente;
    
    private final OutputStream _outStream;

    public HiloConectado(BluetoothSocket socket, Handler manejador, ClienteBluetooth cliente) {
        Log.d(TAG, "Crear HiloConectado");
        _cliente = cliente;
        _manejador = manejador;
        _socket = socket;
        
        OutputStream tmpOut = null;

        // Get the BluetoothSocket input and output streams
        try {
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "FALLO: Socket es escritura NO cargado!", e);
        }

        _outStream = tmpOut;
    }

    public void run() {
        Log.i(TAG, "Empieza el HiloConectado");
//        byte[] buffer = new byte[1024];
//       int bytes;

        // Keep listening to the InputStream while connected
 //       while (true) {
//            try {
                // Read from the InputStream
//                bytes = mmInStream.read(buffer);

                // Send the obtained bytes to the UI Activity
//                mHandler.obtainMessage(BluetoothChat.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
 //           } catch (IOException e) {
  //              Log.e(TAG, "disconnected", e);
//                connectionLost();
//                break;
//            }
//        }
    }

    /**
     * Write to the connected OutStream.
     * @param buffer  The bytes to write
     */
    public void write(byte[] buffer) {
        try {
            _outStream.write(buffer);

            // Share the sent message back to the UI Activity
 //           mHandler.obtainMessage(BluetoothChat.MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
        } catch (IOException e) {
            Log.e(TAG, "Exception during write", e);
        }
    }

    public void cancel() {
        try {
            _socket.close();
        } catch (IOException e) {
            Log.e(TAG, "close() of connect socket failed", e);
        }
    }
}
