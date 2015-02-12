import java.util.*;

public class MazewarTickerThread()extends Thread{
	
	public MazewarTickerThread(){
		super("MazewarServerHandlerThread");
	}
	
	public void run(){
		MazewarPacket tik =  new MazewarPacket();
		tik.type = MW_TICK;
		broadcast(tik); //can multiple broadcast cause problems? plz confirm
		try{
			Thread.sleep(200);
		}catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
} 
