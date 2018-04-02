package com.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import com.chunkserver.ChunkServer;
import com.interfaces.ClientInterface;

/**
 * implementation of interfaces at the client side
 * 
 * @author Shahram Ghandeharizadeh
 *
 */
public class Client implements ClientInterface {
	public static ChunkServer cs = null;
	Socket clientSocket;
	public DataOutputStream os;
    public DataInputStream is;
    String hostname = "localhost";
	
	/**
	 * Initialize the client
	 */
	public Client() {
		// Try to open a socket on port 9898
		try {
			clientSocket = new Socket(hostname, 9893);
			System.out.println("initialized client");
			
			// open input and output streams
			os = new DataOutputStream(clientSocket.getOutputStream());
            is = new DataInputStream(clientSocket.getInputStream());
            System.out.println("initialized os and is");
            
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			System.out.println("Couldn't find host: " + hostname);
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Could not get I/O for the connection to: " + hostname);
			e.printStackTrace();
		}

	}
	

	/**
	 * Create a chunk at the chunk server from the client side.
	 */
	public String initializeChunk() {
		
		// first send a request to the server that you want to initialize a chunk
		try {
			if (clientSocket != null && os != null && is != null) {
				os.writeChar('i');
				System.out.println("client requested server to initialize chunk");
			}
			
			String responseLine = is.readUTF();
			// this should capture the chunk handle
			System.out.println("client read something from server in intializeChunk: " + responseLine);
			
			return responseLine;	
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		
		
		return "";
//		return cs.initializeChunk();
	}

	/**
	 * Write a chunk at the chunk server from the client side.
	 */
	public boolean putChunk(String ChunkHandle, byte[] payload, int offset) {
		if (offset + payload.length > ChunkServer.ChunkSize) {
			System.out.println("The chunk write should be within the range of the file, invalid chunk write!");
			return false;
		}

		try {
			if (clientSocket != null && os != null && is != null) {
				os.writeChar('p');
				// send all the data to the server
				os.writeUTF(ChunkHandle);
				os.writeInt(payload.length);
				os.write(payload);
				os.writeInt(offset);
				System.out.println("client requested server to putChunk");
			}
			
			// read the response from the server
			boolean response = is.readBoolean();
			System.out.println("client read something from server in putChunk: " + response);
			return response;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
//		return cs.putChunk(ChunkHandle, payload, offset);
	}

	/**
	 * Read a chunk at the chunk server from the client side.
	 */
	public byte[] getChunk(String ChunkHandle, int offset, int NumberOfBytes) {
		if (NumberOfBytes + offset > ChunkServer.ChunkSize) {
			System.out.println("The chunk read should be within the range of the file, invalide chunk read!");
			return null;
		}
		
		/* in a while loop, get the number of chunks from the server, then read the chunks */
		try {
			if (clientSocket != null && os != null && is != null) 
			{
				os.writeChar('g');
				// send all the data to the server
				os.writeUTF(ChunkHandle);
				os.writeInt(offset);
				os.writeInt(NumberOfBytes);
				System.out.println("client requested server to putChunk");
			}
				
			int numBytes = is.readInt();
			System.out.println("numBytes read from the server: " + numBytes);
			byte[] buffer = new byte[numBytes];
			int bytesRead = is.read(buffer);
			System.out.println("number of bytes actually read from the server: " + bytesRead);
			return buffer;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// return cs.getChunk(ChunkHandle, offset, NumberOfBytes);
		return null;
	}

}
