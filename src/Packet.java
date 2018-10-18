import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.PublicKey;


public class Packet {
	
	public PublicKey sender;
	public PublicKey reciever;
	
	public byte[] data;
	
	public Packet(PublicKey senderKey, PublicKey recieverKey, byte[] inputData){
		this.data = inputData;
		this.reciever = recieverKey;
		this.sender = senderKey;
	}
	
	//Constructor with a serialized byte array.
	public Packet(byte[] serialized) throws IOException, ClassNotFoundException {
		ByteArrayInputStream in = new ByteArrayInputStream(serialized);
	    ObjectInputStream is = new ObjectInputStream(in);
	    
	    Packet temp = (Packet) is.readObject();
	    this.sender = temp.sender;
	    this.reciever = temp.reciever;
	    this.data = temp.data;
	}
	
	//Return an byte array of this serialized object.
	public byte[] getBytes() throws IOException {
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    ObjectOutputStream os = new ObjectOutputStream(out);
	    os.writeObject(this);
	    return out.toByteArray();
	}
	
}