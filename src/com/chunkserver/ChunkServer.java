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
		
		Socket clientSocket = null;
		// Try to open a server socket on port 9898
		try {
			serverSocket = new ServerSocket(9893);
			System.out.println("Initialized server socket");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		while (true)
		{
			try {
				clientSocket = serverSocket.accept();
				System.out.println("Accepted server socket");
				is = new DataInputStream(clientSocket.getInputStream());
				os = new DataOutputStream(clientSocket.getOutputStream());
				ps = new PrintStream(clientSocket.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
			/* TODO: move this code where you want to be receiving data */
			while (!clientSocket.isClosed())
			{
				try {
					char line = 'a';
					line = is.readChar();
					
					if (line == 'i')
					{
						newInitializeChunk();
					}
					else if (line == 'p')
					{
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
						String ChunkHandle = is.readUTF();
						int offset = is.readInt();
						int NumberOfBytes = is.readInt();
						newGetChunk(ChunkHandle, offset, NumberOfBytes);
					}
				} catch (IOException e) {
					break;
				}
			}
		}
	}

	/**
	 * Each chunk corresponds to a file. Return the chunk handle of the last chunk
	 * in the file.
	 */
	public String initializeChunk() {
		// send the handle through the output stream
		// increment the counter
		counter++;
		String chunkHandle = Long.toString(counter);
		
		// return the counter
		return chunkHandle;
	}
	
	public void newInitializeChunk() {
		// send the handle through the output stream
		try {
			// increment the counter
			counter++;
			String chunkHandle = Long.toString(counter);
			// write chunk handle to output stream
			os.writeUTF(chunkHandle);
			// flush the stream
			os.flush();
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
		String counterStr = Long.toString(counter);
		// initialize a binary file with the counter as the filename
		String fPath = filePath + "/" + counterStr + ".bin";
		File myFile = new File(fPath);
		
		if (payload.length > ChunkServer.ChunkSize) {
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
		String counterStr = Long.toString(counter);
		// initialize a binary file with the counter as the filename
		String fPath = filePath + "/" + counterStr + ".bin";
		
		try {
			if (payload.length > ChunkServer.ChunkSize) {
				os.writeBoolean(false);
				os.flush();
			}

			RandomAccessFile raf = new RandomAccessFile(fPath, "rw");
			raf.seek(offset);
			raf.write(payload, 0, payload.length);
			raf.close();

			os.writeBoolean(true);
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
		try {
			// initialize a binary file with the counter as the filename
			String fPath = filePath + "/" + ChunkHandle + ".bin";
			boolean exists = (new File(fPath)).exists();
			if (exists == false)
			{
				os.write(null);
			}
			
			byte[] data = new byte[NumberOfBytes];
			RandomAccessFile raf = new RandomAccessFile(fPath, "rw");
			raf.seek(offset);
			int numBytes = raf.read(data, 0, NumberOfBytes);
			raf.close();

			/* this is the part where you write the number of bytes in the file in the first byte and then write the array */
			// first write the number of bytes
			os.writeInt(numBytes);
			os.flush();
			// then write the bytes array
			os.write(data);
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
	}
}
