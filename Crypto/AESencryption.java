package Crypto;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.AlgorithmParameters;
import java.security.SecureRandom;
import java.util.Base64;

public class AESencryption {
    // Key default password
    private static final String password = "GoldenKey";

    // Key parameters
    private static int pswdIterations = 65536;
    private static int keySize = 256; // AES-256
    private static String salt = genSalt();
    public static final String filename = "single.key";

    private static PBEKeySpec pbeKeySpec;
    private static byte[] ivBytes;
    private static byte[] saltByte;
    private static SecretKeySpec secretKeySpec;

    public static void main(String[] args) throws Exception {
        saltByte = salt.getBytes("UTF-8");
        // Generate Key

        //generateKey();

        // Test for encryption
        String b64enc = encrypt("Hello");
        System.out.println(b64enc);
        System.out.println(decrypt(b64enc));

        /*
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the AES input:");
        String input = scanner.nextLine();
        System.out.println("Your input : "+input);

        String handler = encrypt(input);
        System.out.println("AES with Base64 : " + handler);
        System.out.println("Decrypt MSG: "+ decrypt(handler));
        */
    }

    /* Generate Key Pair -> generateKey ; saveKey */
    public static void generateKey() {
        try {
            System.out.println("Key information packing ...");
            saltByte = salt.getBytes("UTF-8");
            saveKey(password, saltByte, pswdIterations, keySize);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveKey(String password, byte[] saltByte, int pswdIterations, int keySize) throws IOException {
        FileOutputStream fileOutputStream = null;
        ObjectOutputStream objectOutputStream = null;

        try {
            System.out.println(filename + " Creating ...");
            fileOutputStream = new FileOutputStream(filename);
            objectOutputStream = new ObjectOutputStream(new BufferedOutputStream(fileOutputStream));

            objectOutputStream.writeObject(password);
            objectOutputStream.writeObject(saltByte);
            objectOutputStream.writeObject(pswdIterations);
            objectOutputStream.writeObject(keySize);
            System.out.println("Key generated Successfully");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (objectOutputStream != null)
                objectOutputStream.close();
            if (fileOutputStream != null)
                fileOutputStream.close();
        }
    }

    /* Read Key by saved key module - pbeKeySpec */
    public static PBEKeySpec readKey(String filename) throws IOException {
        // Read AES Key information and construct KeySpec
        FileInputStream fileInputStream = null;
        ObjectInputStream objectInputStream = null;
        try {
            fileInputStream = new FileInputStream(new File(filename));
            objectInputStream = new ObjectInputStream(fileInputStream);

            String password = (String) objectInputStream.readObject();
            byte[] saltByte = (byte[]) objectInputStream.readObject();
            int pswdIterations = (int) objectInputStream.readObject();
            int keySize = (int) objectInputStream.readObject();

            pbeKeySpec = new PBEKeySpec(
                    password.toCharArray(),
                    saltByte,
                    pswdIterations,
                    keySize
            );
            return pbeKeySpec;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (objectInputStream != null)
                objectInputStream.close();
            if (fileInputStream != null)
                fileInputStream.close();
        }
    }

    // Encrypt and Decrypt pre-fix execute
    public static void __doInitial() throws Exception{
        pbeKeySpec = readKey(filename);
        // Constructs secret keys using the Password-Based Key Derivation Function
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        SecretKey secretKey = factory.generateSecret(pbeKeySpec);
        secretKeySpec = new SecretKeySpec(secretKey.getEncoded(), "AES");
    }

    public static String encrypt(String plainText) throws Exception {
        // initialization
        __doInitial();
        // ===== Start Encrypt =====
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        AlgorithmParameters parameters = cipher.getParameters();

        ivBytes = parameters.getParameterSpec(IvParameterSpec.class).getIV();
        byte[] encrypted = cipher.doFinal(plainText.getBytes("UTF-8"));

        System.out.println("Encrypt with AES: " + encrypted);

        //Base64 encoding
        Base64.Encoder encoder = Base64.getEncoder();
        String output = encoder.encodeToString(encrypted);
        return output + "," + Base64.getEncoder().encodeToString(ivBytes);
    }

    public static String decrypt(String encryptedText) throws Exception {
        // initialization
        __doInitial();
        String[] result = encryptedText.split(",");
        // Decrypt Base64
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] encrypted = decoder.decode(result[0]);

        /* Code removed by MVC concept > __doInitial */
//        Cipher old = Cipher.getInstance("AES/CBC/PKCS5Padding");

        // ENCRYPT MODE TO TAKE IV-PARAMETER
//        old.init(Cipher.ENCRYPT_MODE, secretKeySpec);
//        AlgorithmParameters parameters = old.getParameters();
//        ivBytes = parameters.getParameterSpec(IvParameterSpec.class).getIV();
        ivBytes = Base64.getDecoder().decode(result[1]);

        // DECRYPT MODE INITIAL
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(ivBytes));

        // -*- Decrypt process -*-
        byte[] decrypt = cipher.doFinal(encrypted);
        return new String(decrypt);
    }

    public static String genSalt() {
        SecureRandom secRand = new SecureRandom();
        byte salts[] = new byte[keySize / 8];
        secRand.nextBytes(salts);
        return new String(salts);
    }
}

