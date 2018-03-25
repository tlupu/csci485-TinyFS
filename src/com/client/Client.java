package com.client;

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
	
	/**
	 * Initialize the client
	 */
	public Client() {
		if (cs == null)
		{
			cs = new ChunkServer();
		}
		
		try {
			clientSocket = new Socket("Teodora’s MacBook Pro", 7777);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
