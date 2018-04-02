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
	DataOutputStream os;
    DataInputStream is;
    String hostname = "localhost";
	
	/**
	 * Initialize the client
	 */
	public Client() {
//		if (cs == null)
//		{
//			cs = new ChunkServer();
//			System.out.println("after new chunkserver");
//		}
		
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
		
		/* TODO: move this code where you want to write some data to the socket*/
//		if (clientSocket != null && os != null && is != null)
//		{
//			System.out.println("entered the writing bit");
//			
//			try {
//				/* keep on reading from/to the socket till we receive the "Ok" from client,
//				 once we received that then we want to break. */
//				String responseLine;
//				BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
//				while ((responseLine = in.readLine()) != null)
//				{
//					System.out.println("Server: " + responseLine);
//					if (responseLine.indexOf("ok") != -1)
//					{
//						break;
//					}
//				}
//				
//				/* clean up */
//				os.close();
//				is.close();
//				clientSocket.close();
//				
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} 
//		}
	}
	

	/**
	 * Create a chunk at the chunk server from the client side.
	 */
	public String initializeChunk() {
		
		// first send a request to the server that you want to initialize a chunk
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(os));
			if (clientSocket != null && os != null && is != null) {
				System.out.println("client is ready in initializeChunk");
				out.write("i");
				System.out.println("client requested server to initialize chunk");
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// read the response from the server
		try {
			String responseLine;
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			// keep reading from the input stream until you get the chunkHandle
			while ((responseLine = in.readLine()) != null)
			{
				// this should capture the chunk handle
				System.out.println("client read something from server in intializeChunk: " + responseLine);
				return responseLine;
			}
			// close input stream
			is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			// read the response from the server
			boolean response;
			while ( (response = is.readBoolean()) )
			{
				return response;
			}
			is.close();
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
			boolean keepReading = true;
			int numChunks = 0;
			// keep reading from the input stream until you get the chunkHandle
			while (keepReading)
			{
				numChunks = is.readInt();
				if (numChunks > 0)
				{
					keepReading = false;
				}
			}
			
			keepReading = true;
			byte fileContent[] = null;
			while (keepReading)
			{
				is.read(fileContent, offset, numChunks);
				if (fileContent != null && fileContent.length == numChunks)
				{
					return fileContent;
				}
			}
			
			// close input stream
			is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// return cs.getChunk(ChunkHandle, offset, NumberOfBytes);
		return null;
	}

}
