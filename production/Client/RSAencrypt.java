package Crypto;

import javax.crypto.Cipher;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;

/**
 * Created by rb on 2017/11/1.
 */
public class RSAencrypt {

    public static final String PUBLIC_KEY = "public.key";

    public byte[] encryptData(String data){
        System.out.println("\n============ ENCRYPTION STARTED ============");
        System.out.println("Data before encrypt: "+data);
        byte[] data2Encrypt = data.getBytes();
        byte[] encryptedData = null;
        try{
            PublicKey publicKey = readPublicKey(PUBLIC_KEY);
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE,publicKey);
            encryptedData = cipher.doFinal(data2Encrypt);
            System.out.println("Encrypted Data: " + encryptedData);
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("============ ENCRYPTION COMPLETED ============");
        return encryptedData;
    }

    public PublicKey readPublicKey(String fileName) throws Exception{
        FileInputStream fileInputStream = null;
        ObjectInputStream objectInputStream = null;
        try{
            fileInputStream = new FileInputStream(new File(fileName));
            objectInputStream = new ObjectInputStream(fileInputStream);

            // Saved Public Key >>> uncompressed data with modulus & exponent
            BigInteger modulus = (BigInteger)objectInputStream.readObject();
            BigInteger exponent = (BigInteger)objectInputStream.readObject();

            RSAPublicKeySpec rsaPublicKeySpec = new RSAPublicKeySpec(modulus,exponent);
            KeyFactory fact = KeyFactory.getInstance("RSA");
            PublicKey publicKey = fact.generatePublic(rsaPublicKeySpec);
            return publicKey;
        }finally {
            if (objectInputStream != null)
                objectInputStream.close();
            if(fileInputStream != null)
                fileInputStream.close();
        }
    }
}
