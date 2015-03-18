import java.io.Serializable;
import java.awt.event.KeyEvent;
import java.util.*;
import java.net.*;
import java.io.*;
import java.lang.*;

public class MazewarPacket implements Serializable {

	/* define packet formats */
	public static final int MW_NULL        = 0;
	public static final int MW_JOIN        = 100;
	public static final int MW_REQUEST     = 200;
	public static final int MW_REPLY       = 300;
	public static final int MW_BYE         = 400;
	public static final int MW_START       = 500;
	public static final int MW_TICK	       = 600;
	public static final int MW_INIT	       = 700;
	/*new formats for p2p*/
	public static final int JOIN_SERV      = 800;
	public static final int MW_ELECTION    = 801;
	public static final int RING_INFO      = 802;
	public static final int RING_PAUSE     = 803;
	public static final int RING_UNPAUSE   = 804;
	public static final int RING_TOKEN     = 805;
	public static final int CLIENTINFO_REQUEST	=806;
	public static final int ACK	       = 900;
	public static final int MCAST_REQ	=901;
	/* the packet payload */
	
	/* initialized to be a null packet */
	public int type = MW_NULL;
	
	// Client name
	public String cID;
	
	// Key event
	public KeyEvent event = null;	

	// Client's starting position & orientation
	public Point StartPoint = null;
	public String dir = null;

	// Map object to hold list of clients in some cases (eg. join)
	public Map<String, Client> clist = null;
	//Map object of list of client sockets (hostname and port info)
	public Map<String, SocketInfo> cconns= null;
	
	public SocketInfo newsocket = null;
	// Name of elected party leader 
	public String leader;
	
};
