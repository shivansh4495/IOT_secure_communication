import Crypto.InitialKey;
import Crypto.RSAencrypt;

import java.util.Scanner;

public class Client{
    /*
    * Class for Standard Call Lib*/
    public static void main(String[] args){

        // Call this method if the key isn't exists
//        InitialKey init = new InitialKey();
//        init.generateKey();

        Scanner scanner = new Scanner(System.in);
        System.out.println("Config HostName:");
        String host = scanner.nextLine();

        System.out.println("Config PortNumber:");
        int port = Integer.valueOf(scanner.nextLine()); //43212

        System.out.println("=== Key Pair Checker and Repairer ===");
        RSAencrypt rsAencrypt = new RSAencrypt();
        try{
            // Check public key if not exists goto exception
            rsAencrypt.readPublicKey(RSAencrypt.PUBLIC_KEY);
        }catch (Exception e){
            System.out.println("---> Key Not Found and Ready to Fix <---");
            InitialKey initialKey = new InitialKey();
            initialKey.generateKey();
        }

        Thread client = new Thread(new ClientThread(host,port));
        client.start();
    }
}