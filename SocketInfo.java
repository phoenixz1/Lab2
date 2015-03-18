import java.lang.*;
import java.io.*;
import java.util.*;
import java.net.*;

public class SocketInfo implements Serializable {

	private InetAddress addr;
	private int port;
	
	public SocketInfo(Socket info){
		addr = info.getInetAddress();
		port = info.getPort();
	}

	public SocketInfo(InetAddress _addr, int _port){
		addr = _addr;
		port = _port;
	}	
	
	public InetAddress getInetAddress(){
		return addr;
	}

	public int getPort(){
		return port;
	}
}
