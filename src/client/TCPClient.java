package client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class TCPClient {

	private static Socket clientSocket;
	private static PrintWriter out;
	private static BufferedReader in;
	public final static int PUERTO=3030;

	public static void main(String[] args) throws IOException
	{
		String serverName = "localhost";
		int port = PUERTO;
		try {
			System.out.println("Conectando a: " + serverName + " on port " + port);
			Socket client = new Socket(serverName, port);

			System.out.println("Se ha conectado a: " + client.getRemoteSocketAddress());
			OutputStream outToServer = client.getOutputStream();
			DataOutputStream out = new DataOutputStream(outToServer);

			out.writeUTF("Hola desde: " + client.getLocalSocketAddress());
			InputStream inFromServer = client.getInputStream();
			DataInputStream in = new DataInputStream(inFromServer);

			System.out.println("Server says " + in.readUTF());
			int bytesRead;
		    InputStream is;
		    int bufferSize=0;

		    try {
		        bufferSize=client.getReceiveBufferSize();
		        is=client.getInputStream();
		        DataInputStream clientData = new DataInputStream(is);
		        String fileName = clientData.readUTF();
		        System.out.println(fileName);
		        OutputStream output = new FileOutputStream("");
		        byte[] buffer = new byte[bufferSize];
		        int read;
		        while((read = clientData.read(buffer)) != -1){
		            output.write(buffer, 0, read);
		        }

		    } catch (IOException e) {
		        e.printStackTrace();
		    }
			client.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}
