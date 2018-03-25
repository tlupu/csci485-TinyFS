package com.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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
	public static ChunkServer cs = new ChunkServer();
	Socket clientSocket;
	DataOutputStream os;
    DataInputStream is;
    String hostname = "Teodora’s MacBook Pro";
	
	/**
	 * Initialize the client
	 */
	public Client() {
		if (cs == null)
		{
			cs = new ChunkServer();
		}
		
		// Try to open a socket on port 25
		try {
			clientSocket = new Socket(hostname, 25);
			os = new DataOutputStream(clientSocket.getOutputStream());
            is = new DataInputStream(clientSocket.getInputStream());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			System.out.println("Couldn't find host: " + hostname);
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Could not get I/O for the connection to: " + hostname);
			e.printStackTrace();
		}
		
		/* TODO: move this code where you want to write some data to the socket opened on port 25*/
		if (clientSocket != null && os != null && is != null)
		{
			try {
				os.writeBytes("hello \n");
				
				
				/* keep on reading from/to the socket till we receive the "Ok" from client,
				 once we received that then we want to break. */
				String responseLine;
				while ((responseLine = is.readUTF()) != null)
				{
					System.out.println("Server: " + responseLine);
					if (responseLine.indexOf("Ok") != -1)
					{
						break;
					}
				}
				
				/* clean up */
				os.close();
				is.close();
				clientSocket.close();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
	}

	/**
	 * Create a chunk at the chunk server from the client side.
	 */
	public String initializeChunk() {
		return cs.initializeChunk();
	}

	/**
	 * Write a chunk at the chunk server from the client side.
	 */
	public boolean putChunk(String ChunkHandle, byte[] payload, int offset) {
		if (offset + payload.length > ChunkServer.ChunkSize) {
			System.out.println("The chunk write should be within the range of the file, invalide chunk write!");
			return false;
		}
		return cs.putChunk(ChunkHandle, payload, offset);
	}

	/**
	 * Read a chunk at the chunk server from the client side.
	 */
	public byte[] getChunk(String ChunkHandle, int offset, int NumberOfBytes) {
		if (NumberOfBytes + offset > ChunkServer.ChunkSize) {
			System.out.println("The chunk read should be within the range of the file, invalide chunk read!");
			return null;
		}
		return cs.getChunk(ChunkHandle, offset, NumberOfBytes);
	}

}
