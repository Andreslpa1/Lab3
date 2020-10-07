package client;

import java.io.BufferedInputStream;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.codec.digest.DigestUtils;


import com.sun.xml.internal.ws.handler.ClientSOAPHandlerTube;

import sun.misc.BASE64Encoder;

public class TCPClient2 extends Thread {
	
	public long id;
	public Socket clientSocket;
	private PrintWriter cOutput;
	private BufferedReader cInput;
	private String filename;
	
	public static final int PORT = 3030;
	public static final String SERVER = "35.175.186.34";

	private static final String READY = "ready";

	public TCPClient2(Socket clientSocket, long id) {
		this.id = id;
		this.clientSocket = clientSocket;
		try {
			cOutput = new PrintWriter(clientSocket.getOutputStream(), true);
			cInput = new BufferedReader (new InputStreamReader(clientSocket.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void run() {
		System.out.println("Connecting to: " + SERVER + " on port " + PORT);
		System.out.println("Se ha conectado a: " + clientSocket.getRemoteSocketAddress());
		if (clientSocket.isConnected()) {
			try {
				String shaChecksumS = cInput.readLine();
				filename = cInput.readLine();
				System.out.println("cks servidor: " + shaChecksumS);
				getFile();
				//Create checksum for this file
				//File file = new File(DATA_PATH+ filename);
				//Use SHA-1 algorithm
				//MessageDigest shaDigest = MessageDigest.getInstance("SHA-256");
				//SHA-1 checksum 
				String shaChecksumC = DigestUtils.sha256Hex(new FileInputStream(filename));
				System.out.println("cks calculado: " + shaChecksumC);
				if (shaChecksumC.equals(shaChecksumS)) {
					cOutput.println("Fine");
					System.out.println("Fine");
				}else {
					System.out.println("No");
				}
				clientSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}

	}
	

	public void getFile() throws IOException{
		int bytesRead;
	    InputStream is = null;
	    int bufferSize=0;

	    try {
	        bufferSize=clientSocket.getReceiveBufferSize();
	        is=clientSocket.getInputStream();
	        DataInputStream clientData = new DataInputStream(is);
	        OutputStream output = new FileOutputStream(filename);
	        byte[] buffer = new byte[bufferSize];
	        cOutput.println(READY);
	        long length = clientData.readLong();
	        long a = 0;
	        int read;
	        while((read = clientData.read(buffer)) != -1){
	        	a+=read;
	            output.write(buffer, 0, read);
	            if (a>=length) {
					break;
				}
	        }
	        output.close();

	    } catch (IOException e) {
	        e.printStackTrace();
	    } finally {
		}
		
	}

	public static void main(String[] args) throws IOException{

		Socket socket = new Socket(SERVER, PORT);
		TCPClient2 client = new TCPClient2(socket,1);
		client.start();
	}


}
