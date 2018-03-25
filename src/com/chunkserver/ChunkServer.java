package com.chunkserver;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.channels.IllegalBlockingModeException;

import com.interfaces.ChunkServerInterface;

/**
 * implementation of interfaces at the chunkserver side
 * 
 * @author Shahram Ghandeharizadeh
 *
 */

public class ChunkServer implements ChunkServerInterface {
	// /Users/teodoralupu/Desktop/TinyFS-2
	final static String filePath = "/Users/teodoralupu/Desktop/TinyFS-2/Chunks"; // or C:\\newfile.txt
	public static long counter;
	ServerSocket serverSocket;
	Socket clientSocket;
	DataInputStream is;
    PrintStream os;

	/**
	 * Initialize the chunk server
	 */
	public ChunkServer() {
//		System.out.println(
//				"Constructor of ChunkServer is invoked:  Part 1 of TinyFS must implement the body of this method.");
//		System.out.println("It does nothing for now.\n");
		
		Socket clientSocket = null;
		// Try to open a server socket on port 9898
		try {
			serverSocket = new ServerSocket(9898);
			System.out.println("initialized server socket");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			clientSocket = serverSocket.accept();
			System.out.println("accepted server/client connection");
			is = new DataInputStream(clientSocket.getInputStream());
			os = new PrintStream(clientSocket.getOutputStream());
			
			/* TODO: move this code where you want to be receiving data */
			String line;
			while (true) {
				// note: readLine() is deprecated
				line = is.readUTF();
				os.println(line); 
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			System.out.println("SecurityException: if a security manager exists and its checkAccept method doesn't allow the operation");
			e.printStackTrace();
		} catch (IllegalBlockingModeException e) {
			System.out.println("IllegalBlockingModeException: if this socket has an associated channel, the channel is in non-blocking mode, and there is no connection ready to be accepted");
			e.printStackTrace();
		}
		
		/* initialize the counter that keeps track of the chunkhandle 
		   counter should start from the number of files that already exist */
		File directory = new File(filePath);
		
		if(directory.list() != null && directory.list().length > 0)
		{
			counter = directory.list().length;
		}
		else
		{
			counter = 0;
		}
	}

	/**
	 * Each chunk corresponds to a file. Return the chunk handle of the last chunk
	 * in the file.
	 */
	public String initializeChunk() {
//		System.out.println("createChunk invoked:  Part 1 of TinyFS must implement the body of this method.");
//		System.out.println("Returns null for now.\n");
		
		// increment the counter
		counter++;
		String counterStr = Long.toString(counter);
		
		// return the counter
		return counterStr;
	}

	/**
	 * Write the byte array to the chunk at the specified offset The byte array size
	 * should be no greater than 4KB
	 */
	public boolean putChunk(String ChunkHandle, byte[] payload, int offset) {
//		System.out.println("putChunk invoked:  Part 1 of TinyFS must implement the body of this method.");
//		System.out.println("Returns false for now.\n");
		
		String counterStr = Long.toString(counter);
		// initialize a binary file with the counter as the filename
		String fPath = filePath + "/" + counterStr + ".bin";
		File myFile = new File(fPath);
		
		if (payload.length > ChunkServer.ChunkSize) {
//			System.out.println("The chunk size is greater than 4KB!");
			return false;
		}
		
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(myFile);
			fos.write(payload, offset, payload.length);
			fos.close();
			
			return true;
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}

	/**
	 * read the chunk at the specific offset
	 */
	public byte[] getChunk(String ChunkHandle, int offset, int NumberOfBytes) {
//		System.out.println("readChunk invoked:  Part 1 of TinyFS must implement the body of this method.");
//		System.out.println("Returns null for now.\n");
		
		// initialize a binary file with the counter as the filename
		String fPath = filePath + "/" + ChunkHandle + ".bin";
		File myFile = new File(fPath);

		FileInputStream fin = null;
		byte fileContent[] = null;
		try {
			// create FileInputStream object
			fin = new FileInputStream(myFile);

			fileContent = new byte[(int)myFile.length()];
			
			// Reads up to certain bytes of data from this input stream into an array of bytes.
			fin.read(fileContent);
		}
		catch (FileNotFoundException e) {
			System.out.println("File not found" + e);
		}
		catch (IOException ioe) {
			System.out.println("Exception while reading file " + ioe);
		}
		
		return fileContent;
	}
}
