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
		while(true){
			if(inQueue.element == null) { // Nothing is in the queue
				break;
			}
			
			MazewarPacket head = inQueue.remove();
			
			if(head != null) {
				executePacket(head);
			}
		}
    }
	
	public void executePacket(MazewarPacket pkt) {
	
		String cID = pkt.cID;
		Client c = players.get(cID);
		assert(c != null);
		
		if(pkt.type == MW_REPLY) { // Client event to process
			KeyEvent e = pkt.event;
			// c.execute_command(e);
			
                        // Up-arrow moves forward.
                        if(e.getKeyCode() == KeyEvent.VK_UP) {
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
		else if(pkt.type == MW_JOIN) { // New remote client wants to join the game
			// Create a new remote client with the given name in the packet
			RemoteClient newClnt = new RemoteClient(cID, 50);
			maze.addClient(newClnt);
			Mazewar.addKeyListener(newClnt);
			players.put(cID, newClnt);
		}
		else if(pkt.tpye == MW_BYE) { // Client wants to quit the game
			// Remove the client from the hash map of players active in the game and from the maze
			players.remove(cID);
			maze.removeClient(c);
			
			if(c.getType() == 25) { // Local client is quitting; exit from the Mazewar application
				Mazewar.quit();
			}
		}
		else { // Other types have no actions
			return;
		}
	}

}
