/*
Copyright (C) 2004 Geoffrey Alan Washburn
    
This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.
    
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
    
You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
USA.
*/

import java.lang.*;
import java.util.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;
/**
 * An abstract class for {@link Client}s in a {@link Maze} that local to the 
 * computer the game is running upon. You may choose to implement some of 
 * your code for communicating with other implementations by overriding 
 * methods in {@link Client} here to intercept upcalls by {@link GUIClient} and 
 * {@link RobotClient} and generate the appropriate network events.
 * @author Geoffrey Washburn &lt;<a href="mailto:geoffw@cis.upenn.edu">geoffw@cis.upenn.edu</a>&gt;
 * @version $Id: LocalClient.java 343 2004-01-24 03:43:45Z geoffw $
 */


public abstract class LocalClient extends Client {

	// queue to store the incoming packets from server
	public static final LinkedBlockingQueue<MazewarPacket> inQueue = new LinkedBlockingQueue<MazewarPacket>();
		 
	// map of containing active players on maze (Client.name<String> --> <Client Object>)
	public static final Map<String, Client> clients = new HashMap();

        public static ScoreTableModel scoreModel = null;

	// ***Lab3*** map containing connections to all clients
	public static final Map<String, SocketInfo> clientsconn = new HashMap();

	public static final Map<String, Socket> p2psockets = new HashMap();

	public static final Map<String, ClientReceiverThread> p2pthreads = new HashMap();
	
	// ***Lab3*** Socket to communicate to the next client in the ring
	public static Socket nextclientSkt = null;
	public static String nextclient = null;

	public static ObjectOutputStream nextclientstream = null; // <-- initializa this. used in delete
	
	// ***Lab3*** Queue to store outgoing packets (to be multicasted on token receive)
	public static final LinkedBlockingQueue<MazewarPacket> outQueue = new LinkedBlockingQueue<MazewarPacket>();
	
	public static MazewarTickerThread ticker;

	// thread to listen from server and enqueue packets
	static ClientReceiverThread enquethread;
		
	// thread to dequeue and process packets
	ClientExecutionThread dequethread;

	// Thread to listen for client connection requests
	ClientListenerThread listenThread;
	
	// Host name and port number of the Mazewar server

	String hostname;
	int port;
	int defaultport= 8002;
	// Socket and streams to communicate to the server with
	Socket srvSocket = null;
	ServerSocket ownSocket = null;
        public static ObjectOutputStream outStream = null;
        public static ObjectInputStream inStream = null;

	// boolean flag to indicate if localclient is party leader
	public static boolean isleader;

	public static volatile boolean ispaused;
	public static volatile  int ACKnum;

        public LocalClient(String name, int ctype, String host, int port){
	    super(name, ctype);
        }

        /** 
         * Create a {@link Client} local to this machine.
         * @param name The name of this {@link Client}.
         * @param hostname The hostname of the the Mazewar server
         * @param port The port number of the Mazewar server
         */

        public LocalClient(String name, int ctype, String host, int port_num, ScoreTableModel scModel) {
            super(name, ctype);
            assert(name != null);
            
            hostname = host;
            port = port_num;
	    ispaused = false;
	    ACKnum = 0;

	    scoreModel = scModel;
            try {
            	// Initialize the socket to the server's host name and port #
            	srvSocket = new Socket(hostname,port);
		ownSocket = new ServerSocket(defaultport);
            	System.out.println("Socket created.");
            	outStream = new ObjectOutputStream(srvSocket.getOutputStream());
            	//inStream = new ObjectInputStream(srvSocket.getInputStream());

		System.out.println("Socket & input stream created.");
            } catch (UnknownHostException e) {
		System.err.println("ERROR: Don't know where to connect!!");
		System.exit(1);
	    } catch (IOException e) {
		System.err.println("ERROR: Couldn't get I/O for the connection.");
		e.printStackTrace();
		System.exit(1);
	    }
	    enquethread = new ClientReceiverThread(srvSocket, inQueue, true);
	    dequethread = new ClientExecutionThread(inQueue, outQueue, clients, name, clientsconn, ispaused, ACKnum);
	    listenThread = new ClientListenerThread(ownSocket, inQueue);
	    ticker = null;

        }

	public void startthreads(){
	    enquethread.start();
	    dequethread.start();
	    listenThread.start();
	}		

}
