import java.io.*;
import java.net.*;
import java.util.*;

public class MazewarServer {

    // Maps to store the client names and port numbers, and handler threads in insertion order
    public static final Map<String, Socket> clients = new LinkedHashMap<String, Socket>();
    public static final ArrayList<MazewarServerHandlerThread> handlrthreads = new ArrayList<MazewarServerHandlerThread>();

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

	int threadnum = 0;
        while (listening) { // listen and enqueue
		threadnum++;
        	MazewarServerHandlerThread hthread = new MazewarServerHandlerThread(threadnum, serverSocket.accept(), clients);
		handlrthreads.add(hthread);
		hthread.start();
		syncmaps();
        }
        serverSocket.close();
    } 

    public static void syncmaps() {
	// delete handlrthreads that have been removed from clients, to delete all infor on quitting client
	
	System.out.println("Fn: syncmaps");
	System.out.println("Clients:" + clients.keySet());
	System.out.println("Leader:" + handlrthreads[0].cID);
   }
}

