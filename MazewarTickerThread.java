import java.util.*;

public class MazewarTickerThread()extends Thread{
	private Map<Integer, MazewarServerHandlerThread> clients;
	
	public MazewarTickerThread(Map<Integer, MazewarServerHandlerThread> clients){
		super("MazewarServerHandlerThread");
		this.clients = clients;
		
	}
	// might need a while loop in run()
	// TickerThread should only be created on receiving a 'fire' packet ?
	//added forever loop. 
	//ticker is like a server-side clock that broadcast the tick signal every 200ms
	public void run(){
		while(true){
		MazewarPacket tik =  new MazewarPacket();
		tik.type = MW_TICK;
		broadcast(tik); //can multiple broadcast cause problems? plz confirm
		// no, but there isnt a broadcast function in this class
		//thought broadcast was static for some reason. oops
		try{
			Thread.sleep(200);
		}catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	}
	
	public void broadcast(MazewarPacket currPacket) {

		Set<Integer> s = clients.keySet();
		for(int tid : s) {
			MazewarServerHandlerThread t = clients.get(tid);
       			t.send(currPacket);
 		}
	}
} 
