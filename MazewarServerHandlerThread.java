import java.net.*;
import java.io.*;


/*
TODO: 1.Handle Fire Events
	  2.Handle client exit/"bye" (remove thread from 'clients', 
	  								everything else done on client side)
	  3.Might need a dedicated thread for broadcasts
*/


public class MazewarServerHandlerThread extends Thread {
	private Socket socket = null;

	public MazewarServerHandlerThread(Socket socket) {
		super("MazewarServerHandlerThread");
		this.socket = socket;
		System.out.println("Created new Thread to handle client");
	}
	
	// enqueue the request package and multicast top of the queue 
	
	public void broadcast() {
		//while(serverIncomingQueue.size() == 0) {;} //keep polling
		if(serverIncomingQueue.size() != 0) {
			Packet currPacket = serverIncomingQueue.remove();
			for (MazewarServerHandlerThread t : clients) {
        			t.send(currPacket);
    			}
		}
	}


	public void send(Packet packetFromQueue) {
		/* stream to write back to client */
		ObjectOutputStream toClient = new ObjectOutputStream(socket.getOutputStream());
		
		/* create a packet to send to client */
		MazewarPacket packetToClient = new MazewarPacket();
		packetToClient.type = MazewarPacket.MW_REPLY;

		if(packetFromClient.type == MazewarPacket.MW_REQUEST) {
			packetToClient.message = packetFromQueue.message;
			packetToClient.cID = packetFromQueue.cID;
			packetToClient.event = packetFromQueue.event;
			/* send reply back to client */
			toClient.writeObject(packetToClient);	
		}

	}


	public void run() {

		boolean gotByePacket = false;
		
		try {
			/* stream to read from client */
			ObjectInputStream fromClient = new ObjectInputStream(socket.getInputStream());
			MazewarPacket packetFromClient;

			while (( packetFromClient = (MazewarPacket) fromClient.readObject()) != null) {				
				
				/* process message */
				/* add to package global queue */
				if(packetFromClient.type == MazewarPacket.MW_REQUEST) {
					
					serverIncomingQueue.add(packetFromClient);
					System.out.println("From Client "+ packetFromClient.cID +" : "+ packetFromClient.message);
				
					/* send a broadcast */
					broadcast();
					
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
					serverIncomingQueue.add(packetToClient);
					broadcast();
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

		} catch (IOException e) {
			if(!gotByePacket)
				e.printStackTrace();
		} catch (ClassNotFoundException e) {
			if(!gotByePacket)
				e.printStackTrace();
		}
	}
}
