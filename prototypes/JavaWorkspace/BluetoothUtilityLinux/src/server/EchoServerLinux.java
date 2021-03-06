package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.bluetooth.*;
import javax.microedition.io.*;

public class EchoServerLinux {
	
	public final UUID uuid = new UUID("1101", false);
	
	public final String name = "Window Echo Server";
	
	public final String url = "btspp://localhost:" + uuid + ";name=" + name + ";authenticate=false;encrypt=false;";
	
	LocalDevice local = null;
	
	StreamConnectionNotifier server = null;
	
	StreamConnection conn = null;
	
	BufferedReader reader = null;
	
	public EchoServerLinux(){
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
			String cmd = "";
			
			while(!(cmd = reader.readLine()).equals("EXIT\n")){
				System.out.println("Received: " + cmd);
			}

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
	
	public static void main(String[] args){
		new EchoServerLinux();
	}
	
}
