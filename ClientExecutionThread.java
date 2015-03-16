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
	private Map<String, Socket> clist;
	
	
	
	public ClientExecutionThread (Queue<MazewarPacket> incomingQueue, Map<String, Client> clients) 
	{
		super("ClientExecutionThread");
		this.inQueue = incomingQueue;
		this.players = clients;
		
	}
	
	


	public void run() {
		
		// poll inQueue for packets, read packet, executePacket()
		while(true){
			if(inQueue.size() != 0) { // Something is in the queue
			
			MazewarPacket head = inQueue.remove();
			
			if(head != null) {
				executePacket(head);
			}
			}
		}
    }
	
	public void executePacket(MazewarPacket pkt) {
	
		String cID = pkt.cID;
		Client c = players.get(cID);
		assert(c != null);
		
		if(pkt.type == MazewarPacket.MW_REPLY) { // Client event to process
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
		else if(pkt.type == MazewarPacket.MW_BYE) { // Client wants to quit the game
			// Remove the client from the hash map of players active in the game and from the maze
			players.remove(cID);
			c.maze.removeClient(c);
			
			if(c.getType() == 25) { // Local client is quitting; exit from the Mazewar application
				Mazewar.quit();
			}
		}
		else if(pkt.type == MazewarPacket.MW_TICK){
			c.maze.missiletick();
		}
		else if(pkt.type == MazewarPacket.JOIN_SERV){
			clist = pkt.clist;
            MazewarPacket multicastpkt = new MazewarPacket();
            multicastpkt.type = MazewarPacket.CLIENTINFO_REQUEST;
            multicastpkt.cID = this.getName();
            Socket clientsocket = new Socket(hostname, port);//hostname and desired port of self
            multicastpkt.newSocket = clientsocket;
            
			multicast(clist, multicastpkt);
		}
		else if(pkt.type == MazewarPacket.CLIENTINFO_REQUEST){
			MazewarPacket pkttonew = new MazewarPacket();
			pkttonew.type = MazewarPacket.CLIENTINFO_REPLY;
			pkttonew.cID = this.getName();
			pkttonew.StartPoint = this.getPoint();
			pkttonew.dir =  this.getOrientation().toString();
			
			
			
			
		}
		else { // Other types have no actions
			return;
		}
	}

}
