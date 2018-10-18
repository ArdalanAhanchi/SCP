import java.net.Socket;
import java.net.SocketAddress;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

public class Server {
	
	
	private final static int PORT = 1375;
	private final static String URL = "127.0.0.1";
	private final static String KEY_FILE = "Keys.scp"; 
	
	protected static Hashtable<PublicKey, Queue<Packet>> userData;	//Queue for each user. Upon sync all the messages are sent to the corresponding user.
	protected static Hashtable<PublicKey, KeyPair> users;				//Holds the keyPair for communication to users, user public keys are the main key for the hash table.
	
	public static void main(String[] Args) {
		initialize();
		connect();
	}
	
	private static void connect() {
		try {
			SocketAddress address = new InetSocketAddress(URL, PORT);
			ServerSocket host = new ServerSocket(PORT);
			//host.bind(address);
			
			while(true) {
				Socket s = host.accept();
				ServerConnection c = new ServerConnection(s);
				
				Thread t = new Thread(c);
				t.start();
				
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void initialize() {
		//TODO: init hashtable, (Load from save if available)
		userData = new Hashtable<PublicKey, Queue<Packet>> ();
		users = new Hashtable<PublicKey, KeyPair>();
	}
	
	//TODO: Modify or remove after testing...
	//Reads the keyPair from the keys file.
	private static void readKeys() throws IOException {
		FileInputStream fout = new FileInputStream(KEY_FILE);
		ObjectInputStream oos = new ObjectInputStream(fout);
		try {
			oos.readObject();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		oos.readObject();
	}
	
}
