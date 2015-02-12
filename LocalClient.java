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

        /** 
         * Create a {@link Client} local to this machine.
         * @param name The name of this {@link Client}.
         */
        public LocalClient(String name) {
                super(name);
                assert(name != null);
				
				try {
                        /* variables for hostname/port */
                        String hostname = "localhost";
                        int port = 4444;

                        if(args.length == 2 ) {
                                hostname = args[0];
                                port = Integer.parseInt(args[1]);
                        } else {
                                System.err.println("ERROR: Invalid arguments!");
                                System.exit(-1);
                        }
                        mwSocket = new Socket(hostname, port);

                        out = new ObjectOutputStream(echoSocket.getOutputStream());
                        in = new ObjectInputStream(echoSocket.getInputStream());

                } catch (UnknownHostException e) {
                        System.err.println("ERROR: Don't know where to connect!!");
                        System.exit(1);
                } catch (IOException e) {
                        System.err.println("ERROR: Couldn't get I/O for the connection.");
                        System.exit(1);
                }
				
				enquethread = new ClientReceiverThread(inQueue, inStream);
				dequethread = new ClientExecutionThread(inQueue); 
        }

        // queue to store the incoming packets from server
		public static final Queue<MazewarPacket> inQueue = new LinkedList<MazewarPacket>();
		 
		// map of containing active players on maze (Client.name<String> --> <Client Object>)
		public static final Map<String, MazewarServerHandlerThread> clients = new HashMap();
		
		// thread to listen from server and enqueue packets
		ClientReceiverThread enquethread;
		
		// thread to dequeue and process packets
		ClientExecutionThread dequethread;
		
		// Socket and streams to communicate to the server with
		Socket mwSocket = null;
        ObjectOutputStream outStream = null;
        ObjectInputStream inStream = null;
		
		/*
		*  TODO: 1. Handle commands
		*			- Create functions to handle key events
		*			- Create MW_REQUEST package
		*			- Send to server via "outStream"
		*        2. Handle Fire events
		*			- If a client dies, send MW_INIT package
		*			  to server for that client
		*		 3. ???
		*/
}