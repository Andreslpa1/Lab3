package server;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jdk.nashorn.internal.runtime.regexp.joni.Regex;

public class TCPServerPool implements Runnable{

	private Logger LOGGER;
	private Socket socketClient;
	private PrintWriter sOutput;
	private BufferedReader sInput;

	private static final String DATA_PATH = "./data/server/";
	private static final String LOG_PATH = "./log/";
	private static final int PORT = 3030;

	private static final String LIST = "list";
	private static final String SEND_FILE = "get";
	private static final String VALID_COMMAND_REGEX = "(" + LIST + "|" + SEND_FILE + ")" + "(\s)?" + "(.*\\..*)?";

	private static final String FILE_SUCCESFULLY_SENT = "File succesfully sent";

	private static final String INVALID_COMMAND = "Invalid command ";
	private static final String FILE_NOT_FOUND = "File not found ";

	public TCPServerPool(Socket socketClient) {
		this.socketClient = socketClient;
		this.LOGGER = Logger.getLogger("Client " + socketClient.getRemoteSocketAddress().toString());

		String pathLog = LOG_PATH + "Client.log" ;
		try {      
			FileHandler fhandler = new FileHandler(pathLog);  
			LOGGER.addHandler(fhandler);
			SimpleFormatter formatter = new SimpleFormatter();  
			fhandler.setFormatter(formatter);  

		} catch (SecurityException e){  
			e.printStackTrace();  
		} catch (IOException e){  
			e.printStackTrace();  
		}      

		try {
			sOutput = new PrintWriter(socketClient.getOutputStream(), true);
			sInput = new BufferedReader (new InputStreamReader(socketClient.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void list() {
		File directory = new File(DATA_PATH);
		String[] pathnames = directory.list();
		StringBuilder m = new StringBuilder();
		for (String pathname : pathnames) {
			m.append(pathname + "\n");
		}
		sOutput.println(m.toString());
	}

	public void sendFile(String filename) {
		File myFile = new File(DATA_PATH + filename);
		byte[] mybytearray = new byte[8192];
		try {
			FileInputStream fis = new FileInputStream(myFile);
			BufferedInputStream bis = new BufferedInputStream(fis);
			DataInputStream dis = new DataInputStream(bis);
			OutputStream os = socketClient.getOutputStream();
			DataOutputStream dos = new DataOutputStream(os);
			dos.writeUTF(myFile.getName());     
			dos.writeLong(mybytearray.length);
			int read;
			while((read = dis.read(mybytearray)) != -1){
				dos.write(mybytearray, 0, read);
			}
			dos.flush();
			dos.close();
			fis.close();
			bis.close();
			dis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		sOutput.println(FILE_SUCCESFULLY_SENT);
	}

	@Override
	public void run() {
		Pattern regex = Pattern.compile(VALID_COMMAND_REGEX);
		try {
			String inputLine = sInput.readLine();
			System.out.println(inputLine);
			Matcher m = regex.matcher(inputLine);
			if (m.matches()) {
				String command = m.group(1);
				System.out.println("1" + command);
				switch (command) {
				case LIST:
					list();
					break;
				case SEND_FILE:
					System.out.println("Entré");
					String filename = m.group(3);
					sendFile(filename);
					break;
				default:
					break;
				}

			}
			else {
				LOGGER.log(Level.INFO, INVALID_COMMAND + inputLine);
				sOutput.println(INVALID_COMMAND + inputLine);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}


	}

	public static void main(String[] args) {
		int nThreads = 25;
		ExecutorService pool = Executors.newFixedThreadPool(nThreads);
		ServerSocket ss = null;
		try {
			ss = new ServerSocket(PORT);
			while (true)
			{
				Socket client = ss.accept();
				pool.execute(new TCPServerPool(client));
			}
		}catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
