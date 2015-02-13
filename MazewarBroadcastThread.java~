import java.net.*;
import java.io.*;
import java.util.*;


public class MazewarBroadcastThread extends Thread {
	
 	public int tID;	
	private Queue<MazewarPacket> inQueue;
	private Map<Integer, MazewarServerHandlerThread> clients;
	
	public MazewarBroadcastThread
			(int tID, Queue<MazewarPacket> incomingQueue, Map<Integer, MazewarServerHandlerThread> clients) 
	
	{
		super("MazewarServerHandlerThread");
		this.tID = tID;
		this.inQueue = incomingQueue;
		this.clients = clients;
		System.out.println("Created new BroadcastThread; tID = "+ tID);
	}
	
	

	// keep checking if queue is not empty (polling/timer interrupts)
	// deque packet
	// process packet (handle fire event here) <---
	// broadcast to clients
	public void run() {
		while(inQueue.size() == 0) ;
		while(inQueue.size() > 0) {
			System.out.println("Broadcasting packet");
			MazewarPacket top = inQueue.remove();
			broadcast(top);
		}
	}


	// multicast a packet
	public void broadcast(MazewarPacket currPacket) {

		Set<Integer> s = clients.keySet();
		for(int tid : s) {
			MazewarServerHandlerThread t = clients.get(tid);
       			t.send(currPacket);
 		}
	}


};
