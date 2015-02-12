import java.net.*;
import java.io.*;
import java.util.*;
import java.net.*;

public class ClientReceiverThread extends Thread {
	

	public Socket socket = null;
	public ObjectInputStream inStream;
	private Queue<MazewarPacket> inQueue;

	
	
	
	public ClientReceiverThread (Socket socket, Queue<MazewarPacket> incomingQueue, Map<Integer, ObjectInputStream inStream) 
	{
		super("ClientReceiverThread");
		this.socket = socket;
		this.inQueue = incomingQueue;
		this.inStream = inStream;
		
	}
	
	


	public void run() {
		
		MazewarPacket packetFromServer;
		while ((packetFromServer = (EchoPacket) inStream.readObject()) != null) {
                    inQueue.add(packetFromServer);
                }
		// connection ended
        inStream.close();
        socket.close();
	}

}
