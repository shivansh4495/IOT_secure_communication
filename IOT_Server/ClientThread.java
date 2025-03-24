import Crypto.AESencryption;
import Crypto.RSAencrypt;

import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.Base64;
import java.util.Scanner;

public class ClientThread extends Thread {
    /*
     * Client initial encrypt model.
     */
    RSAencrypt rsAencrypt = new RSAencrypt();

    private String hostname;
    private int port;
    private String str = null;
    private boolean pi = false;

    public ClientThread(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public ClientThread(String hostname, int port, String str) {
        this.hostname = hostname;
        this.port = port;
        this.str = str;
    }

    public ClientThread(String hostname, int port, String str, boolean pi) {
        this.hostname = hostname;
        this.port = port;
        this.str = str;
        this.pi = pi;
    }

    /*
     * Thread start will execute run() automatically
     * enableWrite & enableRead -> restrict loop parameters
     * enableWrite pass: =======================
     * 1. Mac Address 2. AES Key 3. test-packet
     * =========================================
     */
    @Override
    public void run() {
        try {
            System.out.print("Establishing connection ...");
            // initial Communication Object
            PrintWriter output = null;
            BufferedReader input = null;
            // Let loop still work
            boolean enableWrite = false;
            boolean enableRead = true;
            String mode = "Mac";
            // Socket Client Thread created
            Socket socket = new Socket(hostname, port);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
            // ----- Maybe change to source address or data -------
            Scanner scanner = new Scanner(System.in);
            String str = null;
            // Three way hand shaking
            for(int i=0;i<3;i++){
                str = null;
                if (mode.equals("Mac") || mode.equals("RSA") || mode.equals("AES")) {
                    // Checker for data transfer status
                    while (enableRead) {
                        while ((str = input.readLine()) == null) ;
                        System.out.println(str);
                        enableWrite = true;
                        enableRead = false;
                    }
                    while (enableWrite) {
                        // Three case to transfer -- switch
                        switch (mode){
                            case "Mac":
                                //Signal : M
                                InetAddress ip = InetAddress.getLocalHost();
                                NetworkInterface network = NetworkInterface.getByInetAddress(ip);
                                byte[] mac = network.getHardwareAddress();
                                // Format to Mac Address
                                StringBuilder sb = new StringBuilder();
                                for(int j=0;j<mac.length;j++){
                                    sb.append(String.format("%02X%s",mac[j],(j<mac.length-1) ? "-" : ""));
                                }
                                String macAdr = sb.toString();
                                // debug
                                System.out.println(macAdr);
                                // concat signal code 'M'
                                macAdr = "M" + macAdr;
                                // Send Mac to server
                                output.println(macAdr);
                                mode = "RSA";
                                break;
                            case "RSA":
                                // Signal : R
                                // --- AES Key packing ---
                                AESencryption.generateKey();
                                // Read Key and encoding with Base64
                                File file = new File(AESencryption.filename);
                                int length = (int)file.length();
                                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
                                byte[] bytes = new byte[length];
                                bis.read(bytes, 0, length);
                                bis.close();
                                String aesKey = Base64.getEncoder().encodeToString(bytes);
                                System.out.println(aesKey);
                                // Send AES Key with RSA to Server
                                String result = Base64.getEncoder().encodeToString(rsAencrypt.encryptData(aesKey));
                                System.out.println(result);
                                // concat Signal code 'R'
                                result = "R" + result;
                                output.println(result);
                                mode = "AES";
                                break;
                            case "AES":
                                //Signal : A
                                String _test = "Flag: {AES_TEST_PACKET}";
                                // AES Key encrypt with Base64
                                String _aes = AESencryption.encrypt(_test);
                                // concat Signal code 'A'
                                _aes = "A" + _aes;
                                System.out.println(_aes);
                                output.println(_aes);
                                break;
                        }

                        /* Pi Version : communication between both of Pi */

                        // To identify that source input from where
//                        if (!pi) {
//                            byte[] byStr = rsAencrypt.encryptData(str);
//                            encoded = Base64.getEncoder().encodeToString(byStr);
//                        } else {
//                            encoded = str;
//                        }
                        enableRead = true;
                        enableWrite = false;
                    }
                }else{
                    i--;
                }
            }

            /* Check -- step 3 -- response is 200 OK or not */

            boolean dataTransfer = false;
            while (enableRead) {
                while ((str = input.readLine()) == null) ;
                System.out.println(str);
                if(str.equals("Server response : Step 3 / 200 OK"))
                    dataTransfer = true;
                enableRead = false;
                enableWrite = true;
                System.out.println("-*- Three-way handshaking finished -*-");
            }
            while(dataTransfer) {
                while(enableWrite){
                    // > Signal code - 'P'
                    String data = null;
                    data = scanner.nextLine();
                    data = AESencryption.encrypt(data);
                    data = "P" + data;
                    output.println(data);
                    // Lock & Unlock
                    enableWrite = false;
                    enableRead = true;
                    System.out.println(data);
                }
                while (enableRead) {
                    while ((str = input.readLine()) == null) ;
                    do {
                        System.out.println(str);
                        if(str.equals("========== Response Finish =========="))
                            break;
                    }while ((str = input.readLine()) != null);
                    // debug
                    System.out.println(">>> Finish Catch response");
                    // Lock & Unlock
                    enableRead = false;
                    enableWrite = true;
                }
            }
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}