import java.util.*;

public class MazewarTickerThread()extends Thread{
	
	public MazewarTickerThread(){
		super("MazewarServerHandlerThread");
	}
	// might need a while loop in run()
	// TickerThread should only be created on receiving a 'fire' packet ?
	public void run(){
		MazewarPacket tik =  new MazewarPacket();
		tik.type = MW_TICK;
		broadcast(tik); //can multiple broadcast cause problems? plz confirm
		// no, but there isnt a broadcast function in this class
		try{
			Thread.sleep(200);
		}catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
} 
