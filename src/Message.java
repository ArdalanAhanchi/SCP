import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDateTime;

public class Message {
	
	private final static String WRONG_TYPE_ERROR = "This message is not the required type.";
	
	//Enumeration for determining the message content type.
	public enum Type{
		TEXT, IMAGE, VIDEO, AUDIO, FILE, CONTACT, STATUS
	}
	
	public Type type;
	
	//Main data field.
	public byte[] data;
	
	//Methods and data values for time management.
	public LocalDateTime sentTime;
	public LocalDateTime deliveredTime;
	public LocalDateTime readTime;
	
	
	//Constructor with a serialized byte array of this object.
	public Message(byte[] serialized) throws IOException, ClassNotFoundException {
	    ByteArrayInputStream in = new ByteArrayInputStream(serialized);
	    ObjectInputStream is = new ObjectInputStream(in);
	    
	    Message temp = (Message) is.readObject();
	    this.type = temp.type;
	    this.data = temp.data;
	    this.sentTime = temp.sentTime;
	    this.deliveredTime = temp.deliveredTime;
	    this.readTime = temp.readTime;
	    
	}
	
	//Constructor and getter for text messages.
	public Message(String text ){
		this.type = Type.TEXT;
		this.data = text.getBytes(); 
	}
	
	public String getText() throws Exception {
		
		if(this.type != Type.TEXT) {
			throw new Exception(WRONG_TYPE_ERROR);
		}
		
		return new String(data);
	}
	
	//TODO: Constructor and Object for Images.
	//TODO: Constructor and Object for Videos.
	//TODO: Constructor and Object for Audio.
	//TODO: Constructor and Object for Files.
	//TODO: Constructor and Object for Contacts.
	//TODO: Constructor and Object for Status.
	
	//Check the delivery status.
	public boolean isSent() { return sentTime == null; }
	public boolean isDelivered() {	return deliveredTime == null; }
	public boolean isRead() { return readTime == null; }
		
	//Set the delivery status.
	public void sent() { this.sentTime =  java.time.LocalDateTime.now(); }
	public void delivered() { this.deliveredTime =  java.time.LocalDateTime.now(); }
	public void read() { this.readTime =  java.time.LocalDateTime.now(); }
	
	//Return an byte array of this serialized object.
	public byte[] getBytes() throws IOException {
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    ObjectOutputStream os = new ObjectOutputStream(out);
	    os.writeObject(this);
	    return out.toByteArray();
	}
		
	
}
