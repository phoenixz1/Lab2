import java.io.*;
import java.net.*;
import java.util.*;

public class MazewarServer {

    private static final int MAX_THREADS = 100;

    // create a global queue to store the incoming client packets
    public static final Queue<MazewarPacket> serverIncomingQueue = new LinkedList<MazewarPacket>();

    // global List of threads serving clients
    public static final Map<Integer, MazewarServerHandlerThread> clients = new HashMap();

    private static MazewarBroadcastThread broadcaster;
    
    private static MazewarTickerThread ticker;


    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = null;
        boolean listening = true;
        try {
        	if(args.length == 1) {
        		serverSocket = new ServerSocket(Integer.parseInt(args[0]));
        	} else {
        		System.err.println("ERROR: Invalid arguments!");
        		System.exit(-1);
        	}
        } catch (IOException e) {
            System.err.println("ERROR: Could not listen on port!");
            System.exit(-1);
        }

	int threadNum = 0;
	broadcaster = new MazewarBroadcastThread(threadNum, serverIncomingQueue, clients);
	ticker = new MazewarTickerThread();
	broadcaster.start();
	ticker.start();
        while (listening) { // listen and enqueue
		threadNum = ++threadNum % MAX_THREADS;
        	MazewarServerHandlerThread client = 
			new MazewarServerHandlerThread(threadNum, serverSocket.accept(), serverIncomingQueue, clients);
		clients.put(threadNum,client);
		client.start();
        }
        serverSocket.close();
    }
}

