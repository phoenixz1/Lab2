import java.net.*;
import java.io.*;
import java.util.*;
import java.net.*;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

public class ClientExecutionThread extends Thread {
	public static boolean ispaused;
	public static int ACKnum;
	public int defaultport = 8000;
	public int tID;
	public MazewarTickerThread ticker;	
	private Queue<MazewarPacket> inQueue;
	private Queue<MazewarPacket> outQueue;
	private Map<String, Client> players;
	private String localID;

	// ***Lab3*** map containing connections to all clients
	public static final Map<String, Socket> clientsconn = new HashMap(); 
	
	// ***Lab3*** Socket to communicate to the next client in the ring
	// Socket nextclientSkt = null;


	
	
	
	public ClientExecutionThread (Queue<MazewarPacket> inQueue, Queue<MazewarPacket> outQueue, Map<String, Client> clients, String localID) 
	{
		super("ClientExecutionThread");
		this.inQueue = inQueue;
		this.outQueue = outQueue;
		this.players = clients;
		this.ticker = NULL;
		this.localID = localID;
		this.ACKnum = 0;
		this.ispaused = false;

		LocalClient.isleader = false;
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
			sendack(cID);
		}
		else if(pkt.type == MazewarPacket.MW_BYE) { // Client wants to quit the game
			// Remove the client from the hash map of players active in the game and from the maze
			if(LocalClient.isleader) {
			// tell server a client has quit
			LocalClient.outstream.writeObject(pkt);
				
			}
			if(LocalClient.nextclientSkt.getInetAddress() == clientsconn.get(pkt.cID).getInetAddress()) {
			// update the next player
			
			}
			players.remove(pkt.cID);
			clientsconn.remove(pkt.cID);
			c.maze.removeClient(c);
			sendack(pkt.cID);
			
		}
		else if(pkt.type == MazewarPacket.MW_TICK){
			c.maze.missiletick();
		}

		else if(pkt.type == MazewarPacket.JOIN_SERV) { //list of clients and leader from naming server
			clientsconn = pkt.cconns;
			if(clientsconn.size()==1){
				LocalClient.isLeader = true;
			}
			else{
            			MazewarPacket leaderpkt = new MazewarPacket();
	            	leaderpkt.type = MazewarPacket.CLIENTINFO_REQUEST;
	            	leaderpkt.cID = localID;
       		     	Socket clientsocket = new Socket(InetAddress.getLocalHost(), defaultport); //local hostname and desired port of self
       		     	leaderpkt.newsocket = clientsocket;
       		     	Client c = players.get(localID);
       		     	leaderpkt.StartPoint = c.getPoint();
       		     	leaderpkt.dir = c.getOrientation().toString();
            
       		     	Socket leaderinfo = clientsconn.get(pkt.leader); 
       			Socket leadersocket = new Socket(leaderinfo.getInetAddress(), leaderinfo.getPort()); // actual socket for connectoin
			try {
				ObjectOutputStream outStream = new ObjectOutputStream(leadersocket.getOutputStream());
			} catch (IOException e) {
				System.err.println("ERROR: Couldn't get I/O for the leader connection.");
				System.exit(1);
			}
		
			try {
				outStream.writeObject(leaderpkt);
			} catch (IOException e) {
				System.err.println("ERROR: Could not write to the leader.");
				System.exit(1);
			}
		
			outStream.close();
			}
		}
		else if(pkt.type == MazewarPacket.CLIENTINFO_REQUEST){ //only leader receives this packet
			pkt.type = MazewarPacket.RING_PAUSE; // contains new client info, socket, send those to other clients
			int ACKmax = players.size();
			ACKnum = 0; //reset ack received
			sendmcast(pkt);
			Client c = players.get(pkt.leader);
			clientsconn.put(pkt.cID, pkt.newsocket);
			RemoteClient newClient = new RemoteClient(pkt.cID, 50);
			c.clients.put(pkt.cID, newClient);
			c.maze.addRemoteClient(newClient, pkt.StartPoint, new Direction(pkt.dir));
			
			MazewarPacket outpkt = new MazewarPacket();
			outpkt.type = MazewarPacket.RING_INFO;
			outpkt.clist = c.clients;
			outpkt.cconns = clientsconn;
			outpkt.newsocket = clientsconn.get(pkt.leader);
			//send clientmap, socketmap, ring to new client
			Socket socket = new Socket(pkt.newsocket.getInetAddress(),pkt.newsocket.getPort());
			ObjectInputStream newinStream = new ObjectInputStream(socket.getInputStream());
			ClientReceiverThread receivethread = new ClientReceiverThread(socket, inQueue, newinStream); //open a receive thread for the new client
			receivethread.start();
			try {
				ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
			} catch (IOException e) {
				System.err.println("ERROR: Couldn't get I/O for the newclient connection.");
				System.exit(1);
			}
		
			try {
				outStream.writeObject(outpkt);
			} catch (IOException e) {
				System.err.println("ERROR: Could not write to the newclient.");
				System.exit(1);
			}
		
			outStream.close();
			while(ACKnum < ACKmax);
			outpkt.type =  MazewarPacket.RING_UNPAUSE;
			sendmcast(outpkt);
			
		}
		else if(pkt.type == MazewarPacket.RING_INFO) { // only new client receives this
			ispaused = true;

			Client c= players.get(localID); //DOUBT: potentially empty?
			
			//creating receive threads
			if (!clientsconn.isEmpty()){
				Iterator i = clientsconn.entrySet().iterator();
			
				while (i.hasNext()){
					Object o = i.next();
					Socket s = (Socket)o;
					if(!s.getInetAddress().toString().equals(InetAddress.getLocalHost().toString())){
					Socket socket = new Socket(s.getInetAddress(),s.getPort());
					ObjectInputStream newinStream = new ObjectInputStream(socket.getInputStream());
					ClientReceiverThread receivethread = new ClientReceiverThread(socket, inQueue, newinStream); 
					receivethread.start();		
					}
				}
			}
			//adding remote clients
			if (!players.isEmpty()){
				Iterator i = pkt.clist.entrySet().iterator();
			
				while (i.hasNext()){
					Object o = i.next();
					Client temp = (Client)o;
					if(!temp.getName().equals(localID)){
						RemoteClient newClient = new RemoteClient(temp.getName(), 50);
						c.clients.put(temp.getName(), newClient);
						c.maze.addRemoteClient(newClient, temp.getPoint(), temp.getOrientation());	
					}
				}
				
			}
			//ACK to leader
			MazewarPacket ackpkt = new MazewarPacket();
			ackpkt.type = MazewarPacket.ACK;
			Socket leaderinfo = clientsconn.get(pkt.leader); 
            Socket leadersocket = new Socket(leaderinfo.getInetAddress(), leaderinfo.getPort()); // actual socket for connectoin
			try {
				ObjectOutputStream outStream = new ObjectOutputStream(leadersocket.getOutputStream());
			} catch (IOException e) {
				System.err.println("ERROR: Couldn't get I/O for the leader connection.");
				System.exit(1);
			}
		
			try {
				outStream.writeObject(ackpkt);
			} catch (IOException e) {
				System.err.println("ERROR: Could not write to the leader.");
				System.exit(1);
			}
		
			outStream.close(); 
			//pause
			while(is_paused) ;
			

		}
		else if(pkt.type == MazewarPacket.RING_PAUSE) { //non-leader clients get this
			ispaused = true;
			//add new client to all maps
			Client c = players.get(localID);
			clientsconn.put(pkt.cID, pkt.newsocket);
			RemoteClient newClient = new RemoteClient(pkt.cID, 50);
			c.clients.put(pkt.cID, newClient);
			c.maze.addRemoteClient(newClient, pkt.StartPoint, new Direction(pkt.dir));

			
			
			//add new client to ring
			Socket leader = clientsconn.get(pkt.leader);
			if(c.nextclientSkt.getInetAddress() == leader.getInetAddress()){
				LocalCLient.nextclientSkt = clientsconn.get(pkt.cID);
				LocalClient.nextclientstream = LocalClient.nextclientSkt.getOutputStream();
			}
			
			//open a receive thread for the new client
			Socket socket = new Socket(pkt.newsocket.getInetAddress(),pkt.newsocket.getPort());
			ObjectInputStream newinStream = new ObjectInputStream(socket.getInputStream());
			ClientReceiverThread receivethread = new ClientReceiverThread(socket, inQueue, newinStream); 
			receivethread.start();
			
			//ack to leader
			MazewarPacket ackpkt = new MazewarPacket();
			ackpkt.type = MazewarPacket.ACK;
			Socket leaderinfo = clientsconn.get(pkt.leader); 
            Socket leadersocket = new Socket(leaderinfo.getInetAddress(), leaderinfo.getPort()); // actual socket for connectoin
			try {
				ObjectOutputStream outStream = new ObjectOutputStream(leadersocket.getOutputStream());
			} catch (IOException e) {
				System.err.println("ERROR: Couldn't get I/O for the leader connection.");
				System.exit(1);
			}
		
			try {
				outStream.writeObject(ackpkt);
			} catch (IOException e) {
				System.err.println("ERROR: Could not write to the leader.");
				System.exit(1);
			}
		
			outStream.close(); 
			while(is_paused);
		}
		else if(pkt.type == MazewarPacket.RING_UNPAUSE) {

		}
		else if(pkt.type == MazewarPacket.RING_TOKEN) {
			sendmcast();
		}
		else if(pkt.type == MazewarPacket.MW_ELECTION) {
			// start a ticker thread
			ticker = new MazewarTickerThread();
			ticker.start();
			LocalClient.isleader = true;
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

	public void createring(MazewarPacket initpkt) {
		// Initialize the sockets to the clients' host names and port #s
		// Initialize socket to next client in ring

	}
	public void sendmcast() {
		// Dequeue and multicast the head of outqueue
		// Send RING_TOKEN to next client
	        // NOTE: Do not send the packet to the local client
	        //   - Process the packet immediately after receiving ACK's from all other clients

		if (!clientsconn.isEmpty()){
			Iterator i = clientsconn.entrySet().iterator();
			MazewarPacket outPacket = outQueue.remove();
			int ACKMax = players.size() - 1;
			Client localClient = players.get(localID);
		
			while (i.hasNext()){
				Object o = i.next();
				String clientName = (String) o.getKey();
				Socket info = (Socket) o.getValue();

				// If the socket is from the local client, skip it
				if (clientName.equals(localID)) {
				    continue;
				}

				Socket mcastSock = new Socket(info.getInetAddress(), info.getPort());
			
				try {
					ObjectOutputStream outStream = new ObjectOutputStream(mcastSock.getOutputStream());
				} catch (IOException e) {
					System.err.println("ERROR: Couldn't get I/O for the multicast connection.");
					System.exit(1);
				}
			
				try {
					outStream.writeObject(outPacket);
				} catch (IOException e) {
					System.err.println("ERROR: Write failed for the multicast connection.");
					System.exit(1);
				}

				outStream.close();
				mcastSock.close();
			}

			// Wait until all ACK's are received
			while(ACKNum < ACKMax) ;

			// All ACK's received; process the key event on the local client
			KeyEvent e = outPacket.event;

			// Up-arrow moves forward.
                        if(e.getKeyCode() == KeyEvent.VK_UP) {
                                localClient.forward();
                        // Down-arrow moves backward.
                        } else if(e.getKeyCode() == KeyEvent.VK_DOWN) {
                                localClient.backup();
                        // Left-arrow turns left.
                        } else if(e.getKeyCode() == KeyEvent.VK_LEFT) {
                                localClient.turnLeft();
                        // Right-arrow turns right.
                        } else if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
                                localClient.turnRight();
                        // Spacebar fires.
                        } else if(e.getKeyCode() == KeyEvent.VK_SPACE) {
                                localClient.fire();
                        }

			// Event processed; send RING_TOKEN to next client in the ring
			MazewarPacket ringPacket = new MazewarPacket();
			ringPacket.type = MazewarPacket.RING_TOKEN;

			try {
			    ObjectOutputStream nextOutStream = new ObjectOutputStream(LocalClient.nextclientSkt.getOutputStream());
			} catch (IOException e) {
			    System.err.println("ERROR: Could not get I/O for the next client connection");
			    Systme.exit(1);
			}

			try {
			    nextOutStream.writeObject(ringPacket);
			} catch (IOException e) {
			    System.err.println("ERROR: Write failed for the token passing.");
			    System.exit(1);
			}

			nextOutStream.close();
		}
	}

	public void sendmcast(MazewarPacket pkt) {
		// Special multicast to be used only by leader
		// send RING_STOP and RING_RESUME
		if (!clientsconn.isEmpty()){
			Iterator i = clientsconn.entrySet().iterator();
			int ACKMax = players.size() - 1;
			
			while (i.hasNext()){
				Object o = i.next();
				String clientName = (String) o.getKey();
				Socket info = (Socket) o.getValue();

				// If the socket is from the local client, skip it
				if (clientName.equals(localID)) {
				    continue;
				}

				Socket mcastSock = new Socket(info.getInetAddress(), info.getPort());
			
				try {
					ObjectOutputStream outStream = new ObjectOutputStream(mcastSock.getOutputStream());
				} catch (IOException e) {
					System.err.println("ERROR: Couldn't get I/O for the multicast connection.");
					System.exit(1);
				}
			
				try {
					outStream.writeObject(pkt);
				} catch (IOException e) {
					System.err.println("ERROR: Write failed for the multicast connection.");
					System.exit(1);
				}

				outStream.close();
				mcastSock.close();
			}

			// Wait until all ACK's are received
			while (ACKNum < ACKMax) ;
		}
	}

	public void sendack (String client) {
	    // Create an acknowledgement packet
	    MazewarPacket ackPkt = new MazewarPacket();
	    ackPkt.type = MazewarPacket.ACK;

	    // Create a socket to obtain the info on the destination
	    Socket destInfo = clientsconn.get(client);

	    Socket destSock = new Socket(destInfo.getInetAddress(), destInfo.getPort());

	    try {
		ObjectOutputStream destOutStream = new ObjectOutputStream(destSock.getOutputStream());
	    } catch (IOException e) {
		System.err.println("ERROR: Could not get I/O for the destination connection");
		System.exit(1);
	    }

	    try {
		destOutStream.writeObject(ackPkt);
	    } catch (IOException e) {
		System.err.println("ERROR: Write failed for the acknowledgement");
		System.exit(1);
	    }

	    destOutStream.close();
	    destSock.close();
	}

}
