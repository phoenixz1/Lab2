import java.net.*;
import java.io.*;
import java.util.*;
import java.net.*;

public class ClientReceiverThread extends Thread {
	
	public boolean ispaused;
	public int ACKnum;

	public Socket socket = null;
	public ObjectInputStream inStream;
	private Queue<MazewarPacket> inQueue;

	public ClientReceiverThread (Socket socket, Queue<MazewarPacket> incomingQueue, ObjectInputStream inStream, boolean ispaused, int ACKnum) 
	{
		super("ClientReceiverThread");
		this.socket = socket;
		this.inQueue = incomingQueue;
		this.inStream = inStream;
		this.ispaused = ispaused;
		this.ACKnum = ACKnum;
	}
	
	public void run() {
		
	    MazewarPacket packetFromServer;
	    try {
		while ((packetFromServer = (MazewarPacket) inStream.readObject()) != null) {
		    System.out.println("receives a packet of type "+packetFromServer.type);
		    // check acks and unpauses; ACK and RING_UNPAUSE packets are not queued
		    //synchronized(this) {
		    	if(packetFromServer.type == MazewarPacket.RING_UNPAUSE)
		    		ispaused = false;
		    	else if(packetFromServer.type == MazewarPacket.ACK)
			    	ACKnum++;
		    	else { 
				inQueue.add(packetFromServer);
				System.out.println("queued packet. Queue size = "+LocalClient.inQueue.size());
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
