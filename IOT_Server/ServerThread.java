import Crypto.AESencryption;
import Crypto.RSAdecrypt;
import Crypto.RSAencrypt;

import java.io.*;
import java.net.Socket;
import java.sql.*;
import java.util.Base64;

public class ServerThread extends Thread {

    /*
     * Server initial decrypt model.
     */
    RSAdecrypt rsAdecrypt = new RSAdecrypt();

    Socket client = null;
    PrintWriter output;
    BufferedReader input;

    public ServerThread(Socket client) {
        this.client = client;
    }


    /*
     * Client send: =============================================
     * 1. Mac -> transfer it to database and get RSA private key
     * 2. encrypted AES key -> use the private key to decrypt
     * 3. testing packet and will response 200 OK
     * ==========================================================
     * Start Transfer data
     */
    @Override
    public void run() {
        System.out.println("Connection established");
        try {
            // Create PrintWriter and BufferedReader on the socket
            output = new PrintWriter(client.getOutputStream(), true);
            input = new BufferedReader(new InputStreamReader(client.getInputStream()));
            System.out.println("-*- Server response ready -*-");

            /* Server handle client connection */

            String str;
            boolean bye = false;
            // Server say hello to client -- Step 1
            output.println("Hello Client ...");
            while (!bye) {
                str = null;
                byte[] byStr = null;
                while ((str = input.readLine()) != null) {
                    System.out.println("Client send : " + str);
                    // catch signal code to response
                    String index = str.substring(0, 1);
                    System.out.println(index);
                    // Signal switch
                    switch (index){
                        case "M":
                            // JDBC connect database
                            try{
                                // get mac and prepare sql statement
                                String mac = str.substring(1);
                                String sql = "SELECT pri FROM `otp` WHERE mac='" + mac + "';";
                                // Load Driver
                                Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
                                // connection establish
                                String dburl = "jdbc:mysql://localhost:3306/app_project?verifyServerCertificate=false&useSSL=true&autoReconnect=true";
                                String user = "root";
                                String passwd = "root";
                                Connection connection = DriverManager.getConnection(dburl, user, passwd);
                                if (connection != null && !connection.isClosed())
                                    System.out.println("=== Server message : Database connected ===");
                                // prepare sql-execute parameters
                                String privateKey_b64 = null;
                                Statement statement = connection.createStatement();
                                ResultSet result = statement.executeQuery(sql);
                                while(result.next()) {
                                    privateKey_b64 = result.getString("pri");
                                    //System.out.println(privateKey_b64);
                                    byte[] rsaKey = Base64.getDecoder().decode(privateKey_b64);
                                    File file = new File("private.key");
                                    BufferedOutputStream bof = new BufferedOutputStream(new FileOutputStream(file));
                                    bof.write(rsaKey);
                                    bof.flush();
                                    bof.close();
                                    // === RSA Key Ready ===
                                    System.out.println("=== RSA Key Ready ===");
                                }

                            }catch (ClassNotFoundException e){
                                System.out.println("## Error : Driver class failed to load ##");
                            }catch (SQLException e){
                                System.out.println("## Error : database connect failed ##\n" + e.getMessage());
                            } catch (Exception e){
                                e.printStackTrace();
                            }
                            // Server response 1
                            output.println("Server response : Step 1 / 200 OK");

                            break;
                        case "R":
                            // RSA decrypt model loading
                            str = str.substring(1);
                            byte[] aesEnc = Base64.getDecoder().decode(str);
                            String aesB64 = rsAdecrypt.decryptData(aesEnc);
                            byte[] aesKey = Base64.getDecoder().decode(aesB64);
                            File file = new File(AESencryption.filename);
                            BufferedOutputStream bof = new BufferedOutputStream(new FileOutputStream(file));
                            bof.write(aesKey);
                            bof.flush();
                            bof.close();
                            // === AES Key Ready ===
                            // Server response 2
                            output.println("Server response : Step 2 / 200 OK");

                            break;
                        case "A":
                            // AES packet test
                            String _original = "Flag: {AES_TEST_PACKET}";

                            str = str.substring(1);
                            String _test = AESencryption.decrypt(str);
                            System.out.println(_test);
                            if (_test.equals(_original)){
                                // Server response 3
                                output.println("Server response : Step 3 / 200 OK");

                            }else{
                                output.println("--- Packet Check Failed ---");
                            }
                            System.out.println("TEST SUCCESS");
                            break;
                        case "P":
                            // Data transfer with client
                            str = str.substring(1);
                            String data = AESencryption.decrypt(str);
                            System.out.println("Server received: " + data);
                            output.println("========== Response Header ==========");
                            output.println("Server Message : 200 OK\nServer token : " + data);
                            output.println("========== Response Finish ==========");
                            output.flush();

                            break;
                    }
                }
            }
            /*
             * Server process and execute cryptography */
            //output.println(str);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("server-thread error  Reason: Died");
        } finally {
            // close connection from client
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Connection Closed");
        }
    }
}
