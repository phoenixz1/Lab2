import java.net.*;
import java.io.*;
import java.util.*;


public class MazewarServerHandlerThread extends Thread {
	
 	public int tID;	
	public Socket socket = null;
	public ObjectOutputStream toClient;
	private Queue<MazewarPacket> inQueue;
	private Map<Integer, MazewarServerHandlerThread> clients;

	
	public MazewarServerHandlerThread
			(int tID, Socket socket, Queue<MazewarPacket> incomingQueue, Map<Integer, MazewarServerHandlerThread> clients) 
	{
		super("MazewarServerHandlerThread");
		this.tID = tID;
		this.socket = socket;
		this.inQueue = incomingQueue;
		this.clients = clients;
		System.out.println("Created new Thread to handle client; tID = "+ tID);
	}
	
	


	public void run() {

		boolean gotByePacket = false;
		try {
			/* stream to read from client */

			/* stream to write back to client */
			toClient = new ObjectOutputStream(socket.getOutputStream());	
			ObjectInputStream fromClient = new ObjectInputStream(socket.getInputStream());		
			MazewarPacket packetFromClient;
			MazewarPacket packetToClient = new MazewarPacket();

			while (( packetFromClient = (MazewarPacket) fromClient.readObject()) != null) {				
				
				/* process message */
				/* add to package global queue */
				if(packetFromClient.type != MazewarPacket.MW_NULL && packetFromClient.type != MazewarPacket.MW_BYE) 
				{
					System.out.println("Enqueueuing packet of type "+packetFromClient.type);
					inQueue.add(packetFromClient);
					System.out.println("Enqueued");
					/* wait for next packet */
					continue;
				}
				
				/* Sending an MW_NULL || MW_BYE means quit */
				if (packetFromClient.type == MazewarPacket.MW_NULL || packetFromClient.type == MazewarPacket.MW_BYE) {
					gotByePacket = true;
					packetToClient.type = MazewarPacket.MW_BYE;
					packetToClient.cID = packetFromClient.cID;
					inQueue.add(packetToClient);
					break;
				}
				
				/* if code comes here, there is an error in the packet */
				System.err.println("ERROR: Unknown MW_* packet!!");
				System.exit(-1);
			}
			
			/* cleanup when client exits */
			fromClient.close();
			toClient.close();
			socket.close();
			clients.remove(tID);

		} catch (IOException e) {
			if(!gotByePacket)
				e.printStackTrace();
		} catch (ClassNotFoundException e) {
			if(!gotByePacket)
				e.printStackTrace();
		}
	}


	public void send(MazewarPacket packetFromQueue) {
	   try {

		MazewarPacket packetToClient = new MazewarPacket();		

		if(packetFromQueue.type == MazewarPacket.MW_REQUEST) {	
			packetToClient.cID = packetFromQueue.cID;
			packetToClient.event = packetFromQueue.event;
			packetToClient.type = MazewarPacket.MW_REPLY;
		}
		else if(packetFromQueue.type == MazewarPacket.MW_JOIN) {
			packetToClient.cID = packetFromQueue.cID;
			packetToClient.type = MazewarPacket.MW_JOIN;
		}
		else {
			packetToClient.cID = packetFromQueue.cID;
			packetToClient.type = packetFromQueue.type;
		}
	
		/* send reply back to client */
		System.out.println("Thread "+tID+" Sending packet to client "+packetFromQueue.cID);
		toClient.writeObject(packetToClient);
		System.out.println("Sent.");
	    }
	    catch (IOException e) {
			e.printStackTrace();
	    }
	}
}
