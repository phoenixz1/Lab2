import java.net.*;
import java.io.*;
import java.util.*;
import java.net.*;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

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
			if(inQueue.element() == null) { // Nothing is in the queue
				break;
			}
			
			MazewarPacket head = inQueue.remove();
			
			if(head != null) {
				executePacket(head);
			}
		}
    }
	
	public void executePacket(MazewarPacket pkt) {
	
		String cID = pkt.Player;
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
		else if(pkt.type == MW_BYE) { // Client wants to quit the game
			// Remove the client from the hash map of players active in the game and from the maze
			players.remove(cID);
			c.maze.removeClient(c);
			
			if(c.getType() == 25) { // Local client is quitting; exit from the Mazewar application
				Mazewar.quit();
			}
		}
		else if(pkt.type == MW_TICK){
			c.maze.missiletick();
		}
		else { // Other types have no actions
			return;
		}
	}

}
