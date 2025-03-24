package Crypto;

import java.io.*;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.*;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Base64;

import static java.lang.System.exit;

/**
 * Created by rb on 2017/11/1.
 */
public class InitialKey {
    private static final String PUBLIC_KEY = "public.key";
    private static final String PRIVATE_KEY = "private.key";

    public void generateKey(){
        try{
            System.out.println("============ GENERATE PUBLIC/PRIVATE KEY ============");
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048); // 2048-bits
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();
            /* KeyPair for public and private key */
            System.out.println("Public Key - " + publicKey);
            System.out.println("Private Key - " + privateKey);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            RSAPublicKeySpec rsaPublicKeySpec = keyFactory.getKeySpec(publicKey,RSAPublicKeySpec.class);
            RSAPrivateCrtKeySpec rsaPrivateCrtKeySpec = keyFactory.getKeySpec(privateKey,RSAPrivateCrtKeySpec.class);
            /* Print Something information about Keypair */
            System.out.println("\n============ SOME PARAMETERS ABOUT KEYPAIR ============\n");
            System.out.println("PublicKey Modulus: " + rsaPublicKeySpec.getModulus());
            System.out.println("PublicKey Exponent: " + rsaPublicKeySpec.getPublicExponent());
            System.out.println("Private Modulus: " + rsaPrivateCrtKeySpec.getModulus());
            System.out.println("Private Exponent: " + rsaPrivateCrtKeySpec.getPrivateExponent());

            /* Save the Key to file.key */
            System.out.println("\n============ SAVING KEY INTO FILE ============\n");
            InitialKey initialKey = new InitialKey();
            initialKey.saveKey(PUBLIC_KEY,rsaPublicKeySpec.getModulus(),rsaPublicKeySpec.getPublicExponent());
            initialKey.saveKey(PRIVATE_KEY,rsaPrivateCrtKeySpec.getModulus(),rsaPrivateCrtKeySpec.getPrivateExponent());
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void saveKey(String filename, BigInteger mod, BigInteger exp) throws IOException{
        FileOutputStream fileOutputStream = null;
        ObjectOutputStream objectOutputStream = null;
        try{
            System.out.println(filename + " Creating ...");
            fileOutputStream = new FileOutputStream(filename);
            objectOutputStream = new ObjectOutputStream(new BufferedOutputStream(fileOutputStream));

            objectOutputStream.writeObject(mod);
            objectOutputStream.writeObject(exp);

            System.out.println("Key generated Successfully");

            objectOutputStream.close();
            fileOutputStream.close();
            // call JDBC insert data
            if(filename.equals(InitialKey.PRIVATE_KEY)) {
                insertKey(filename);
                // Key stored in DB, and destroy itself;
                File file = new File(filename);
                System.out.println("=== Private Key destroy program launch ===");
                if(file.delete()){
                    System.out.println("Private Key Deleted");
                }else{
                    System.out.println("Key delete Failed");
                }
                // Key checker
                File fileCheck = new File(filename);
                if(fileCheck.exists()){
                    System.out.println("Key is exists >>> program down");
                    exit(1);
                }else{
                    System.out.println("=== Key Checker : delete success ===");
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (objectOutputStream != null)
                objectOutputStream.close();
            if (fileOutputStream != null)
                fileOutputStream.close();
        }
    }
    private void insertKey(String filename) throws Exception{
        InetAddress ip = InetAddress.getLocalHost();
        NetworkInterface network = NetworkInterface.getByInetAddress(ip);
        byte[] mac = network.getHardwareAddress();
        // Format to Mac Address
        StringBuilder sb = new StringBuilder();
        for(int j=0;j<mac.length;j++){
            sb.append(String.format("%02X%s",mac[j],(j<mac.length-1) ? "-" : ""));
        }
        String macAdr = sb.toString();

        File file = new File(filename);
        int length = (int)file.length();
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        byte[] bytes = new byte[length];
        bis.read(bytes, 0, length);
        bis.close();
        String pri = Base64.getEncoder().encodeToString(bytes);

        String sql = "INSERT INTO `otp`(id,mac,pri) VALUES('','" + macAdr + "','" + pri +"');";
        Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
        String dburl = "jdbc:mysql://localhost:3306/app_project?sql_mode='NO_ENGINE_SUBSTITUTION'&verifyServerCertificate=false&useSSL=true&autoReconnect=true&jdbcCompliantTruncation=false";
        String user = "user";
        String passwd = "user";
        Connection connection = DriverManager.getConnection(dburl, user, passwd);
        if (connection != null && !connection.isClosed())
            System.out.println("== Server message: Database connected ==");
        Statement statement = connection.createStatement();
        int result = statement.executeUpdate(sql);
        System.out.println(result + " >>> Setting complete");
    }
}
