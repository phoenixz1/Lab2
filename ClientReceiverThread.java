import java.net.*;
import java.io.*;
import java.util.*;
import java.net.*;

public class ClientReceiverThread extends Thread {
	

	public Socket socket = null;
	public ObjectInputStream inStream;
	private Queue<MazewarPacket> inQueue;

	
	
	
	public ClientReceiverThread (Socket socket, Queue<MazewarPacket> incomingQueue, ObjectInputStream inStream) 
	{
		super("ClientReceiverThread");
		this.socket = socket;
		this.inQueue = incomingQueue;
		this.inStream = inStream;
		
	}
	
	


	public void run() {
		
	    MazewarPacket packetFromServer;
	    try {
		while ((packetFromServer = (MazewarPacket) inStream.readObject()) != null) {
		    // check acks and unpauses


		    synchronized(this) {
                    	inQueue.add(packetFromServer);
                    }
		}
		// connection ended
                // inStream.close();
                // socket.close();
	    } catch (IOException e) {
		 System.err.println("ERROR: Couldn't get I/O for the connection.");
		 System.exit(1);
	    } catch (ClassNotFoundException e) {
		 System.err.println("ERROR: Cannot find the class.");
		 System.exit(1);
	    }
	}

}
