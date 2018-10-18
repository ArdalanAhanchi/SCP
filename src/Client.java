import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;

public class Client {
	
	protected final static int PORT = 1375;
	protected final static String URL = "127.0.0.1";
	private final static String KEY_FILE = "Keys.scp"; 
	
	protected static PrivateKey myPrivateKey;
	protected static PublicKey myPublicKey;
	
	protected static PublicKey serverPublicKey;
	
	protected static Hashtable<PublicKey, ArrayList<Message>> myData;		//Holds the contacts along with the messages in a hash table.
	protected static Queue<Packet> outbox;								//Holds the Packets that are waiting to get sent.
	
	public static void main(String[] Args) {
		initialize();
		connect();
		
		try { send(null, null); } 
		catch (Exception e) { e.printStackTrace(); }
		
	}
	
	private static void connect() {
		ClientConnection connection = new ClientConnection();
		
		Thread t = new Thread(connection);
		t.start();
	}
	
	private static void initialize()  {
		//TODO: Check the saved files to see if they exist. If yes, load the variables.
		//If not create the key pairs
		
		myData = new Hashtable<PublicKey, ArrayList<Message>>();
		outbox = new LinkedList<Packet>();
		
		//Initialize the keys
		try {
			KeyPair myKeys = Security.buildKeyPair();
			myPrivateKey = myKeys.getPrivate();
			myPublicKey = myKeys.getPublic();
			
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	//TODO: Modify or remove after testing...
	//Saves the keyPair to the output file.
	private static void saveKeys() throws IOException {
		FileOutputStream fout = new FileOutputStream(KEY_FILE);
		ObjectOutputStream oos = new ObjectOutputStream(fout);
		oos.writeObject(myPublicKey);
		oos.writeObject(myPrivateKey);
	}
	
	//A method for sending messages to the receiver.
	private static void send(Message toSend, PublicKey recieverKey) throws IOException, Exception {
		
		if(!myData.contains(recieverKey)) {						//Check to see if the key exists, if not add the key to myData.
			myData.put(recieverKey, new ArrayList<Message>());
		}
		
		myData.get(recieverKey).add(toSend);					//Add the message to myData.
		
		//Create the packet and add it to the outbox.
		Packet newPacket = new Packet(myPublicKey, recieverKey, Security.encrypt(recieverKey, toSend.getBytes()));
		outbox.add(newPacket);
	}
	
	
}
