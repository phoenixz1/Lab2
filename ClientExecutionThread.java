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
		MazewarPacket head = inQueue.remove();
		
		if(head != null){
			executePacket(head);
		}
    }
	
	public void executePacket(MazewarPacket pkt) {
	
		String cID = pkt.cID;
		Client c = players.get(cID);
		
		if(pkt.type == MW_REPLY) {
			KeyEvent e = pkt.event;
			// c.execute_command(e);
			
                        // If the user pressed Q, invoke the cleanup code and quit. 
                        if((e.getKeyChar() == 'q') || (e.getKeyChar() == 'Q')) {
                                if(c.getType() = 100){ // Local client
                                        Mazewar.quit();
                                }
                                else { // Remote client
                                        // TODO: Remove the remote client from the hash map and maze
                                }
                        // Up-arrow moves forward.
                        } else if(e.getKeyCode() == KeyEvent.VK_UP) {
                                c.forward();
                        // Down-arrow moves backward.
                        } else if(e.getKeyCode() == KeyEvent.VK_DOWN) {
                                c.backup();
                        // Left-arrow turns left.
                        } else if(e.getKeyCode() == KeyEvent.VK_LEFT) {
                                c.turnLeft();
                        // Right-arrow turns right.
                        } else if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
                                c.turnRight();
                        // Spacebar fires.
                        } else if(e.getKeyCode() == KeyEvent.VK_SPACE) {
                                c.fire();
                        }
		}
		else {
			// fill this for all remaining packet types
		
		}
	}

}
