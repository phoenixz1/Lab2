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
			ObjectInputStream fromClient = new ObjectInputStream(socket.getInputStream());
			/* stream to write back to client */
			toClient = new ObjectOutputStream(socket.getOutputStream());			
			MazewarPacket packetFromClient;
			MazewarPacket packetToClient = new MazewarPacket();

			while (( packetFromClient = (MazewarPacket) fromClient.readObject()) != null) {				
				
				/* process message */
				/* add to package global queue */
				if(packetFromClient.type == MazewarPacket.MW_REQUEST) {
					inQueue.add(packetFromClient);
					System.out.println("From Client "+ packetFromClient.cID +" : "+ packetFromClient.message);
					/* wait for next packet */
					continue;
				}
				
				/* Sending an MW_NULL || MW_BYE means quit */
				if (packetFromClient.type == MazewarPacket.MW_NULL || packetFromClient.type == MazewarPacket.MW_BYE) {
					gotByePacket = true;
					packetToClient = new MazewarPacket();
					packetToClient.type = MazewarPacket.MW_BYE;
					packetToClient.message = "Bye!";
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
		/* stream to write back to client */
		//ObjectOutputStream toClient = new ObjectOutputStream(socket.getOutputStream());
		
		/* create a packet to send to client */
		MazewarPacket packetToClient = new MazewarPacket();		

		if(packetFromQueue.type == MazewarPacket.MW_REQUEST) {	
			packetToClient.message = packetFromQueue.message;
			packetToClient.cID = packetFromQueue.cID;
			packetToClient.event = packetFromQueue.event;
			packetToClient.type = MazewarPacket.MW_REPLY;
		}
		else if(packetFromQueue.type == MazewarPacket.MW_JOIN) {
			packetToClient.message = packetFromQueue.message;
			packetToClient.cID = packetFromQueue.cID;
			packetToClient.type = MazewarPacket.MW_JOIN;
			}		
	
		/* send reply back to client */
		toClient.writeObject(packetToClient);
	    }
	    catch (IOException e) {
			e.printStackTrace();
	    }
	}
}
