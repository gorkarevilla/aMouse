package com.gorkarevilla.amouse;

import com.homelinux.ramuh76.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;


public class aMouse extends Activity  {
	// Debugging
	private static final String TAG = "aMouse";
	private static final boolean D = true;
	

	private Vista _vista;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		if(D) Log.i(TAG, "+++ ON CREATE +++");

		_vista= new Vista(this);

		setContentView(_vista);

		//Cargamos los sensores a la vista
		_vista._sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		_vista._accelerometro = _vista._sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		_vista._accelerometroLineal = _vista._sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

		
	}



	@Override
	public void onStart() {
		super.onStart();
		if(D) Log.i(TAG, "++ ON START ++");


	}


	@Override
	public void onResume() {
		super.onResume();
		if(D) Log.i(TAG, "+ ON RESUME +");
		
		//Activamos el listener de los sensores
		_vista._sensorManager.registerListener(_vista, _vista._accelerometro, SensorManager.SENSOR_DELAY_NORMAL);
		_vista._sensorManager.registerListener(_vista, _vista._accelerometroLineal, SensorManager.SENSOR_DELAY_NORMAL);

		
	}

	@Override
	public void onPause() {
		super.onPause();
		if(D) Log.i(TAG, "- ON PAUSE -");
		
		//Desactivamos el listener de los sensores
		_vista._sensorManager.unregisterListener(_vista);
	}

	@Override
	public void onStop() {
		super.onStop();
		if(D) Log.i(TAG, "-- ON STOP --");
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
		
		//Desactivamos el recibidor a broadcast
		this.unregisterReceiver(_vista._controlador._bluetooth._recibidor);
		
		if(D) Log.i(TAG, "--- ON DESTROY ---");
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK && requestCode == ClienteBluetooth.REQUEST_ENABLE_BT) {
			if(D) Log.i(TAG, "Bluetooth Activado!");
		}
		else if(resultCode == Activity.RESULT_CANCELED && requestCode == ClienteBluetooth.REQUEST_ENABLE_BT ){
			if(D) Log.i(TAG, "Bluetooth No Activado!");
		}
		else if(requestCode == ClienteBluetooth.REQUEST_DEVICE_FOUND);
			if(D) Log.i(TAG, "Dispositivo Encontrado!");
			

	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}



	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Manejador del menu
		switch (item.getItemId()) {
		case R.id.salirmenu:
			finish();
			return true;
		case R.id.ayudamenu:
			_vista.mostrarAyuda();
			return true;
		case R.id.opcionesmenu:
			_vista.mostrarOpciones();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
