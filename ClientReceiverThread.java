import java.net.*;
import java.io.*;
import java.util.*;
import java.net.*;
import java.util.concurrent.*;

public class ClientReceiverThread extends Thread {


	public Socket socket = null;
	public ObjectInputStream inStream;
	public ObjectOutputStream outStream;
	private LinkedBlockingQueue<MazewarPacket> inQueue;
        public boolean isRunning = true;
        private static int ricounter = 0;
    
	public ClientReceiverThread (Socket socket, LinkedBlockingQueue<MazewarPacket> incomingQueue) 
	{
		super("ClientReceiverThread");
		this.socket = socket;
		this.inQueue = incomingQueue;
		try {
			this.outStream = new ObjectOutputStream(socket.getOutputStream()); 
			this.inStream = new ObjectInputStream(socket.getInputStream());
		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}


	public ClientReceiverThread (Socket socket, LinkedBlockingQueue<MazewarPacket> incomingQueue, boolean isserver) 
	{
		super("ClientReceiverThread");
		if(isserver) {
		this.socket = socket;
		this.inQueue = incomingQueue;
		try {
			this.inStream = new ObjectInputStream(socket.getInputStream());
		} catch(IOException ex) {
			ex.printStackTrace();
		}
		}
	}
	
	public void run() {
		
	    MazewarPacket packetFromServer;
	    try {
		while (isRunning && ((packetFromServer = (MazewarPacket) inStream.readObject()) != null)) {
			if(packetFromServer.type != MazewarPacket.RING_TOKEN && packetFromServer.type != MazewarPacket.MW_TICK)
		    System.out.println("receives a packet of type "+packetFromServer.type + " from " +packetFromServer.cID);
		    // check acks and unpauses; ACK and RING_UNPAUSE packets are not queued
		    //synchronized(this) {
		    	if(packetFromServer.type == MazewarPacket.RING_UNPAUSE)
		    		LocalClient.ispaused = false;

		    	else if(packetFromServer.type == MazewarPacket.ACK)
			    	LocalClient.ACKnum++;

			else if(packetFromServer.type == MazewarPacket.RING_PAUSE && !packetFromServer.cID.equals(ClientExecutionThread.localID)) {
				LocalClient.p2psockets.put(packetFromServer.cID, this.socket);
				inQueue.put(packetFromServer);			
				LocalClient.p2pthreads.put(packetFromServer.cID, this);
				System.out.println("p2pthreads keys: "+LocalClient.p2pthreads.keySet());
			}

			else if(packetFromServer.type == MazewarPacket.RING_INFO){
				ricounter++;
				inQueue.put(packetFromServer);
				if(ricounter == LocalClient.p2psockets.size()) {
					MazewarPacket np = new MazewarPacket();
					np.type = MazewarPacket.MCAST_REQ;
					inQueue.put(np);
				}
			}
		    	else { 
				inQueue.put(packetFromServer);
				//System.out.println("queued packet. Queue size = "+LocalClient.inQueue.size());
                    	}
		}
		//Close the streams and socket
		inStream.close();
		outStream.close();
		socket.close();
	    } catch (IOException e) {
		 System.err.println("ERROR: Couldn't get I/O for the connection.");
		 e.printStackTrace();
		// System.exit(1);
		return;
	    } catch (ClassNotFoundException e) {
		 System.err.println("ERROR: Cannot find the class.");
		 System.exit(1);
	    } catch(InterruptedException ex) {
		System.out.println("interrupted");
		ex.printStackTrace();
		//terminate();
		// return;
	    }
	}
	
	public void send(MazewarPacket pkt) {
		try {		
			this.outStream.writeObject(pkt);
		}
		catch(IOException ex) {
			ex.printStackTrace();
		}
	}

        public void terminate(){
	    isRunning = false;
	    Thread.currentThread().interrupt();
	    try {
	    
	    inStream.close();
	    outStream.close();
	    socket.close();
	    } catch (IOException e) {
		 System.err.println("ERROR: Couldn't get I/O for the connection.");
		 e.printStackTrace();
		 System.exit(1);
	    }
	}
}
