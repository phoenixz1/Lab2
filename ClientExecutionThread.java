import java.net.*;
import java.io.*;
import java.util.*;
import java.net.*;

public class ClientExecutionThread extends Thread {
	
	public int tID;
	private Queue<MazewarPacket> inQueue;
	private Map<String, Client> players;
	
	
	
	public ClientExecutionThread (Queue<MazewarPacket> incomingQueue, Map<String, Client> clients) 
	{
		super("ClientExecutionThread");
		this.inQueue = incomingQueue;
		this.players = clients;
		
	}
	
	


	public void run() {
		
		// poll inQueue for packets, read packet, executePacket()
		
    }
	
	public void executePacket(MazewarPacket pkt) {
	
		String cID = pkt.cID;
		Client c = players.value(cID);
		
		if(pkt.type == MW_REPLY) {
			KeyEvent e = pkt.event;
			// c.execute_command(e);
		}
		else {
			// fill this for all remaining packet types
		
		}
	}

}
