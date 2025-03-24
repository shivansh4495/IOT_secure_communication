package Crypto;

import java.util.Scanner;

public class CryptoGraphy{

    private static final String PUBLIC_KEY = "public.key";
    private static final String PRIVATE_KEY = "private.key";

    public static void main(String[] args){
        RSAencrypt rsAencrypt = new RSAencrypt();
        RSAdecrypt rsAdecrypt = new RSAdecrypt();
        Scanner scanner = new Scanner(System.in);
        try{
            rsAencrypt.readPublicKey(PUBLIC_KEY);
            rsAdecrypt.readPrivateKey(PRIVATE_KEY);
        }catch (Exception e){
            InitialKey initialKey = new InitialKey();
            initialKey.generateKey();
        }

        System.out.println("Please enter string you want: ");
        String data = scanner.nextLine();
        // FUNC CALL with 'AESencryption':encrypt
        byte[] storeData = rsAencrypt.encryptData(data);
        System.out.println();

        System.out.println("Please wait 5 second to decrypt");
        // Just waiting for seconds without purpose
        int index = 1;
        while (index < 6) {
            try {
                System.out.println("Waiting for " + index + " second");
                Thread.sleep(1000);
                index++;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println();
        // FUNC CALL with 'AESencryption':decrypt
        rsAdecrypt.decryptData(storeData);
    }
}