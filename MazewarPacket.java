import java.io.Serializable;
import java.awt.event.KeyEvent;

public class MazewarPacket implements Serializable {

	/* define packet formats */
	public static final int MW_NULL    = 0;
	public static final int MW_JOIN    = 100;
	public static final int MW_REQUEST = 200;
	public static final int MW_REPLY   = 300;
	public static final int MW_BYE     = 400;
	
	/* the packet payload */
	
	/* initialized to be a null packet */
	public int type = MW_NULL;
	
	/* send your message here */
	public String cID;
	public KeyEvent event = null;	

	/* send your message here */
	public String message;

	public int posx = 0;
	public int posy = 0;
	public Direction dir = null;
	
};
