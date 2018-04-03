package com.chunkserver;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.io.Reader;
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
    DataOutputStream os;
    PrintStream ps;
  

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
			serverSocket = new ServerSocket(9893);
			System.out.println("initialized server socket");
			clientSocket = serverSocket.accept();
			System.out.println("accepted server/client connection");
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
		
		try {
			is = new DataInputStream(clientSocket.getInputStream());
			os = new DataOutputStream(clientSocket.getOutputStream());
			ps = new PrintStream(clientSocket.getOutputStream());
			
			/* TODO: move this code where you want to be receiving data */
			while (true)
			{
				char line = 'a';
				if (is.available() != 0)
				{
					line = is.readChar();
				}
				
				if (line == 'i')
				{
					System.out.println("server read request to initialize chunk");
					newInitializeChunk();
				}
				else if (line == 'p')
				{
					System.out.println("server read request to put chunk");
					String ChunkHandle = is.readUTF();
					int payloadSize = is.readInt();
					byte[] buffer = new byte[payloadSize];
					// read data into buffer
					is.read(buffer);
					int offset = is.readInt();
					newPutChunk(ChunkHandle, buffer, offset);
				}
				else if (line == 'g')
				{
					System.out.println("server read request to get chunk");
					String ChunkHandle = is.readUTF();
					int offset = is.readInt();
					int NumberOfBytes = is.readInt();
					newGetChunk(ChunkHandle, offset, NumberOfBytes);
				}
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
	}

	/**
	 * Each chunk corresponds to a file. Return the chunk handle of the last chunk
	 * in the file.
	 */
	public String initializeChunk() {
//		System.out.println("createChunk invoked:  Part 1 of TinyFS must implement the body of this method.");
//		System.out.println("Returns null for now.\n");
		
		// send the handle through the output stream
		// increment the counter
		counter++;
		String chunkHandle = Long.toString(counter);
		
		// return the counter
		return chunkHandle;
	}
	
	public void newInitializeChunk() {
//		System.out.println("createChunk invoked:  Part 1 of TinyFS must implement the body of this method.");
//		System.out.println("Returns null for now.\n");
		
		// send the handle through the output stream
		try {
			// increment the counter
			counter++;
			String chunkHandle = Long.toString(counter);
			// write chunk handle to output stream
			os.writeUTF(chunkHandle);
			// flush the stream
			os.flush();
			System.out.println("Sent chunkHandle: " + chunkHandle);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	
	public void newPutChunk(String ChunkHandle, byte[] payload, int offset) {
//		System.out.println("putChunk invoked:  Part 1 of TinyFS must implement the body of this method.");
//		System.out.println("Returns false for now.\n");
		
		String counterStr = Long.toString(counter);
		// initialize a binary file with the counter as the filename
		String fPath = filePath + "/" + counterStr + ".bin";
//		File myFile = new File(fPath);
		
		try {
			if (payload.length > ChunkServer.ChunkSize) {
				os.writeBoolean(false);
				System.out.println("server wrote false");
				os.flush();
			}
			
//			FileOutputStream fos;
//			fos = new FileOutputStream(myFile);
//			fos.write(payload, offset, payload.length);
//			fos.close();
			
			RandomAccessFile raf = new RandomAccessFile(fPath, "rw");
			raf.seek(offset);
			raf.write(payload, 0, payload.length);
			raf.close();

			os.writeBoolean(true);
			System.out.println("server wrote true");
			os.flush();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
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
	
	public void newGetChunk(String ChunkHandle, int offset, int NumberOfBytes) {
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
			int numBytes = fin.read(fileContent);
			System.out.println("numBytes in newGetChunk: " + numBytes);
			/* this is the part where you write the number of bytes in the file in the first byte and then write the array */
			// first write the number of bytes
			os.writeInt(numBytes);
			os.flush();
			// then write the bytes array
			os.write(fileContent);
			os.flush();
		}
		catch (FileNotFoundException e) {
			System.out.println("File not found" + e);
		}
		catch (IOException ioe) {
			System.out.println("Exception while reading file " + ioe);
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ChunkServer chunkServer = new ChunkServer();
		
		/* clean up */
//		try {
//			chunkServer.os.close();
//			chunkServer.is.close();
//			chunkServer.clientSocket.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
}
