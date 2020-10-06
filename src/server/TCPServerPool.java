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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.codec.digest.DigestUtils;


import jdk.nashorn.internal.runtime.regexp.joni.Regex;
import sun.misc.BASE64Encoder;

public class TCPServerPool implements Runnable{

	private static Logger LOGGER;
	private Socket socketClient;
	private PrintWriter sOutput;
	private BufferedReader sInput;
	private File myFile;
	private long startTime;
	private long endTime;


	private static AtomicInteger nClients =  new AtomicInteger(0);
	private static Object monitor = new Object();
	private static int nConcurrentClients;
	private static String filename;
	private static String shaChecksum;

	private static final int PORT = 3030;

	private static final String READY = "ready";
	private static final String FINE = "Fine";

	public TCPServerPool(Socket socketClient) {
		this.socketClient = socketClient;
		try {
			sOutput = new PrintWriter(socketClient.getOutputStream(), true);
			sInput = new BufferedReader (new InputStreamReader(socketClient.getInputStream()));
			LOGGER.log(Level.INFO, "Connected to: " + socketClient.getRemoteSocketAddress());
		} catch (IOException e) {
			e.printStackTrace();
			LOGGER.log(Level.WARNING, "Cannot connect to: " + socketClient.getRemoteSocketAddress());
		}
	}

	public static void list() {
		File directory = new File("./");
		String[] pathnames = directory.list();
		StringBuilder m = new StringBuilder();
		for (String pathname : pathnames) {
			m.append(pathname + "\n");
		}
		System.out.println(m.toString());
	}

	public void sendFile(String filename) {
		myFile = new File(filename);
		byte[] mybytearray = new byte[8192];
		try {
			FileInputStream fis = new FileInputStream(myFile);
			BufferedInputStream bis = new BufferedInputStream(fis);
			DataInputStream dis = new DataInputStream(bis);
			OutputStream os = socketClient.getOutputStream();
			DataOutputStream dos = new DataOutputStream(os);
			//dos.writeUTF(myFile.getName());     
			dos.writeLong(myFile.length());
			int read;
			while((read = dis.read(mybytearray)) != -1){
				dos.write(mybytearray, 0, read);
			}
			dos.flush();
			//dos.close();
			fis.close();
			bis.close();
			dis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			System.out.println(shaChecksum);
			sOutput.println(shaChecksum);
			sOutput.println(filename);
			String inputLine = sInput.readLine();

			if (inputLine.startsWith(READY)) {
				nClients.addAndGet(1);
				LOGGER.log(Level.INFO, "CLIENT: " + socketClient.getRemoteSocketAddress() + ". Client is ready for file tranfer.");
			}
			if (nClients.get()>=nConcurrentClients) {
				for (int i = 0; i < nConcurrentClients; i++) {
					synchronized (monitor) {
						monitor.notify();
					}
				}
			}else {
				synchronized (monitor) {
					monitor.wait();
				}
			}
			startTime = System.currentTimeMillis();
			LOGGER.log(Level.INFO, "CLIENT: " + socketClient.getRemoteSocketAddress() + ". Starting to send file " + filename);
			sendFile(filename);
			LOGGER.log(Level.INFO, "CLIENT: " + socketClient.getRemoteSocketAddress() + ". Finished to send file " + filename);
			inputLine = sInput.readLine();
			if (inputLine!= null && inputLine.equals(FINE)) {
				endTime = System.currentTimeMillis();
				long transferTime = (endTime-startTime)/1000;
				LOGGER.log(Level.INFO, "CLIENT: " + socketClient.getRemoteSocketAddress() + ". Succesfull file transfer" + "\nFilename: " + filename + "\nFile size: " + myFile.length() + " bytes" + "\nTransfer time: " + transferTime + "seconds");
			}else {
				LOGGER.log(Level.WARNING, "CLIENT: " + socketClient.getRemoteSocketAddress() + ". Unsuccesfull file transfer.");
			}


		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}


	}


	public static void main(String[] args) throws IOException {

		Date date = new Date();
		Timestamp ts = new Timestamp(date.getTime());
		LOGGER = Logger.getLogger("Client " + ts);

		String pathLog = "Client" + ts.toString().replaceAll(":", " ") + ".log" ;
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

		System.out.println("Lista de archivos disponibles:");
		list();
		System.out.println("Escriba el nombre del archivo:");
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		filename = stdIn.readLine();
		shaChecksum = DigestUtils.sha256Hex(new FileInputStream(filename));

		System.out.println("Escriba el número de clientes en simultaneo:");
		nConcurrentClients = Integer.parseInt(stdIn.readLine());
		System.out.println("Servidor activo.");
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
