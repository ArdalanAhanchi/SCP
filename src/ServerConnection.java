import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Random;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class ServerConnection extends Server implements Runnable {
	
	static final int STRING_SIZE = 32; 
	
	private Socket connection;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	
	private PublicKey clientPublicKey;
	private PublicKey serverPublicKey;
	
	public ServerConnection(Socket input){
		connection = input;
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
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//Authenticates with the client and identifies them.
	private boolean authenticate() {
		
		try {
			
			clientPublicKey = (PublicKey) in.readObject();				// 1. Receive Client's PublicKey.

			if(!users.contains(clientPublicKey)) return false;			// 2. Check if the supposed client is registered.
			serverPublicKey = users.get(clientPublicKey).getPublic();	// 3. Lookup Server's PublicKey for the client.
			
			out.writeObject(serverPublicKey);	// 4. Send the server's public key for the given client to the client.
			
			String randomString = randString();	// 5. Generate a random string for authentication.
			
			// 6. Encrypt the message.
			byte[] encryptedMessage = Security.encrypt(clientPublicKey, new Message(randomString).getBytes()); 
			
			// 7. create a new packet.
			Packet randText = new Packet(serverPublicKey, clientPublicKey, encryptedMessage);		 
			
			// 8. Send the packet to the client.
			out.writeObject(randText); 						
			
			// 9. Decrypt the packet and create a new message.
			Message reSentMessage = new Message(Security.decrypt(users.get(clientPublicKey).getPrivate(), ((Packet) in.readObject()).data));
			
			// 10. Check the original string with the decrypted string for authentication.
			if(!randomString.equals(reSentMessage.getText())) return false; 
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return true;
	}
	
	//Returns a random string of size STRING_SIZE for authentication.
	private String randString() {
		    byte[] buffer = new byte[STRING_SIZE];
		    new Random().nextBytes(buffer);
		    String randomString = new String(buffer, Charset.forName("UTF-8"));
		    return randomString;
	}
	
	//Writes the outbox to the server and reads the server messages and passes them to route.
	private void sync() throws Exception {
		
		int availableMessages = in.readInt();
		
		int toSyncMessages = userData.get(clientPublicKey).size();
		out.writeInt(toSyncMessages);
			
		//For every incoming package, decrypt it and route it to the appropriate inbox.
		for(int j = 0; j < availableMessages; j++) {
			Packet inbound = (Packet) in.readObject();
						
			//If the packet was not signed by the sender for this server.
			if ((!inbound.reciever.equals(serverPublicKey)) || (!inbound.sender.equals(clientPublicKey))) {
				//TODO: proper error handling.
				return;
			}
						
			//Route the decrypted packet to the proper user (The message is still encrypted).
			route(new Packet(Security.decrypt(users.get(clientPublicKey).getPrivate(), inbound.data)));
		}
		
		//For every package in the user's outbox encrypt it and send it to the stream.
		for(int i = 0 ; i < toSyncMessages; i++) {
			byte[] encryptedPacket = Security.encrypt( clientPublicKey , userData.get(clientPublicKey).poll().getBytes());
			out.writeObject(new Packet(serverPublicKey, clientPublicKey, encryptedPacket));
		}
			
		
	}
		
	//Routes the incoming package to the appropriate sender.
	private void route(Packet recieved) {
			
		//Check to see if this packet was sent from the client.
		if( !recieved.sender.equals(clientPublicKey)) {
			//TODO: Error message here.
			return;
		}
		
		//Add the packet to the destinations queue.
		userData.get(recieved.reciever).add(recieved); 			
			
	}
}
