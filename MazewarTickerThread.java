import java.util.*;
import java.net.*;
import java.lang.*;
import java.io.*;

public class MazewarTickerThread extends Thread{
	private Map<String, SocketInfo> clientsconn;
	private String localID;
	
	public MazewarTickerThread(Map<String, SocketInfo> clientsconn, String thisID){
		super("MazewarTickerThread");
		this.clientsconn = clientsconn;
		this.localID = thisID;

		System.out.println("Created MazewarTickerThread");
		
	}
	// might need a while loop in run()
	// TickerThread should only be created on receiving a 'fire' packet ?
	//added forever loop. 
	//ticker is like a server-side clock that broadcast the tick signal every 200ms
	public void run(){
	
		try{

			while(!Thread.interrupted()){
			MazewarPacket tik =  new MazewarPacket();
			tik.type = MazewarPacket.MW_TICK;

			broadcast(tik); 
			Thread.sleep(200);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	
	}
	
	public void broadcast(MazewarPacket currPacket) {

		if (!clientsconn.isEmpty()){
			Iterator i = clientsconn.keySet().iterator();
			
			while (i.hasNext()){
				Object o = i.next();
				assert(o instanceof String);

				SocketInfo info = clientsconn.get((String)o);

				// If the socket is from the local client, skip it
				if (((String)o).equals(localID)) {
				    continue;
				}
				try {
					Socket mcastSock = new Socket(info.getInetAddress(), info.getPort());
					ObjectOutputStream outStream = new ObjectOutputStream(mcastSock.getOutputStream());

					outStream.writeObject(currPacket);

					outStream.close();
					mcastSock.close();
				} catch (IOException e) {
					System.err.println("ERROR: Write failed for the multicast connection.");
					System.exit(1);
				}
			}
		}
		try{
			LocalClient.inQueue.put(currPacket);
		} catch(InterruptedException ex) {
			ex.printStackTrace();
		}
	}
} 
