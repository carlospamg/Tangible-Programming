package com.example.bluetoothutility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

import android.bluetooth.*;
import android.content.Intent;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class BluetoothUtility extends Activity{

	private static final String TAG = "Bluetooth";

	private BluetoothAdapter btAdapter = null;
	private BluetoothSocket btSocket = null;
	private OutputStream outStream = null;
	private InputStream inStream = null;

	private boolean inStreamMonitorStop = false;
	private Thread inStreamMonitor = null;

	private TextView log, message;

	// SPP UUID service
	private static final UUID LOCAL_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	// Mac-Address of blue tooth module
	// NOTE: Change to match remote device address, tested on both windows and rPi
	private static String address = "00:15:83:0C:BF:EB";		// rPi module address
	//private static String address = "00:1F:81:00:08:30";		// Windows module address

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bluetooth_utility);

		btAdapter = BluetoothAdapter.getDefaultAdapter();

		log = (TextView)findViewById(R.id.log);

		Button connectBtn = (Button)findViewById(R.id.connect_btn);
		connectBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Connect();
			}
		});

		
		Button sendFEBtn = (Button)findViewById(R.id.send_ack_received_btn);
		sendFEBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				byte data = (byte)0xfe; 
				sendData(data);
			}
		});
		
		Button sendFDBtn = (Button)findViewById(R.id.send_ack_completed_btn);
		sendFDBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				byte data = (byte)0xfd; 
				sendData(data);
			}
		});
	}

	public void Connect(){
		BluetoothDevice device = btAdapter.getRemoteDevice(address);

		try {
			btSocket = createBluetoothSocket(device);
		} catch (IOException e1) {
			logMessage("In onResume() and socket create failed: " + e1.getMessage() + ".");
		}

		// Discovery is resource intensive.  Make sure it isn't going on
		// when you attempt to connect and pass your message.
		btAdapter.cancelDiscovery();

		// Establish the connection.  This will block until it connects.
		logMessage("Connecting...");
		try {
			btSocket.connect();
			logMessage("Connection ok...");
		} catch (IOException e) {
			try {
				btSocket.close();
			} catch (IOException e2) {
				logMessage("In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
			}
		}

		// Create a data stream so we can talk to server.
		logMessage("Creating Socket...");

		try {
			outStream = btSocket.getOutputStream();
			inStream = btSocket.getInputStream();
			logMessage("Socket ok...");
		} catch (IOException e) {
			logMessage("In onResume() and output/input stream creation failed:" + e.getMessage() + ".");
		}

		// Start inStream thread monitor
		inStreamMonitor = new Thread(new Runnable() {

			@Override
			public void run() {
				BufferedReader r = new BufferedReader(new InputStreamReader(inStream));
				while(!inStreamMonitorStop){
					try {
						if(r.ready()){
							byte data = (byte) inStream.read();
							logMessage(">> Receiving: " + String.format("%02X", data));
						}
					} catch (IOException e) {
						e.printStackTrace();
					}

				}

				try {
					r.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		logMessage("Starting inStream Monitor...");
		inStreamMonitor.start();
		logMessage("inStream Monitor ok...");

		logMessage("READY\n\n");
	}

	public void logMessage(final String msg){
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				log.append(msg + "\n");
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		inStreamMonitorStop = true;

		if (outStream != null) {
			try {
				outStream.flush();
			} catch (IOException e) {
				logMessage("In onPause() and failed to flush output stream: " + e.getMessage() + ".");
			}
		}

		if (inStream != null) {
			try {
				inStream.close();
			} catch (IOException e) {
				logMessage("In onPause() and failed to close input stream: " + e.getMessage() + ".");
			}
		}

		try{
			if(btSocket != null)
				btSocket.close();
		} catch (IOException e2) {
			logMessage("In onPause() and failed to close socket." + e2.getMessage() + ".");
		}
	}

	private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
		if(Build.VERSION.SDK_INT >= 10){
			try {
				final Method  m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[] { UUID.class });
				return (BluetoothSocket) m.invoke(device, LOCAL_UUID);
			} catch (Exception e) {
				Log.e(TAG, "Could not create Insecure RFComm Connection",e);
			}
		}
		return  device.createRfcommSocketToServiceRecord(LOCAL_UUID);
	}

	private void sendData(int message){
		
		logMessage("...Send data: " + String.format("%02X", (byte)message) + "...");

		try {
			outStream.write((byte)message);
		} catch (IOException e) {
			logMessage("In onResume() and an exception occurred during write: " + e.getMessage());      
		}
	}
}
