package server;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;


public class TCPServer extends Thread{
	
	private ServerSocket serverSocket;
	
	

	public TCPServer(int port) throws IOException {
		serverSocket = new ServerSocket(port);
		serverSocket.setSoTimeout(10000);
	}

	public void run() {
		while(true) {
			try {
				System.out.println("Esperando al cliente en el puerto " + 
						serverSocket.getLocalPort() + "...");
				Socket server = serverSocket.accept();

				System.out.println("Conectado a: " + server.getRemoteSocketAddress());
				DataInputStream in = new DataInputStream(server.getInputStream());

				System.out.println(in.readUTF());
				DataOutputStream out = new DataOutputStream(server.getOutputStream());
				out.writeUTF("Se ha conectado a: " + server.getLocalSocketAddress());
				System.out.println("Ingrese el archivo que desea mandar al cliente");
				BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
				String name = reader.readLine();
				System.out.println(name); 
				out.writeUTF(name);
				final File myFile= new File(""); //sdcard/DCIM.JPG
				byte[] mybytearray = new byte[8192];
				FileInputStream fis = new FileInputStream(myFile);  
				BufferedInputStream bis = new BufferedInputStream(fis);
				DataInputStream dis = new DataInputStream(bis);
				OutputStream os;
				try {
					os = server.getOutputStream();
					DataOutputStream dos = new DataOutputStream(os);
					dos.writeUTF(myFile.getName());     
					dos.writeLong(mybytearray.length);
					int read;
					while((read = dis.read(mybytearray)) != -1){
						dos.write(mybytearray, 0, read);
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
				server.close();

			} catch (SocketTimeoutException s) {
				System.out.println("Socket timed out!");
				break;
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
		}
	}

	public static void main(String [] args) {
		int port = 3030;
		try {
			Thread t = new TCPServer(port);
			t.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}
