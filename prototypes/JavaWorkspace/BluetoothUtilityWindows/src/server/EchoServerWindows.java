package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import javax.bluetooth.*;
import javax.microedition.io.*;

public class EchoServerWindows {
	
	public final UUID uuid = new UUID("1101", false);
	
	public final String name = "Window Echo Server";
	
	public final String url = "btspp://localhost:" + uuid + ";name=" + name + ";authenticate=false;encrypt=false;";
	
	LocalDevice local = null;
	
	StreamConnectionNotifier server = null;
	
	StreamConnection conn = null;
	
	BufferedReader reader = null;
	
	DataOutputStream writer = null;
	
	boolean isRunning = true;
	
	public EchoServerWindows(){
		try{
			
			System.out.println("Setting device to be discoverable...");
			local = LocalDevice.getLocalDevice();
			local.setDiscoverable(DiscoveryAgent.GIAC);
			
			System.out.println("Start advertising service...");
			
			server = (StreamConnectionNotifier)Connector.open(url);
			
			System.out.println("Waiting for incoming connection...");
			
			conn = server.acceptAndOpen();
			
			System.out.println("Client Connected...");

			reader = new BufferedReader(new InputStreamReader(conn.openDataInputStream()));
			
			writer = conn.openDataOutputStream();
			
			String cmd = "";
			
			while(isRunning){
				try {
					cmd = reader.readLine();
					System.out.println("Received: " + cmd);
					sendData(cmd);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			isRunning = false;

		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(reader != null){
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void sendData(String data){
		try {
			System.out.println("Sending: " + data);
			data += "\n";
			writer.write(data.getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public static void main(String[] args){
		new EchoServerWindows();
	}
}
