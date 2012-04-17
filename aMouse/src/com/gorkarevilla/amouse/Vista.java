package com.gorkarevilla.amouse;

import java.util.List;

import com.homelinux.ramuh76.R;

import android.app.Dialog;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter.LengthFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class Vista extends LinearLayout implements SensorEventListener{
	// Debugging
	private static final String TAG = "Vista";
	private static final boolean D = true;

	//Mensajes de cambios en la conexion
	public static final int MENSAJE_NUEVO_DISPOSITIVO_ENCONTRADO = 0;
	public static final int MENSAJE_CAMBIO_ESTADO = 1;
	public static final int MENSAJE_TOAST = 2;
	


	//Controlador de la vista
	public Controlador _controlador;

	//Contexto
	private Context _context;

	//Widgets
	private Button _izquierdo,_derecho,_mover,_botonBuscar;
	private SeekBar _barrasenSibilidad;
	private ListView _listaDispCono,_listaDispDescono;

	//Dialogs
	private Dialog _dialogOpciones;
	private Dialog _dialogAyuda;

	//Sensores
	public SensorManager _sensorManager;
	public Sensor _accelerometro;
	public Sensor _accelerometroLineal;

	//Dispositivos
	public ArrayAdapter<String> _dispositivosConocidosAA;
	public ArrayAdapter<String> _dispositivosDesconocidosAA;


	public Vista(Context context) {

		super(context);

		if(D)Log.i(TAG, "Arranca la Vista");

		_context=context;

		_controlador= new Controlador(_context,_manejador);

		this.cargarVentanaPrincipal();

		this.cargarOpciones();

		this.cargarAyuda();

	}



	private void cargarVentanaPrincipal() {



		//Cargamos el Layout
		LayoutInflater inflater = LayoutInflater.from(_context);
		RelativeLayout myRoot = new RelativeLayout(_context);
		View v = inflater.inflate(R.layout.main, myRoot);
		this.addView(v);

		//Cargamos los Botones
		_izquierdo=(Button)findViewById(R.id.botonizquierdo);
		_derecho=(Button)findViewById(R.id.botonderecho);
		_mover=(Button)findViewById(R.id.botonmover);




		//A cada boton le asignamos una funcion, controlamos un click y el mantener pulsado.
		_izquierdo.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				clickIzquierdo();
			}
		});
		_izquierdo.setOnLongClickListener(new OnLongClickListener() {
			public boolean onLongClick(View v) {
				clickLargoIzquierdo();
				return true;
			}
		});
		_derecho.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				clickDerecho();
			}
		});
		_derecho.setOnLongClickListener(new OnLongClickListener() {
			public boolean onLongClick(View v) {
				clickLargoDerecho();
				return true;
			}
		});
		_mover.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mover();
			}
		});

	}

	/**
	 * Encargado de cargar el layout de opciones
	 */
	private void cargarOpciones() {

		if(D)Log.i(TAG, "Cargar Layout Opciones");

		_dialogOpciones = new Dialog(_context);
		_dialogOpciones.setContentView(R.layout.configuracion);
		_dialogOpciones.setTitle(R.string.opciones);
		_dialogOpciones.setCancelable(true);

		_dispositivosConocidosAA= new ArrayAdapter<String>(_context, R.layout.nombredispositivo);
		_dispositivosDesconocidosAA = new ArrayAdapter<String>(_context, R.layout.nombredispositivo);

		_barrasenSibilidad=(SeekBar)_dialogOpciones.findViewById(R.id.barrasensibilidad);

		_barrasenSibilidad.setProgress(_controlador.getSensibilidad());

		_barrasenSibilidad.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			public void onStartTrackingTouch(SeekBar seekBar) {
				empezarMoviendoBarra(seekBar);
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
				terminarMoviendoBarra(seekBar);
			}

			public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
				cambiandoProgreso(seekBar, progress, fromUser);
			}
		});

		_botonBuscar = (Button)_dialogOpciones.findViewById(R.id.BotonBuscar);
		_botonBuscar.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				clickBuscar();
			}
		});

		_listaDispCono = (ListView)_dialogOpciones.findViewById(R.id.dispConocidos);
		_listaDispCono.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> av, View v, int arg2,
					long arg3) {
				
	            // Los ultimos 17 caracteres son la MAC del dispositivo
	            String info = ((TextView) v).getText().toString();
				
				clickDispConocidos(info);

			}
		});


		_listaDispDescono = (ListView)_dialogOpciones.findViewById(R.id.dispDesconocidos);
		_listaDispDescono.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> av, View v, int arg2,
					long arg3) {
				
	            // Los ultimos 17 caracteres son la MAC del dispositivo
	            String info = ((TextView) v).getText().toString();
				
				clickDispDesconocidos(info);

			}
		});





	}

	/**
	 * Encargado de monstrar las opciones
	 */
	public void mostrarOpciones() {
		if(D)Log.i(TAG, "Mostrar Layout Opciones");

		cargarDispositivosConocidos();
		cargarDispositivosDesconocidos();

		_dialogOpciones.show();
	}

	/**
	 * Encargado de cargar el layout de ayuda
	 */
	private void cargarAyuda() {

		if(D)Log.i(TAG, "Cargar Layout Ayuda");

		_dialogAyuda = new Dialog(_context);
		_dialogAyuda.setContentView(R.layout.ayuda);
		_dialogAyuda.setTitle(R.string.ayuda);
		_dialogAyuda.setCancelable(true);


	}

	/**
	 * Encargado de mostrar la Ayuda
	 */
	public void mostrarAyuda() {
		if(D)Log.i(TAG, "Mostrar Layout Ayuda");
		_dialogAyuda.show();

	}



	/**
	 * Carga los dispositivos conocidos en la lista
	 */
	private void cargarDispositivosConocidos(){
		if(D)Log.i(TAG, "Cargamos los dispositivos Conocidos");

		List<String> dispositivosConocidos=_controlador.getDispositivosConocidos();

		if(dispositivosConocidos.size()==0){
			Toast.makeText(_context, "No se conocen dispositivos", 300);
		}
		else {

			_dispositivosConocidosAA.clear();

			for(int i=0;i<dispositivosConocidos.size();++i) {
				String nombre = dispositivosConocidos.get(i);

				if(D)Log.i(TAG, "Cargamos el dispositivo Nº "+i+" Nombre: "+nombre);

				_dispositivosConocidosAA.add(nombre);

			}
			_listaDispCono.setAdapter(_dispositivosConocidosAA);

		}


	}

	/**
	 * Carga los dispositivos conocidos en la lista
	 */
	public void cargarDispositivosDesconocidos(){
		if(D)Log.i(TAG, "Cargamos los dispositivos Nuevos");

		List<String> dispositivosDesconocidos=_controlador.getDispositivosDesconocidos();

		if(dispositivosDesconocidos.size()==0){
			Toast.makeText(_context, "No se encuentran nuevos dispositivos", 300);
		}
		else {

			_dispositivosDesconocidosAA.clear();

			for(int i=0;i<dispositivosDesconocidos.size();++i) {
				String nombre = dispositivosDesconocidos.get(i);

				if(D)Log.i(TAG, "Cargamos el dispositivo Nº "+i+" Nombre: "+nombre);

				_dispositivosDesconocidosAA.add(nombre);


			}
			_listaDispDescono.setAdapter(_dispositivosDesconocidosAA);

		}


	}


	// Handler que controla los eventos del Bluetooth
	private final Handler _manejador = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if(D)Log.i(TAG, "Nuevo Mensaje!");
			switch (msg.what) {
			
			case MENSAJE_NUEVO_DISPOSITIVO_ENCONTRADO:
				if(D) Log.i(TAG, "Nuevo Dispositivo: ");
				cargarDispositivosDesconocidos();
				break;
			
			case MENSAJE_CAMBIO_ESTADO:
				if(D) Log.i(TAG, "Cambia el estado: " + msg.arg1);
				
				switch (msg.arg1) {
				
				case ClienteBluetooth.CONECTADO:
					setConectado();
					break;
					
				case ClienteBluetooth.DESCONECTADO:
					setDesconectado();
					break;
					
				case ClienteBluetooth.CONECTANDO:
					setConectando();
					break;
				
				}
				break;
				
			case MENSAJE_TOAST:
				
				if(D) Log.i(TAG, "TOAST: " + msg.arg1);
				
				switch (msg.arg1) {
				
				case ClienteBluetooth.ERROR_CONEXION:
					Toast.makeText(_context, R.string.errorConexion, 10).show();
					break;
					
				
				}
				break;

				
				
			}
		}
	};







	/*
	 * Cambios en la vista principal
	 */
	private void setConectado() {
		final TextView texto=(TextView) findViewById(R.id.estado);
		texto.setTextColor(getResources().getColor(R.color.verde));
		texto.setText(R.string.conectado);
	}
	
	private void setDesconectado() {
		final TextView texto=(TextView) findViewById(R.id.estado);
		texto.setTextColor(getResources().getColor(R.color.rojo));
		texto.setText(R.string.desconectado);

	}
	
	private void setConectando() {
		final TextView texto=(TextView) findViewById(R.id.estado);
		texto.setTextColor(getResources().getColor(R.color.amarillo));
		texto.setText(R.string.conectando);

	}
	


	/*
	 * EVENTOS DE LA BARRA
	 */

	private void empezarMoviendoBarra(SeekBar seekBar) {
		// No hay que hacer nada
		//if(D)Log.i(TAG, "Empieza a Mover La Barra");
	}

	private void terminarMoviendoBarra(SeekBar seekBar) {
		// No hay que hacer nada
		//if(D)Log.i(TAG, "Termina de Mover La Barra");

	}

	private void cambiandoProgreso(SeekBar seekBar, int progress,boolean fromUser) {
		// Cambiar el valor de la sensibilidad
		if(D)Log.i(TAG, "Cambia el Progreso de La Barra");
		if(D)Log.i(TAG, "Progreso: "+progress);
		_controlador.setSensibilidad(progress);
	}


	/*
	 * EVENTOS DE LOS BOTONES
	 */

	private void clickIzquierdo() {
		if(D)Log.i(TAG, "Pulsar Boton Izquierdo");
		_controlador.clickIzquierdo();
	}

	private void clickLargoIzquierdo() {
		if(D)Log.i(TAG, "Mantener Pulsado Boton Izquierdo");
		_controlador.clickLargoIzquierdo();
	}

	private void clickDerecho() {
		if(D)Log.i(TAG, "Pulsar Boton Derecho");
		_controlador.clickDerecho();
	}

	private void clickLargoDerecho() {
		if(D)Log.i(TAG, "Mantener Pulsado Boton Derecho");
		_controlador.clickLargoDerecho();
	}

	private void mover() {
		if(D)Log.i(TAG, "Pulsar Boton Mover");

		if(((ToggleButton)findViewById(R.id.botonmover)).isChecked()){
			if(D)Log.i(TAG, "Mover: Activado!");
			_controlador.setMovimientosHabilitados(true);
		}
		else{
			if(D)Log.i(TAG, "Mover: Desactivado!");
			_controlador.setMovimientosHabilitados(false);
		}
	}

	private void clickBuscar() {
		if(D)Log.i(TAG, "Pulsar Boton Buscar");
		_controlador.buscarDispositivosDesconocidos();
	}

	private void clickDispConocidos(String disp) {
		if(D)Log.i(TAG, "Pulsar Dispositivos Conocidos");
		
        // Los ultimos 17 caracteres son la MAC del dispositivo
        String mac = disp.substring(disp.length() - 17);
		
		_controlador.linkarDispConocido(mac);
	}

	private void clickDispDesconocidos(String disp) {
		
        // Los ultimos 17 caracteres son la MAC del dispositivo
        String mac = disp.substring(disp.length() - 17);
		
		if(D)Log.i(TAG, "Pulsar Dispositivos Desconocidos");
		_controlador.linkarDispDesconocido(mac);
	}



	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		if(_controlador.getMoviminetosHabilitados()) {
			//if(D)Log.i(TAG, "Se a cambiado la Precision de la Aceleracion!");
		}


	}



	@Override
	public void onSensorChanged(SensorEvent event) {
		if(_controlador.getMoviminetosHabilitados()) {
			_controlador.cambioEnSensor(event);
		}


	}




}
