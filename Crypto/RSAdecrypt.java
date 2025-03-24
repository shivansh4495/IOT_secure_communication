package Crypto;

import javax.crypto.Cipher;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.RSAPrivateKeySpec;

/**
 * Created by rb on 2017/11/6.
 */
public class RSAdecrypt {

    public static final String PRIVATE_KEY = "private.key";

    public String decryptData(byte[] data){
        System.out.println("\n ============ DECRYPTION STARTED ============");
        byte[] decryptedData;
        String plain = null;
        try{
            PrivateKey privateKey = readPrivateKey(PRIVATE_KEY);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE,privateKey);
            decryptedData = cipher.doFinal(data);
            plain = new String(decryptedData);
            System.out.println("Decrypted Data: " + plain);
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("============ DECRYPTION COMPLETED ============");
        return plain;
    }

    public PrivateKey readPrivateKey(String fileName) throws Exception{
        FileInputStream fileInputStream = null;
        ObjectInputStream objectInputStream = null;
        try{
            fileInputStream = new FileInputStream(new File(fileName));
            objectInputStream = new ObjectInputStream(fileInputStream);

            // Saved Private Key >>> uncompressed data with modulus & exponent
            BigInteger modulus = (BigInteger)objectInputStream.readObject();
            BigInteger exponent = (BigInteger)objectInputStream.readObject();

            RSAPrivateKeySpec rsaPrivateKeySpec = new RSAPrivateKeySpec(modulus,exponent);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(rsaPrivateKeySpec);

            return privateKey;
        }finally {
            if (objectInputStream != null)
                objectInputStream.close();
            if(fileInputStream != null)
                fileInputStream.close();
        }
    }
}
