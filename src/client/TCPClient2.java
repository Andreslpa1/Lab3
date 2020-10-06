package client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

import com.sun.xml.internal.ws.handler.ClientSOAPHandlerTube;

public class TCPClient2 extends Thread {

	public Socket clientSocket;
	private PrintWriter cOutput;
	private BufferedReader cInput;

	private static final String DATA_PATH = "./data/client/";
	
	public static final int PORT = 3030;
	public static final String SERVER = "localhost";

	private static final String LIST = "list";
	private static final String GET_FILE = "get ";

	public TCPClient2(Socket clientSocket) {
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
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		while (clientSocket.isConnected()) {
			printMenu();
			try {
				int option = Integer.parseInt(stdIn.readLine());
				switch (option) {
				case 1:
					cOutput.println(LIST);
					String message =  cInput.readLine();
					System.out.println("Avaliable files at server are: ");
					System.out.println(message);
					System.out.println();
					break;
				case 2:
					System.out.println("Enter the filename:");
					String filename = stdIn.readLine();
					getFile(filename);
					break;
				case 3:
					System.out.println("Fin.");
					System.exit(-1);
					break;
				default:
					break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public void printMenu() {
		System.out.println("Choosee one option:");
		System.out.println("1. List files");
		System.out.println("2. Download file");
		System.out.println("3. Exit");
		System.out.println();
	}

	public void getFile(String filename) throws IOException{
		int bytesRead;
	    InputStream is = null;
	    int bufferSize=0;

	    try {
	        bufferSize=clientSocket.getReceiveBufferSize();
	        is=clientSocket.getInputStream();
	        DataInputStream clientData = new DataInputStream(is);
	        cOutput.println(GET_FILE + filename);
	        System.out.println(GET_FILE + filename);
	        OutputStream output = new FileOutputStream(DATA_PATH + filename);
	        byte[] buffer = new byte[bufferSize];
	        int read;
	        while((read = clientData.read(buffer)) != -1){
	            output.write(buffer, 0, read);
	        }
	        output.close();

	    } catch (IOException e) {
	        e.printStackTrace();
	    } finally {
			is.close();
		}
		
	}

	public static void main(String[] args) throws IOException{

		Socket socket = new Socket(SERVER, PORT);
		TCPClient2 client = new TCPClient2(socket);
		client.start();

	}


}
