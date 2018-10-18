import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Cipher;

public class Security {
	
	final static int KEY_SIZE = 2048;
    
    /*public static void main(String [] args) throws Exception {
        // generate public and private keys
        KeyPair keyPair = buildKeyPair();
        PublicKey pubKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();
        
        // encrypt the message
        byte [] encrypted = encrypt(privateKey, (new String("This is a secret message").getBytes()));     
        System.out.println(new String(encrypted));  // <<encrypted message>>
        
        // decrypt the message
        byte[] secret = decrypt(pubKey, encrypted);                                 
        System.out.println(new String(secret));     // This is a secret message
    }*/

    public static KeyPair buildKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(KEY_SIZE);      
        return keyPairGenerator.genKeyPair();
    }

    public static byte[] encrypt(PublicKey publicKey, byte [] data) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");  
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);  

        return cipher.doFinal(data);  
    }
    
    public static byte[] decrypt(PrivateKey privateKey, byte [] encrypted) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");  
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        
        return cipher.doFinal(encrypted);
    }
    
}