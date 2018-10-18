import java.net.Socket;
import java.net.SocketAddress;
import java.security.PublicKey;
import java.util.ArrayList;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class ClientConnection extends Client implements Runnable{
	
	private Socket connection;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	
	public ClientConnection(){
		try {
			connection = new Socket(URL, PORT);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			this.in = new ObjectInputStream(connection.getInputStream());
			this.out = new ObjectOutputStream(connection.getOutputStream());
			
			if(!authenticate()) return;
			
			while(connection.isConnected()) {
				sync();
			}
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	//Authenticates with the server based on the server side code.
	private boolean authenticate() {
		//Auth to server
		try {
			out.writeObject(myPublicKey);						// 1. Send Client's PublicKey to Server.		
		
			serverPublicKey = (PublicKey) in.readObject();		// 2. Receive Server's PublicKey.
			
			Packet encryptedExam = (Packet) in.readObject();	// 3. Receive Randomly Generated Exam from the server.
			
			Message decryptedExam = new Message(Security.decrypt(myPrivateKey, encryptedExam.data));	// 4. Decrypt the message.
			
			// 5. Re encrypt the sent message with server's key and create a new packet for the server.
			Packet reEncryptedExam = new Packet(myPublicKey, serverPublicKey, Security.encrypt(serverPublicKey, decryptedExam.getBytes()));
			
			out.writeObject(reEncryptedExam);					// 6. Send the reEncryptedExam to the server.
			
			return true;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	//Writes the outbox to the server and reads the server messages and passes them to route.
	private void sync() throws Exception {

		int toSyncMessages = outbox.size();
		out.writeInt(toSyncMessages);
		
		int availableMessages = in.readInt();
		
		//For every package in the outbox encrypt it and send it to the stream.
		for(int i = 0 ; i < toSyncMessages; i++) {
			byte[] encryptedPacket = Security.encrypt( serverPublicKey , outbox.poll().getBytes());
			out.writeObject(new Packet(myPublicKey, serverPublicKey, encryptedPacket));
		}
		
		//For every incoming package, decrypt it and route it to the appropriate inbox.
		for(int j = 0; j < availableMessages; j++) {
			Packet inbound = (Packet) in.readObject();
			
			//If the packet was not signed by the server for this client.
			if ((!inbound.reciever.equals(myPublicKey)) || (!inbound.sender.equals(serverPublicKey))) {
				//TODO: proper error handling.
				return;
			}
			
			//Route the decrypted packet from server (The message is still encrypted).
			route(new Packet(Security.decrypt(myPrivateKey, inbound.data)));
		}
	}
	
	//Routes the incoming package to the appropriate sender.
	private void route(Packet recieved) {
		
		//Check to see if this packet was meant for the receiver.
		if( !recieved.reciever.equals(myPublicKey)) {
			//TODO: Error message here.
			return;
		}
		
		//Check if there is a previous conversation with the sender, if not create it.
		if( !myData.contains(recieved.sender)) {
			myData.put(recieved.sender, new ArrayList<Message>());
		}
		
	}
	
}
