import java.net.*;
import java.io.*;
import java.util.*;


public class MazewarServerHandlerThread extends Thread {
	
 	public int tID;	
	public String cID;
	public Socket socket = null;
	private Map<String, SocketInfo> clients;

	
	public MazewarServerHandlerThread (int tID, Socket socket, Map<String, SocketInfo> clients) 
	{
		super("MazewarServerHandlerThread");
		this.tID = tID;
		this.socket = socket;
		this.clients = clients;
		System.out.println("Created new Thread to handle client; tID = "+ tID);
	}
	
	


	public void run() {

		boolean gotByePacket = false;
		try {
			/* stream to read from client */
			ObjectInputStream fromClient = new ObjectInputStream(socket.getInputStream());		
			MazewarPacket packetFromClient;

			/* stream to write back to client */
			ObjectOutputStream toClient = new ObjectOutputStream(socket.getOutputStream());


			while (( packetFromClient = (MazewarPacket) fromClient.readObject()) != null) 
			{				
				
				MazewarPacket packetToClient = new MazewarPacket();				
				
				if(packetFromClient.type == MazewarPacket.JOIN_SERV) {
					System.out.println("join packet received");
					//synchronized(this) {
						if(clients.size() == 0)	{
							MazewarServer.leader = packetFromClient.cID;
						}
						cID = packetFromClient.cID;
						packetToClient.type = MazewarPacket.JOIN_SERV;
						packetToClient.cconns = new LinkedHashMap<String, SocketInfo>(clients);
						packetToClient.leader = MazewarServer.leader;
						toClient.writeObject(packetToClient);
						clients.put(packetFromClient.cID,new SocketInfo(this.socket));
						System.out.println("sent join serv to client");
						/*if(clients.size() == 1) {
							MazewarPacket initpkt = new MazewarPacket();
							initpkt.type = MazewarPacket.RING_TOKEN;
							toClient.writeObject(packetToClient);	
						System.out.println("sent token to client");					
						}*/	
					//}
					continue;
					
				}
				else if(packetFromClient.type == MazewarPacket.MW_ELECTION) {
					MazewarServer.leader = packetFromClient.cID;
					continue;

				}
				else if(packetFromClient.type == MazewarPacket.MW_BYE || packetFromClient.type == MazewarPacket.MW_NULL) {
					if(packetFromClient.cID == cID) {						
						gotByePacket = true;
						break;
					}
					else {
						synchronized(this) { clients.remove(packetFromClient.cID); }
					}
				}
				else {/* if code comes here, there is an error in the packet */
					System.out.println("UNKNOWN PACKET SENT TO SERVER BY "+packetFromClient.cID);
					System.exit(-1);
				}

				
			}
			/* cleanup when client exits */
			//synchronized(this) {
				fromClient.close();
				toClient.close();
				clients.remove(cID);
				socket.close();
			//}

		} catch (IOException e) {
			if(!gotByePacket)
				e.printStackTrace();
		} catch (ClassNotFoundException e) {
			if(!gotByePacket)
				e.printStackTrace();
		}
	}

	public void send(MazewarPacket pkt){
		;
	}

	
}
