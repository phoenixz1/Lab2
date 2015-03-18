import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;

public class ClientListenerThread extends Thread {
	
	public ServerSocket listenSocket = null;
	private Queue<MazewarPacket> inQueue;
	
	public ClientListenerThread (ServerSocket locSocket, Queue<MazewarPacket> incomingQueue){
		super("ClientListenerThread");
		listenSocket = locSocket;
		inQueue = incomingQueue;
	}
	
	public void run(){
		
		Socket connSocket = null;
		ObjectInputStream connInStream = null;
		ObjectOutputStream connOutStream = null;
		
		// Listen for new clients trying to join the game session
		// If a client wants to connect, create a new ClientReceiverThread for that client
		while (!Thread.interrupted()) {
			try {
				connSocket = listenSocket.accept();
				connInStream = new ObjectInputStream(connSocket.getInputStream());
				connOutStream = new ObjectOutputStream(connSocket.getOutputStream());
			} catch (IOException e) {
				System.err.println("ERROR: Couldn't get I/O for the naming server connection.");
				System.exit(1);
			}
			
			new ClientReceiverThread(connSocket, inQueue, connInStream, LocalClient.ispaused, LocalClient.ACKnum).start();
		}

		connInStream.close();
		connSocket.close();
		listenSocket.close();
	}
}
