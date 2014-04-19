package communication;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

import ast.Command;

public class BluetoothTransmitter implements ITransmitter{

	public final UUID uuid = new UUID("1101", false);

	public final String name = "Window Echo Server";

	public final String url = "btspp://localhost:" + uuid + ";name=" + name + ";authenticate=false;encrypt=false;";

	LocalDevice local = null;

	StreamConnectionNotifier server = null;

	StreamConnection conn = null;

	BufferedReader reader = null;

	DataOutputStream writer = null;

	Thread readerMonitor = null;
	
	boolean readerMonitorRunning = true;
	
	boolean acknowledgementReceived = false;			// received after sending command
	
	boolean acknowledgementComplete = false;			// received once command is complete
	
	@Override
	public void setup() {
		try{

			System.out.println("Setting device to be discoverable...");
			local = LocalDevice.getLocalDevice();
			local.setDiscoverable(DiscoveryAgent.GIAC);

			System.out.println("Start advertising service...");

			server = (StreamConnectionNotifier)Connector.open(url);

			System.out.println("Waiting for incoming connection...");

			conn = server.acceptAndOpen();

			System.out.println("Client Connected... ");

			reader = new BufferedReader(new InputStreamReader(conn.openInputStream()));

			writer = conn.openDataOutputStream();
			
			// Start reader thread monitor
			readerMonitor = new Thread(new Runnable() {

				@Override
				public void run() {
					while(readerMonitorRunning){
						try {
							if(reader.ready()){
								final String line = reader.readLine();
								System.out.println(">> Receiving: " + line);
								byte[] data = line.getBytes();
								if(data[0] == -2){	// java doesn't support unsigned bytes, so -2 :: 0xFE
									acknowledgementReceived = true;
									System.out.println("RECEIVED ACK RECEIVED!!!!");
								}else if(data[0] == -1){
									acknowledgementComplete = true;
									System.out.println("RECEIVED ACK COMPLETE!!!!");
								}
							}
						} catch (IOException e) {
							e.printStackTrace();
						}

					}

					try {
						reader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
			
			readerMonitor.start();

		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void sendCommand(Command cmd) {
		sendCommand(cmd.outputId);
	}
	
	@Override
	public void sendCommand(int cmd) {
		try {
			System.out.println("[BT] Sending Command: " + cmd);
			writer.write(cmd);
			writer.write('\n');
			
			while(!acknowledgementReceived){
				Thread.sleep(50);
			}
			
			// reset acknowledgements
			acknowledgementReceived = false;
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void shutdown() {
		if(writer != null){
			try {
				writer.write('e');
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			readerMonitor.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
